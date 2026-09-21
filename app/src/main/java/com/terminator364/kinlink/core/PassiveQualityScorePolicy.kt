package com.terminator364.kinlink.core

data class PassiveQualityScore(
    val score: Int,
    val summary: String
)

/**
 * Low-cost 0..100 passive quality index.
 *
 * It uses only Android framework state already observed by KINLINK. It is not a
 * throughput test and must never override NET_CAPABILITY_VALIDATED.
 */
object PassiveQualityScorePolicy {
    fun score(truth: NetworkTruth): PassiveQualityScore {
        if (truth.transport == Transport.NONE || truth.internetState == InternetState.OFFLINE) {
            return PassiveQualityScore(0, "Aucun accès Internet utilisable observé.")
        }

        var points = when (truth.internetState) {
            InternetState.VALIDATED -> 45
            InternetState.PARTIAL -> 24
            InternetState.CAPTIVE_PORTAL -> 12
            InternetState.STALLED -> 8
            InternetState.UNKNOWN -> 16
            InternetState.OFFLINE -> 0
        }

        points += when {
            truth.downstreamKbps <= 0 -> 4
            truth.downstreamKbps < 500 -> 2
            truth.downstreamKbps < 1_000 -> 5
            truth.downstreamKbps < 2_500 -> 10
            truth.downstreamKbps < 5_000 -> 15
            truth.downstreamKbps < 10_000 -> 20
            else -> 25
        }

        points += when {
            truth.upstreamKbps <= 0 -> 2
            truth.upstreamKbps < 256 -> 1
            truth.upstreamKbps < 750 -> 4
            truth.upstreamKbps < 1_500 -> 7
            else -> 10
        }

        if (truth.androidNotSuspended) points += 10
        if (truth.androidNotCongested) points += 5

        val radioBonus = when (truth.transport) {
            Transport.CELLULAR -> when (MobileRadioQualityPolicy.assess(truth).quality) {
                MobileRadioQuality.STRONG -> 5
                MobileRadioQuality.USABLE -> 3
                MobileRadioQuality.UNKNOWN -> 2
                MobileRadioQuality.WEAK -> 0
            }
            Transport.WIFI -> when (WifiRadioQualityPolicy.assess(truth).quality) {
                WifiRadioQuality.GOOD -> 5
                WifiRadioQuality.FAIR -> 3
                WifiRadioQuality.UNKNOWN -> 2
                WifiRadioQuality.WEAK -> 0
            }
            else -> 2
        }
        points += radioBonus

        val clamped = points.coerceIn(0, 100)
        val label = when {
            clamped >= 85 -> "fort"
            clamped >= 70 -> "bon"
            clamped >= 50 -> "moyen"
            clamped >= 30 -> "faible"
            else -> "très faible"
        }
        return PassiveQualityScore(
            clamped,
            "Indice passif $clamped/100 ($label), sans speedtest."
        )
    }
}
