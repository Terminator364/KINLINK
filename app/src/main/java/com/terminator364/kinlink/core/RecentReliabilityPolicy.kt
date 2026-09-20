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
        longestMillis: Long,
        lowQualityEpisodeCount: Int = 0,
        lowQualityCumulativeMillis: Long = 0L,
        lowQualityLongestMillis: Long = 0L
    ): RecentReliabilityBurden = when {
        longestMillis >= 60_000L ||
            cumulativeMillis >= 120_000L ||
            lowQualityLongestMillis >= 10L * 60L * 1000L ||
            lowQualityCumulativeMillis >= 30L * 60L * 1000L ->
            RecentReliabilityBurden.SEVERE

        longestMillis >= 30_000L ||
            interruptionCount >= 4 ||
            cumulativeMillis >= 30_000L ||
            lowQualityEpisodeCount >= 4 ||
            lowQualityLongestMillis >= 5L * 60L * 1000L ||
            lowQualityCumulativeMillis >= 15L * 60L * 1000L ->
            RecentReliabilityBurden.UNSTABLE

        interruptionCount >= 1 ||
            cumulativeMillis > 0L ||
            lowQualityEpisodeCount >= 1 ||
            lowQualityCumulativeMillis > 0L ->
            RecentReliabilityBurden.NOTICEABLE

        else -> RecentReliabilityBurden.QUIET
    }
}
