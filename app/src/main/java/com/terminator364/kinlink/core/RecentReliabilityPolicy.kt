package com.terminator364.kinlink.core

enum class RecentReliabilityBurden {
    QUIET,
    NOTICEABLE,
    UNSTABLE,
    SEVERE
}

object RecentReliabilityPolicy {
    fun classify(
        interruptionCount: Int,
        cumulativeMillis: Long,
        longestMillis: Long
    ): RecentReliabilityBurden = when {
        longestMillis >= 60_000L || cumulativeMillis >= 120_000L -> RecentReliabilityBurden.SEVERE
        longestMillis >= 30_000L || interruptionCount >= 4 || cumulativeMillis >= 30_000L ->
            RecentReliabilityBurden.UNSTABLE
        interruptionCount >= 1 || cumulativeMillis > 0L -> RecentReliabilityBurden.NOTICEABLE
        else -> RecentReliabilityBurden.QUIET
    }
}
