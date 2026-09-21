package com.terminator364.kinlink.core

enum class MobileAssistEvidenceTrend {
    INSUFFICIENT,
    SUSTAINED_BETTER,
    RELAPSING,
    NO_CONFIRMED_BENEFIT,
    MIXED
}

data class MobileAssistEvidenceSummary(
    val trend: MobileAssistEvidenceTrend,
    val label: String,
    val evaluatedActions: Int
)

object MobileAssistEvidenceSummaryPolicy {
    fun summarize(counts: Map<String, Int>): MobileAssistEvidenceSummary {
        val sustained = counts["SUSTAINED_BETTER"] ?: 0
        val earlyRelapse = counts["RELAPSED"] ?: 0
        val lateRelapse = counts["RELAPSED_AFTER_SUSTAINED"] ?: 0
        val noBetter = counts["NO_BETTER"] ?: 0
        val metricsOnly = counts["METRICS_AVAILABLE"] ?: 0
        val inconclusive = counts["INCONCLUSIVE"] ?: 0

        val relapses = earlyRelapse + lateRelapse

        // A late relapse is a later state of a previously recorded
        // SUSTAINED_BETTER event, not a second independent action.
        val sustainedStillHolding = (sustained - lateRelapse).coerceAtLeast(0)
        val evaluated =
            sustainedStillHolding +
                relapses +
                noBetter +
                metricsOnly +
                inconclusive

        if (evaluated == 0) {
            return MobileAssistEvidenceSummary(
                MobileAssistEvidenceTrend.INSUFFICIENT,
                "Preuve 24 h · en collecte",
                0
            )
        }

        if (sustainedStillHolding > 0 && relapses == 0 && noBetter == 0) {
            return MobileAssistEvidenceSummary(
                MobileAssistEvidenceTrend.SUSTAINED_BETTER,
                "Preuve 24 h · mieux durable corrélé ×$sustainedStillHolding",
                evaluated
            )
        }

        if (relapses > 0 && sustainedStillHolding == 0 && noBetter == 0) {
            return MobileAssistEvidenceSummary(
                MobileAssistEvidenceTrend.RELAPSING,
                "Preuve 24 h · amélioration transitoire / rechute ×$relapses",
                evaluated
            )
        }

        if (noBetter > 0 && sustainedStillHolding == 0 && relapses == 0) {
            return MobileAssistEvidenceSummary(
                MobileAssistEvidenceTrend.NO_CONFIRMED_BENEFIT,
                "Preuve 24 h · aucun mieux confirmé ×$noBetter",
                evaluated
            )
        }

        return MobileAssistEvidenceSummary(
            MobileAssistEvidenceTrend.MIXED,
            "Preuve 24 h · mieux=$sustainedStillHolding · rechute=$relapses · sans mieux=$noBetter",
            evaluated
        )
    }
}
