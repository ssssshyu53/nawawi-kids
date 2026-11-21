package com.nawawikids.app.util

import kotlin.math.max
import kotlin.math.roundToInt

object ArabicUtils {
    private val arDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    private val tashkeelRegex = Regex("[\u064B-\u065F\u0670\u0640]")
    private val punctRegex = Regex("[«»﴿﴾().,،؛:!؟\\-\\s\"']")

    fun toArabicDigits(value: Int): String = toArabicDigits(value.toString())

    fun toArabicDigits(text: String): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(arDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatClock(seconds: Double): String {
        val safe = if (seconds.isFinite() && seconds >= 0) seconds else 0.0
        val totalSec = safe.toInt()
        val m = totalSec / 60
        val s = totalSec % 60
        return "%02d:%02d".format(m, s)
    }

    fun stripTashkeel(input: String): String = tashkeelRegex.replace(input, "")

    fun firstLetterHint(word: String): String {
        val bare = punctRegex.replace(stripTashkeel(word), "")
        val first = bare.firstOrNull() ?: '.'
        return "$first ..."
    }

    /**
     * Deterministic seeded shuffle matching the original kids.js progressive masking algorithm.
     */
    fun computeHiddenIndices(wordCount: Int, hadithId: Int, seedCounter: Int, hidePercent: Int): Set<Int> {
        if (hidePercent <= 0 || wordCount <= 0) return emptySet()
        if (hidePercent >= 100) return (0 until wordCount).toSet()

        val indices = MutableList(wordCount) { it }
        var seed = (hadithId * 997L + seedCounter * 131L + hidePercent.toLong()) and 0xFFFFFFFFL
        fun nextRand(): Double {
            seed = (seed * 1664525L + 1013904223L) and 0xFFFFFFFFL
            return seed.toDouble() / 4294967296.0
        }

        for (i in wordCount - 1 downTo 1) {
            val j = (nextRand() * (i + 1)).toInt().coerceIn(0, i)
            val tmp = indices[i]
            indices[i] = indices[j]
            indices[j] = tmp
        }
        val target = max(1, (wordCount * hidePercent / 100.0).roundToInt()).coerceAtMost(wordCount)
        return indices.take(target).toSet()
    }
}
