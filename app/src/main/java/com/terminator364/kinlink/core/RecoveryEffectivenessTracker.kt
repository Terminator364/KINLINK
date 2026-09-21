package com.terminator364.kinlink.core

enum class RecoveryEffectiveness {
    IMPROVED,
    UNCHANGED,
    DEGRADED,
    INCONCLUSIVE
}

data class RecoveryEffectivenessEvidence(
    val result: RecoveryEffectiveness,
    val baseline: PassiveLinkQuality,
    val current: PassiveLinkQuality,
    val summary: String
)

class RecoveryEffectivenessTracker(
    private val expectedTransport: Transport = Transport.WIFI
) {
    private var baseline: PassiveLinkQuality? = null
    private var baselineStartedAtMillis: Long? = null
    private var remainingObservations = 0

    fun start(truth: NetworkTruth) {
        baseline = PassiveLinkQualityPolicy.assess(truth).quality
        baselineStartedAtMillis = truth.observedAtMillis
        remainingObservations = 2
    }

    fun observe(truth: NetworkTruth): RecoveryEffectivenessEvidence? {
        val start = baseline ?: return null
        val startedAt = baselineStartedAtMillis
        val ageMillis = startedAt?.let { truth.observedAtMillis - it }
        if (
            startedAt == null ||
            ageMillis == null ||
            ageMillis < 0L ||
            ageMillis > MAX_EFFECT_WINDOW_MS
        ) {
            clear()
            return RecoveryEffectivenessEvidence(
                RecoveryEffectiveness.INCONCLUSIVE,
                start,
                PassiveLinkQualityPolicy.assess(truth).quality,
                "Résultat inconclusif : fenêtre de preuve dépassée ou horloge incohérente."
            )
        }

        if (truth.transport != expectedTransport || truth.internetState != InternetState.VALIDATED) {
            clear()
            return RecoveryEffectivenessEvidence(
                RecoveryEffectiveness.INCONCLUSIVE,
                start,
                PassiveLinkQualityPolicy.assess(truth).quality,
                "Résultat inconclusif : le contexte ${expectedTransport.name} validé a changé après l’action."
            )
        }

        remainingObservations -= 1
        if (remainingObservations > 0) return null

        val current = PassiveLinkQualityPolicy.assess(truth).quality
        val result = compare(start, current)
        clear()

        val summary = when (result) {
            RecoveryEffectiveness.IMPROVED -> "Qualité passive améliorée après l’action."
            RecoveryEffectiveness.UNCHANGED -> "Qualité passive inchangée après l’action."
            RecoveryEffectiveness.DEGRADED -> "Qualité passive dégradée après l’action."
            RecoveryEffectiveness.INCONCLUSIVE -> "Résultat inconclusif : qualité passive non comparable."
        }
        return RecoveryEffectivenessEvidence(result, start, current, summary)
    }

    private fun compare(
        baseline: PassiveLinkQuality,
        current: PassiveLinkQuality
    ): RecoveryEffectiveness {
        val before = rank(baseline) ?: return RecoveryEffectiveness.INCONCLUSIVE
        val after = rank(current) ?: return RecoveryEffectiveness.INCONCLUSIVE
        return when {
            after > before -> RecoveryEffectiveness.IMPROVED
            after < before -> RecoveryEffectiveness.DEGRADED
            else -> RecoveryEffectiveness.UNCHANGED
        }
    }

    private fun rank(value: PassiveLinkQuality): Int? = when (value) {
        PassiveLinkQuality.CONSTRAINED -> 1
        PassiveLinkQuality.LIMITED -> 2
        PassiveLinkQuality.COMFORTABLE -> 3
        PassiveLinkQuality.UNKNOWN -> null
    }

    private fun clear() {
        baseline = null
        baselineStartedAtMillis = null
        remainingObservations = 0
    }

    companion object {
        const val MAX_EFFECT_WINDOW_MS = 2L * 60L * 1000L
    }
}
