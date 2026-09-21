package com.terminator364.kinlink.core

/**
 * At most three local framework reads after an accepted Mobile Assist metric refresh.
 * These are one-shot callbacks, not a repeating poll and they emit no network traffic.
 */
object MobileAssistEvidenceSamplingPolicy {
    val sampleDelaysMs: LongArray = longArrayOf(21_000L, 42_000L, 65_000L)
    const val MAX_SAMPLES_PER_ACTION = 3

    fun valid(): Boolean =
        sampleDelaysMs.size == MAX_SAMPLES_PER_ACTION &&
            sampleDelaysMs.indices.drop(1).all { index ->
                sampleDelaysMs[index] > sampleDelaysMs[index - 1]
            } &&
            sampleDelaysMs.last() <= 65_000L
}
