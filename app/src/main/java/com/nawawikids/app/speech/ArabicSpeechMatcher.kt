package com.nawawikids.app.speech

import com.nawawikids.app.model.WordTiming
import kotlin.math.max
import kotlin.math.min

/**
 * Pure Kotlin Arabic speech normalization and fuzzy sequential word matcher
 * powering real-time word-by-word reveal and automatic mistake detection.
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
        s = nonArabicWordChars.replace(s, " ")
        return multiSpace.replace(s, " ").trim()
    }

    fun tokenize(raw: String): List<String> {
        val norm = normalize(raw)
        if (norm.isEmpty()) return emptyList()
        return norm.split(" ").filter { it.isNotEmpty() }
    }

    /**
     * Checks if a spoken Arabic token matches an expected hadith word token,
     * tolerating diacritics, Alef/Teh/Yeh normalization, common "بن"/"ابن" variations,
     * and small ASR phonetic differences for kids.
     */
    fun tokensMatch(spokenRaw: String, expectedRaw: String): Boolean {
        val spoken = normalize(spokenRaw).replace(" ", "")
        val expected = normalize(expectedRaw).replace(" ", "")
        if (expected.isEmpty()) return true
        if (spoken.isEmpty()) return false
        if (spoken == expected) return true

        // Common classical Arabic ASR equivalences
        if ((spoken == "بن" && expected == "ابن") || (spoken == "ابن" && expected == "بن")) return true
        if ((spoken == "الرحمن" && expected == "الرحمان") || (spoken == "الرحمان" && expected == "الرحمن")) return true
        if ((spoken == "هذا" && expected == "هاذا") || (spoken == "ذلك" && expected == "ذالك")) return true
        if ((spoken == "اله" && expected == "الله") || (spoken == "الله" && expected == "لله")) return true

        // Short 1-2 character particles require exact or stripped-prefix match
        if (expected.length <= 2) {
            if (spoken.startsWith("و") && spoken.drop(1) == expected) return true
            if (expected.startsWith("و") && expected.drop(1) == spoken) return true
            return false
        }

        // Prefix/suffix tolerance for connecting "و" / "ف" / "ال"
        if (spoken.startsWith("و") && spoken.drop(1) == expected) return true
        if (expected.startsWith("و") && expected.drop(1) == spoken) return true

        // Fuzzy Levenshtein similarity for children's speech / ASR quirks
        val sim = similarity(spoken, expected)
        val threshold = when {
            expected.length <= 4 -> 0.74
            expected.length <= 7 -> 0.70
            else -> 0.68
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

    /**
     * Skips over pure punctuation items in WordTiming list (if any).
     */
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
        val consumedSpokenTokens: Int
    )

    /**
     * Evaluates newly spoken tokens against the expected position in [words].
     * Reveals matched words one-by-one and detects mistakes immediately when
     * the child pronounces a wrong word.
     */
    fun evaluateSpokenTokens(
        words: List<WordTiming>,
        currentTargetIndex: Int,
        newTokens: List<String>,
        isFinalResult: Boolean
    ): MatchEvaluation {
        var idx = advancePastPunctuation(words, currentTargetIndex)
        val newlyMatched = mutableListOf<Int>()
        var mistakeWord: String? = null
        var consumed = 0

        var tPos = 0
        while (tPos < newTokens.size && idx < words.size) {
            val token = newTokens[tPos]
            val targetText = words[idx].t

            // 1. Direct match on expected word
            if (tokensMatch(token, targetText)) {
                newlyMatched.add(idx)
                idx = advancePastPunctuation(words, idx + 1)
                consumed = tPos + 1
                mistakeWord = null
                tPos++
                continue
            }

            // 2. Check if ASR merged two consecutive words into one token (e.g. "عبدالله" for "عَبْدِ" + "اللهِ")
            val nextIdx = advancePastPunctuation(words, idx + 1)
            if (nextIdx < words.size) {
                val combinedExpected = normalize(targetText) + normalize(words[nextIdx].t)
                if (tokensMatch(token, combinedExpected)) {
                    newlyMatched.add(idx)
                    newlyMatched.add(nextIdx)
                    idx = advancePastPunctuation(words, nextIdx + 1)
                    consumed = tPos + 1
                    mistakeWord = null
                    tPos++
                    continue
                }
            }

            // 3. Check if ASR split one hadith word into two spoken tokens (e.g. "إن ما" for "إِنَّمَا")
            if (tPos + 1 < newTokens.size) {
                val combinedSpoken = token + newTokens[tPos + 1]
                if (tokensMatch(combinedSpoken, targetText)) {
                    newlyMatched.add(idx)
                    idx = advancePastPunctuation(words, idx + 1)
                    consumed = tPos + 2
                    mistakeWord = null
                    tPos += 2
                    continue
                }
            }

            // 4. 1-word lookahead tolerance ONLY if current word is a tiny particle or ASR skipped one word
            if (nextIdx < words.size && tokensMatch(token, words[nextIdx].t)) {
                val currNorm = normalize(targetText).replace(" ", "")
                if (currNorm.length <= 3 || (tPos + 1 < newTokens.size && nextIdx + 1 < words.size && tokensMatch(newTokens[tPos + 1], words[nextIdx + 1].t))) {
                    newlyMatched.add(idx)
                    newlyMatched.add(nextIdx)
                    idx = advancePastPunctuation(words, nextIdx + 1)
                    consumed = tPos + 1
                    mistakeWord = null
                    tPos++
                    continue
                }
            }

            // 5. Ignore accidental echo of the previous 1-2 already-matched words
            val isEchoOfPrevious = (1..2).any { back ->
                val prevIdx = idx - back
                prevIdx >= 0 && tokensMatch(token, words[prevIdx].t)
            }
            if (isEchoOfPrevious) {
                consumed = tPos + 1
                tPos++
                continue
            }

            // 6. Otherwise, detect mistake! On partial results, wait until we either have another token
            //    or token length >= 3 so half-spoken syllables don't trigger false alarms prematurely.
            if (isFinalResult || token.length >= 3 || tPos < newTokens.size - 1) {
                mistakeWord = token
                consumed = tPos + 1
                break
            } else {
                break
            }
        }

        return MatchEvaluation(
            newTargetIndex = idx,
            newlyMatchedIndices = newlyMatched,
            mistakeSpokenWord = mistakeWord,
            consumedSpokenTokens = consumed
        )
    }
}
