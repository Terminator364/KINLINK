package com.terminator364.kinlink.core

enum class LongitudinalRadioPattern {
    INSUFFICIENT,
    RADIO_DOMINANT,
    CONGESTION_DOMINANT,
    SUSPENSION_DOMINANT,
    MIXED
}

data class LongitudinalRadioAssessment(
    val pattern: LongitudinalRadioPattern,
    val sampleCount: Int,
    val dominantCount: Int,
    val dominancePercent: Int,
    val summary: String
)

/**
 * Pure, passive-only reducer over already-retained cause-transition counts.
 * It emits no packets, performs no polling, and does not alter routing.
 */
object LongitudinalRadioEvidencePolicy {
    private const val MIN_SAMPLES = 3
    private const val DOMINANCE_PERCENT = 60

    fun assess(causeCounts: Map<String, Int>): LongitudinalRadioAssessment {
        val weak = causeCounts[PassiveProblemCause.WEAK_WIFI_SIGNAL.name].orZero()
        val congestion = causeCounts[PassiveProblemCause.CONGESTION_SUSPECT.name].orZero()
        val suspension = causeCounts[PassiveProblemCause.NETWORK_SUSPENDED.name].orZero()
        val total = weak + congestion + suspension

        if (total < MIN_SAMPLES) {
            return LongitudinalRadioAssessment(
                pattern = LongitudinalRadioPattern.INSUFFICIENT,
                sampleCount = total,
                dominantCount = maxOf(weak, congestion, suspension),
                dominancePercent = if (total == 0) 0 else maxOf(weak, congestion, suspension) * 100 / total,
                summary = "Preuves longitudinales insuffisantes ($total/$MIN_SAMPLES transitions radio pertinentes)."
            )
        }

        val dominantCount = maxOf(weak, congestion, suspension)
        val dominancePercent = dominantCount * 100 / total
        if (dominancePercent < DOMINANCE_PERCENT) {
            return LongitudinalRadioAssessment(
                LongitudinalRadioPattern.MIXED,
                total,
                dominantCount,
                dominancePercent,
                "Les causes radio récentes sont mixtes; aucune cause n'atteint $DOMINANCE_PERCENT%."
            )
        }

        val pattern = when (dominantCount) {
            weak -> LongitudinalRadioPattern.RADIO_DOMINANT
            congestion -> LongitudinalRadioPattern.CONGESTION_DOMINANT
            else -> LongitudinalRadioPattern.SUSPENSION_DOMINANT
        }
        val label = when (pattern) {
            LongitudinalRadioPattern.RADIO_DOMINANT -> "signal Wi-Fi faible"
            LongitudinalRadioPattern.CONGESTION_DOMINANT -> "congestion suspectée"
            LongitudinalRadioPattern.SUSPENSION_DOMINANT -> "suspension réseau Android"
            else -> "cause mixte"
        }
        return LongitudinalRadioAssessment(
            pattern,
            total,
            dominantCount,
            dominancePercent,
            "Tendance longitudinale dominante: $label ($dominantCount/$total, $dominancePercent%)."
        )
    }

    private fun Int?.orZero(): Int = (this ?: 0).coerceAtLeast(0)
}
