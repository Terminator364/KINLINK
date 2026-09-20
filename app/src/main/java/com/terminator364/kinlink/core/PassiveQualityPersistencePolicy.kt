package com.terminator364.kinlink.core

object PassiveQualityPersistencePolicy {
    const val REQUIRED_CONSECUTIVE_LOW_QUALITY = 3

    fun nextStreak(previous: Int, truth: NetworkTruth): Int {
        if (truth.transport != Transport.WIFI || truth.internetState != InternetState.VALIDATED) return 0
        val quality = PassiveLinkQualityPolicy.assess(truth).quality
        return if (
            quality == PassiveLinkQuality.CONSTRAINED ||
            quality == PassiveLinkQuality.LIMITED
        ) {
            (previous + 1).coerceAtMost(REQUIRED_CONSECUTIVE_LOW_QUALITY)
        } else 0
    }

    fun persistentLowQuality(streak: Int): Boolean =
        streak >= REQUIRED_CONSECUTIVE_LOW_QUALITY
}
