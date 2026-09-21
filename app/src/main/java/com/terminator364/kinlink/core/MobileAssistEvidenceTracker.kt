package com.terminator364.kinlink.core

enum class MobileAssistEvidenceResult {
    METRICS_AVAILABLE,
    SUSTAINED_BETTER,
    RELAPSED,
    RELAPSED_AFTER_SUSTAINED,
    NO_BETTER,
    INCONCLUSIVE
}

data class MobileAssistEvidence(
    val result: MobileAssistEvidenceResult,
    val baseline: PassiveLinkQuality,
    val current: PassiveLinkQuality,
    val elapsedMillis: Long,
    val summary: String
)

/**
 * Zero-wakeup evidence tracker for Mobile Assist.
 *
 * It observes only NetworkCallback-driven passive Android bandwidth estimates.
 * A better estimate after requestBandwidthUpdate is correlation, never claimed
 * as proof that KINLINK changed radio throughput.
 */
class MobileAssistEvidenceTracker {
    private data class Active(
        val baseline: PassiveLinkQuality,
        val startedAt: Long,
        val baselineWasUnknown: Boolean,
        var firstBetterAt: Long? = null
    )

    private data class SustainedWatch(
        val originalBaseline: PassiveLinkQuality,
        val confirmedQuality: PassiveLinkQuality,
        val confirmedAt: Long
    )

    private var active: Active? = null
    private var sustainedWatch: SustainedWatch? = null

    fun start(truth: NetworkTruth) {
        sustainedWatch = null
        active = Active(
            baseline = PassiveLinkQualityPolicy.assess(truth).quality,
            startedAt = truth.observedAtMillis,
            baselineWasUnknown =
                PassiveLinkQualityPolicy.assess(truth).quality == PassiveLinkQuality.UNKNOWN
        )
    }

    fun observe(truth: NetworkTruth): MobileAssistEvidence? {
        sustainedWatch?.let { watch ->
            val age = truth.observedAtMillis - watch.confirmedAt
            if (age < 0L || age > RELAPSE_MONITOR_WINDOW_MS) {
                sustainedWatch = null
            } else if (
                truth.transport == Transport.CELLULAR &&
                truth.internetState == InternetState.VALIDATED &&
                rank(PassiveLinkQualityPolicy.assess(truth).quality) <=
                    rank(watch.originalBaseline)
            ) {
                val current = PassiveLinkQualityPolicy.assess(truth).quality
                sustainedWatch = null
                return MobileAssistEvidence(
                    MobileAssistEvidenceResult.RELAPSED_AFTER_SUSTAINED,
                    watch.originalBaseline,
                    current,
                    age,
                    "La qualité était restée meilleure puis est retombée au niveau de départ; KINLINK peut réévaluer après cooldown."
                )
            }
        }

        val state = active ?: return null
        val elapsed = truth.observedAtMillis - state.startedAt
        val current = PassiveLinkQualityPolicy.assess(truth).quality

        if (
            elapsed < 0L ||
            elapsed > MAX_EVIDENCE_WINDOW_MS ||
            truth.transport != Transport.CELLULAR ||
            truth.internetState != InternetState.VALIDATED
        ) {
            active = null
            return MobileAssistEvidence(
                MobileAssistEvidenceResult.INCONCLUSIVE,
                state.baseline,
                current,
                elapsed.coerceAtLeast(0L),
                "Preuve Mobile Assist inconclusive : fenêtre dépassée ou contexte mobile changé."
            )
        }

        if (state.baselineWasUnknown && current != PassiveLinkQuality.UNKNOWN) {
            active = null
            return MobileAssistEvidence(
                MobileAssistEvidenceResult.METRICS_AVAILABLE,
                state.baseline,
                current,
                elapsed,
                "Android expose maintenant une estimation de capacité. Cela prouve un rafraîchissement de métriques, pas une accélération du réseau."
            )
        }

        val better = rank(current) > rank(state.baseline)
        if (better) {
            val firstBetter = state.firstBetterAt
            if (firstBetter == null) {
                state.firstBetterAt = truth.observedAtMillis
                return null
            }
            if (truth.observedAtMillis - firstBetter >= MIN_SUSTAINED_BETTER_MS) {
                active = null
                sustainedWatch = SustainedWatch(
                    originalBaseline = state.baseline,
                    confirmedQuality = current,
                    confirmedAt = truth.observedAtMillis
                )
                return MobileAssistEvidence(
                    MobileAssistEvidenceResult.SUSTAINED_BETTER,
                    state.baseline,
                    current,
                    elapsed,
                    "Qualité passive restée meilleure pendant la fenêtre de confirmation; corrélation observée, causalité non affirmée."
                )
            }
            return null
        }

        if (state.firstBetterAt != null) {
            active = null
            return MobileAssistEvidence(
                MobileAssistEvidenceResult.RELAPSED,
                state.baseline,
                current,
                elapsed,
                "Amélioration passive transitoire puis rechute avant confirmation."
            )
        }

        if (elapsed >= NO_BENEFIT_AFTER_MS) {
            active = null
            return MobileAssistEvidence(
                MobileAssistEvidenceResult.NO_BETTER,
                state.baseline,
                current,
                elapsed,
                "Aucune amélioration passive observée dans la fenêtre de preuve."
            )
        }

        return null
    }

    private fun rank(q: PassiveLinkQuality): Int = when (q) {
        PassiveLinkQuality.UNKNOWN -> 0
        PassiveLinkQuality.CONSTRAINED -> 1
        PassiveLinkQuality.LIMITED -> 2
        PassiveLinkQuality.COMFORTABLE -> 3
    }

    companion object {
        const val MIN_SUSTAINED_BETTER_MS = 20_000L
        const val NO_BENEFIT_AFTER_MS = 60_000L
        const val MAX_EVIDENCE_WINDOW_MS = 180_000L
        const val RELAPSE_MONITOR_WINDOW_MS = 10L * 60L * 1000L
    }
}
