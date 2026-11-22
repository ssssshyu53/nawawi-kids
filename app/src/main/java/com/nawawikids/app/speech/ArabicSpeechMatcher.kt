package com.nawawikids.app.speech

import com.nawawikids.app.model.WordTiming
import kotlin.math.max
import kotlin.math.min

/**
 * Enhanced Arabic speech normalization and sliding-window sequential matcher
 * tailored for children reciting classical Prophetic Hadiths via Android SpeechRecognizer.
 *
 * Key improvements:
 * - Phonetic Arabic normalization (tolerates ذ/ز, ث/س, ظ/ض/ز, classical spellings, Alef/Teh/Yeh).
 * - Multi-hypothesis evaluation (evaluates top ASR alternatives and chooses the best advancement).
 * - Full-utterance alignment from utterance anchor so ASR live partial revisions never cause false mistakes.
 * - Conservative mistake triggering on partial results (only flags settled wrong words, never mid-syllable).
 */
object ArabicSpeechMatcher {
    private val diacriticsRegex = Regex("[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED\u0640]")
    private val nonArabicWordChars = Regex("[^\\u0621-\\u064A\\s]")
    private val multiSpace = Regex("\\s+")

    fun normalize(raw: String): String {
        var s = diacriticsRegex.replace(raw, "")
        s = s
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ة', 'ه')
            .replace('ى', 'ي')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace('ء', 'ا')
        s = nonArabicWordChars.replace(s, " ")
        return multiSpace.replace(s, " ").trim()
    }

    /**
     * Secondary phonetic folding for kids' pronunciation and common Arabic ASR confusions.
     */
    private fun phoneticFold(norm: String): String {
        return norm
            .replace('ذ', 'ز')
            .replace('ث', 'س')
            .replace('ظ', 'ز')
            .replace('ض', 'د')
            .replace('ق', 'ك')
    }

    fun tokenize(raw: String): List<String> {
        val norm = normalize(raw)
        if (norm.isEmpty()) return emptyList()
        return norm.split(" ").filter { it.isNotEmpty() }
    }

    fun tokensMatch(spokenRaw: String, expectedRaw: String): Boolean {
        val spoken = normalize(spokenRaw).replace(" ", "")
        val expected = normalize(expectedRaw).replace(" ", "")
        if (expected.isEmpty()) return true
        if (spoken.isEmpty()) return false
        if (spoken == expected) return true

        // Common classical Arabic & Hadith ASR equivalences
        val pairs = setOf(
            "بن" to "ابن",
            "الرحمن" to "الرحمان",
            "هذا" to "هاذا",
            "هذه" to "هاذه",
            "ذلك" to "ذالك",
            "لكن" to "لاكن",
            "اله" to "الله",
            "الله" to "لله",
            "الاه" to "الله",
            "صلي" to "صلا",
            "عليه" to "علي",
            "وسلم" to "سلم",
            "رضي" to "راضي",
            "عنه" to "عن",
            "عنها" to "عن",
            "عنهم" to "عن",
            "انما" to "ان ما",
            "الا" to "الي",
            "الي" to "علي",
            "عمر" to "عمرو",
            "ابا" to "ابي",
            "ابو" to "ابي"
        )
        if (pairs.contains(spoken to expected) || pairs.contains(expected to spoken)) return true

        // Connecting prefixes (و، ف، ب، ل) tolerance
        val prefixes = listOf('و', 'ف', 'ب', 'ل')
        for (p in prefixes) {
            if (spoken.length > 2 && spoken.first() == p && spoken.drop(1) == expected) return true
            if (expected.length > 2 && expected.first() == p && expected.drop(1) == spoken) return true
        }

        // Definite article "ال" tolerance for longer words
        if (spoken.length >= 4 && spoken.startsWith("ال") && spoken.drop(2) == expected) return true
        if (expected.length >= 4 && expected.startsWith("ال") && expected.drop(2) == spoken) return true

        // Short particles (1-2 chars)
        if (expected.length <= 2) {
            if (phoneticFold(spoken) == phoneticFold(expected)) return true
            return false
        }

        // Phonetic exact match
        val pSpoken = phoneticFold(spoken)
        val pExpected = phoneticFold(expected)
        if (pSpoken == pExpected) return true

        // Fuzzy Levenshtein similarity (use best of standard & phonetic-folded)
        val sim = max(similarity(spoken, expected), similarity(pSpoken, pExpected))
        val threshold = when {
            expected.length <= 3 -> 0.67
            expected.length <= 5 -> 0.66
            expected.length <= 8 -> 0.64
            else -> 0.62
        }
        return sim >= threshold
    }

