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
    val baselineScore: Int,
    val currentScore: Int,
    val elapsedMillis: Long,
    val summary: String
)

/**
 * Zero-wakeup follow-up for Mobile Assist.
 *
 * It observes only NetworkCallback-driven passive Android state. Better quality
 * after a bandwidth-metric refresh is correlation, never claimed as proof that
 * KINLINK changed radio throughput.
 */
class MobileAssistEvidenceTracker {
    private data class Active(
        val baseline: PassiveLinkQuality,
        val baselineScore: Int,
        val startedAt: Long,
        val baselineWasUnknown: Boolean,
        var firstBetterAt: Long? = null
    )

    private data class SustainedWatch(
        val originalBaseline: PassiveLinkQuality,
        val originalBaselineScore: Int,
        val confirmedQuality: PassiveLinkQuality,
        val confirmedScore: Int,
        val confirmedAt: Long
    )

    private var active: Active? = null
    private var sustainedWatch: SustainedWatch? = null

    @Synchronized
    fun start(truth: NetworkTruth) {
        val quality = PassiveLinkQualityPolicy.assess(truth).quality
        startBaseline(
            quality = quality,
            score = PassiveQualityScorePolicy.score(truth).score,
            observedAtMillis = truth.observedAtMillis
        )
    }

    @Synchronized
    fun startBaseline(
        quality: PassiveLinkQuality,
        score: Int,
        observedAtMillis: Long
    ) {
        sustainedWatch = null
        active = Active(
            baseline = quality,
            baselineScore = score.coerceIn(0, 100),
            startedAt = observedAtMillis,
            baselineWasUnknown = quality == PassiveLinkQuality.UNKNOWN
        )
    }

    @Synchronized
    fun observe(truth: NetworkTruth): MobileAssistEvidence? {
        val currentQuality = PassiveLinkQualityPolicy.assess(truth).quality
        val currentScore = PassiveQualityScorePolicy.score(truth).score

        sustainedWatch?.let { watch ->
            val age = truth.observedAtMillis - watch.confirmedAt
            if (age < 0L || age > RELAPSE_MONITOR_WINDOW_MS) {
                sustainedWatch = null
            } else if (
                truth.transport == Transport.CELLULAR &&
                truth.internetState == InternetState.VALIDATED &&
                currentScore <= watch.originalBaselineScore + RELAPSE_TOLERANCE_POINTS
            ) {
                sustainedWatch = null
                return MobileAssistEvidence(
                    MobileAssistEvidenceResult.RELAPSED_AFTER_SUSTAINED,
                    watch.originalBaseline,
                    currentQuality,
                    watch.originalBaselineScore,
                    currentScore,
                    age,
                    "La qualité était restée meilleure puis est retombée près du niveau de départ; KINLINK peut réévaluer après cooldown."
                )
            }
        }

        val state = active ?: return null
        val elapsed = truth.observedAtMillis - state.startedAt

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
                currentQuality,
                state.baselineScore,
                currentScore,
                elapsed.coerceAtLeast(0L),
                "Preuve Mobile Assist inconclusive : fenêtre dépassée ou contexte mobile changé."
            )
        }

        if (state.baselineWasUnknown && currentQuality != PassiveLinkQuality.UNKNOWN) {
            active = null
            return MobileAssistEvidence(
                MobileAssistEvidenceResult.METRICS_AVAILABLE,
                state.baseline,
                currentQuality,
                state.baselineScore,
                currentScore,
                elapsed,
                "Android expose maintenant une estimation de capacité. Cela prouve un rafraîchissement de métriques, pas une accélération du réseau."
            )
        }

        val better = currentScore >= state.baselineScore + MIN_SCORE_DELTA
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
                    originalBaselineScore = state.baselineScore,
                    confirmedQuality = currentQuality,
                    confirmedScore = currentScore,
                    confirmedAt = truth.observedAtMillis
                )
                return MobileAssistEvidence(
                    MobileAssistEvidenceResult.SUSTAINED_BETTER,
                    state.baseline,
                    currentQuality,
                    state.baselineScore,
                    currentScore,
                    elapsed,
                    "Indice passif resté meilleur pendant la fenêtre de confirmation; corrélation observée, causalité non affirmée."
                )
            }
            return null
        }

        if (state.firstBetterAt != null) {
            active = null
            return MobileAssistEvidence(
                MobileAssistEvidenceResult.RELAPSED,
                state.baseline,
                currentQuality,
                state.baselineScore,
                currentScore,
                elapsed,
                "Amélioration passive transitoire puis rechute avant confirmation."
            )
        }

        if (elapsed >= NO_BENEFIT_AFTER_MS) {
            active = null
            return MobileAssistEvidence(
                MobileAssistEvidenceResult.NO_BETTER,
                state.baseline,
                currentQuality,
                state.baselineScore,
                currentScore,
                elapsed,
                "Aucune amélioration passive significative observée dans la fenêtre de preuve."
            )
        }

        return null
    }

    companion object {
        const val MIN_SCORE_DELTA = 12
        const val RELAPSE_TOLERANCE_POINTS = 4
        const val MIN_SUSTAINED_BETTER_MS = 20_000L
        const val NO_BENEFIT_AFTER_MS = 60_000L
        const val MAX_EVIDENCE_WINDOW_MS = 180_000L
        const val RELAPSE_MONITOR_WINDOW_MS = 10L * 60L * 1000L
    }
}
