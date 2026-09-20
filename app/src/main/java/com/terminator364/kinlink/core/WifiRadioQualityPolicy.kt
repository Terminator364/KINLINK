package com.terminator364.kinlink.core

enum class WifiRadioQuality {
    UNKNOWN,
    WEAK,
    FAIR,
    GOOD
}

data class WifiRadioAssessment(
    val quality: WifiRadioQuality,
    val summary: String
)

object WifiRadioQualityPolicy {
    fun assess(truth: NetworkTruth): WifiRadioAssessment {
        if (truth.transport != Transport.WIFI) {
            return WifiRadioAssessment(WifiRadioQuality.UNKNOWN, "Hors Wi-Fi.")
        }
        val dbm = truth.signalStrengthDbm
            ?: return WifiRadioAssessment(
                WifiRadioQuality.UNKNOWN,
                "Puissance radio non exposée par Android."
            )

        return when {
            dbm <= -78 -> WifiRadioAssessment(
                WifiRadioQuality.WEAK,
                "Signal Wi-Fi faible ($dbm dBm)."
            )
            dbm <= -67 -> WifiRadioAssessment(
                WifiRadioQuality.FAIR,
                "Signal Wi-Fi moyen ($dbm dBm)."
            )
            else -> WifiRadioAssessment(
                WifiRadioQuality.GOOD,
                "Signal Wi-Fi bon ($dbm dBm)."
            )
        }
    }
}