    fun similarity(a: String, b: String): Double {
        if (a == b) return 1.0
        val maxLen = max(a.length, b.length)
        if (maxLen == 0) return 1.0
        val dist = levenshtein(a, b)
        return 1.0 - (dist.toDouble() / maxLen.toDouble())
    }

    private fun levenshtein(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        var prev = IntArray(n + 1) { it }
        var curr = IntArray(n + 1)
        for (i in 1..m) {
            curr[0] = i
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = min(
                    min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                )
            }
            val tmp = prev
            prev = curr
            curr = tmp
        }
        return prev[n]
    }

    fun advancePastPunctuation(words: List<WordTiming>, startIdx: Int): Int {
        var idx = startIdx
        while (idx < words.size && normalize(words[idx].t).isEmpty()) {
            idx++
        }
        return idx
    }

    data class MatchEvaluation(
        val newTargetIndex: Int,
        val newlyMatchedIndices: List<Int>,
        val mistakeSpokenWord: String?,
        val matchedTokenCount: Int
    )

    /**
     * Evaluates multiple ASR candidate transcriptions against the hadith starting from
     * [utteranceStartIndex] and picks the best alignment (furthest clean progress, or
     * smallest/most confident mistake if all hypotheses clearly diverge).
     */
    fun evaluateCandidates(
        words: List<WordTiming>,
        utteranceStartIndex: Int,
        currentTargetIndex: Int,
        candidates: List<String>,
        isFinalResult: Boolean
    ): MatchEvaluation {
        var best: MatchEvaluation? = null
        for (raw in candidates) {
            val tokens = tokenize(raw)
            if (tokens.isEmpty()) continue
            val eval = evaluateSingleUtterance(
                words = words,
                utteranceStartIndex = utteranceStartIndex,
                currentTargetIndex = currentTargetIndex,
                utteranceTokens = tokens,
                isFinalResult = isFinalResult
            )
            if (best == null || isBetterEvaluation(eval, best)) {
                best = eval
            }
        }
        return best ?: MatchEvaluation(
            newTargetIndex = currentTargetIndex,
            newlyMatchedIndices = emptyList(),
            mistakeSpokenWord = null,
            matchedTokenCount = 0
        )
    }

    private fun isBetterEvaluation(a: MatchEvaluation, b: MatchEvaluation): Boolean {
        // Prefer further advancement in hadith words
        if (a.newTargetIndex != b.newTargetIndex) return a.newTargetIndex > b.newTargetIndex
        // Tie-breaker: prefer no mistake over a mistake
        if ((a.mistakeSpokenWord == null) != (b.mistakeSpokenWord == null)) {
            return a.mistakeSpokenWord == null
        }
        return a.matchedTokenCount > b.matchedTokenCount
    }

    /**
     * Full-utterance alignment starting from [utteranceStartIndex].
     * Handles Android ASR live partial revisions gracefully without losing sync!
     */
    private fun evaluateSingleUtterance(
        words: List<WordTiming>,
        utteranceStartIndex: Int,
        currentTargetIndex: Int,
        utteranceTokens: List<String>,
        isFinalResult: Boolean
    ): MatchEvaluation {
        var idx = advancePastPunctuation(words, utteranceStartIndex.coerceAtMost(currentTargetIndex))
        val newlyMatched = mutableListOf<Int>()
        var mistakeWord: String? = null
        var tPos = 0

        while (tPos < utteranceTokens.size && idx < words.size) {
            val token = utteranceTokens[tPos]
            val targetText = words[idx].t

            // 1. Direct match
            if (tokensMatch(token, targetText)) {
                if (idx >= currentTargetIndex) newlyMatched.add(idx)
                idx = advancePastPunctuation(words, idx + 1)
                mistakeWord = null
                tPos++
                continue
            }

            // 2. ASR merged two consecutive words into one token (e.g. "عبدالله", "يارسول", "لااله")
            val nextIdx = advancePastPunctuation(words, idx + 1)
            if (nextIdx < words.size) {
                val combinedExpected = normalize(targetText) + normalize(words[nextIdx].t)
                if (tokensMatch(token, combinedExpected)) {
                    if (idx >= currentTargetIndex) newlyMatched.add(idx)
                    if (nextIdx >= currentTargetIndex) newlyMatched.add(nextIdx)
                    idx = advancePastPunctuation(words, nextIdx + 1)
                    mistakeWord = null
                    tPos++
                    continue
                }
            }

            // 3. ASR split one hadith word into two spoken tokens (e.g. "ان ما" for "إنما")
            if (tPos + 1 < utteranceTokens.size) {
                val combinedSpoken = token + utteranceTokens[tPos + 1]
                if (tokensMatch(combinedSpoken, targetText)) {
                    if (idx >= currentTargetIndex) newlyMatched.add(idx)
                    idx = advancePastPunctuation(words, idx + 1)
                    mistakeWord = null
                    tPos += 2
                    continue
                }
            }

            // 4. Smart 1-word or 2-word lookahead when ASR dropped a short/fast word
            if (nextIdx < words.size && tokensMatch(token, words[nextIdx].t)) {
                val currNorm = normalize(targetText).replace(" ", "")
                val nextNextIdx = advancePastPunctuation(words, nextIdx + 1)
                val confirmedByNext = tPos + 1 < utteranceTokens.size &&
                    nextNextIdx < words.size &&
                    tokensMatch(utteranceTokens[tPos + 1], words[nextNextIdx].t)
                if (currNorm.length <= 3 || confirmedByNext) {
                    if (idx >= currentTargetIndex) newlyMatched.add(idx)
                    if (nextIdx >= currentTargetIndex) newlyMatched.add(nextIdx)
                    idx = advancePastPunctuation(words, nextIdx + 1)
                    mistakeWord = null
                    tPos++
                    continue
                }
            }

            // 5. Ignore natural repeats/stutters of the previous 1-3 words (kids often repeat a word!)
            val isRepeatOfRecent = (1..3).any { back ->
                val prevIdx = idx - back
                prevIdx >= 0 && tokensMatch(token, words[prevIdx].t)
            }
            if (isRepeatOfRecent) {
                tPos++
                continue
            }

            // 6. If we haven't even reached currentTargetIndex yet (ASR revised an earlier word),
            //    skip that revised earlier token without penalizing the child.
            if (idx < currentTargetIndex) {
                tPos++
                continue
            }

            // 7. Genuine mismatch at or beyond currentTargetIndex!
            //    On partial results, wait until the child has said at least one more token
            //    (so half-spoken words aren't prematurely flagged as errors!).
            //    On final result, flag immediately if the spoken token has >= 2 chars.
            val hasFollowingToken = tPos < utteranceTokens.size - 1
            if (isFinalResult && token.length >= 2) {
                mistakeWord = token
                break
            } else if (!isFinalResult && hasFollowingToken && token.length >= 3) {
                mistakeWord = token
                break
            } else {
                // Still speaking current word — wait for more audio
                break
            }
        }

        val finalTarget = max(currentTargetIndex, idx)
        return MatchEvaluation(
            newTargetIndex = finalTarget,
            newlyMatchedIndices = newlyMatched,
            mistakeSpokenWord = mistakeWord,
            matchedTokenCount = tPos
        )
    }
}
