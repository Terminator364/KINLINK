package com.terminator364.kinlink.core

enum class PassiveLinkQuality {
    UNKNOWN,
    CONSTRAINED,
    LIMITED,
    COMFORTABLE
}

data class PassiveLinkQualityAssessment(
    val quality: PassiveLinkQuality,
    val summary: String
)

/**
 * Heuristic only. Uses Android's passive bandwidth estimates; it is not a speed test
 * and must never be used alone to declare Internet unavailable.
 */
object PassiveLinkQualityPolicy {
    fun assess(truth: NetworkTruth): PassiveLinkQualityAssessment {
        val down = truth.downstreamKbps
        val up = truth.upstreamKbps
        if (down <= 0 && up <= 0) {
            return PassiveLinkQualityAssessment(
                PassiveLinkQuality.UNKNOWN,
                "Capacité non estimée par Android."
            )
        }

        return when {
            (down in 1..999) || (up in 1..255) ->
                PassiveLinkQualityAssessment(
                    PassiveLinkQuality.CONSTRAINED,
                    "Capacité Android estimée très limitée; Internet peut être validé mais rester lent."
                )
            (down in 1..4_999) || (up in 1..999) ->
                PassiveLinkQualityAssessment(
                    PassiveLinkQuality.LIMITED,
                    "Capacité Android estimée limitée; surveiller la stabilité avant toute action."
                )
            else ->
                PassiveLinkQualityAssessment(
                    PassiveLinkQuality.COMFORTABLE,
                    "Capacité Android estimée compatible avec un usage courant."
                )
        }
    }
}
