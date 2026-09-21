package com.terminator364.kinlink.core

enum class MobileRadioQuality {
    UNKNOWN,
    WEAK,
    USABLE,
    STRONG
}

data class MobileRadioAssessment(
    val quality: MobileRadioQuality,
    val summary: String
)

/**
 * Coarse passive cellular radio heuristic.
 *
 * Android may expose different radio-family measurements through
 * NetworkCapabilities.signalStrength. These thresholds are intentionally broad
 * and are never used to declare Internet unavailable or to change routing.
 */
object MobileRadioQualityPolicy {
    fun assess(truth: NetworkTruth): MobileRadioAssessment {
        if (truth.transport != Transport.CELLULAR) {
            return MobileRadioAssessment(
                MobileRadioQuality.UNKNOWN,
                "Qualité radio mobile non applicable hors cellulaire."
            )
        }
        val dbm = truth.signalStrengthDbm
            ?: return MobileRadioAssessment(
                MobileRadioQuality.UNKNOWN,
                "Android n’expose pas de niveau radio mobile exploitable."
            )

        return when {
            dbm <= -110 -> MobileRadioAssessment(
                MobileRadioQuality.WEAK,
                "Signal mobile passif faible ($dbm dBm); un refresh logiciel ne peut pas renforcer la radio."
            )
            dbm <= -100 -> MobileRadioAssessment(
                MobileRadioQuality.USABLE,
                "Signal mobile passif utilisable mais moyen ($dbm dBm)."
            )
            else -> MobileRadioAssessment(
                MobileRadioQuality.STRONG,
                "Signal mobile passif relativement fort ($dbm dBm)."
            )
        }
    }
}
