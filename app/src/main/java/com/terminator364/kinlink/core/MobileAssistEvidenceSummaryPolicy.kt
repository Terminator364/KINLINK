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
        val evaluated =
            sustained + relapses + noBetter + metricsOnly + inconclusive

        if (evaluated == 0) {
            return MobileAssistEvidenceSummary(
                MobileAssistEvidenceTrend.INSUFFICIENT,
                "Preuve 24 h · en collecte",
                0
            )
        }

        if (sustained > 0 && relapses == 0 && noBetter == 0) {
            return MobileAssistEvidenceSummary(
                MobileAssistEvidenceTrend.SUSTAINED_BETTER,
                "Preuve 24 h · mieux durable corrélé ×$sustained",
                evaluated
            )
        }

        if (relapses > 0 && sustained == 0 && noBetter == 0) {
            return MobileAssistEvidenceSummary(
                MobileAssistEvidenceTrend.RELAPSING,
                "Preuve 24 h · amélioration transitoire / rechute ×$relapses",
                evaluated
            )
        }

        if (noBetter > 0 && sustained == 0 && relapses == 0) {
            return MobileAssistEvidenceSummary(
                MobileAssistEvidenceTrend.NO_CONFIRMED_BENEFIT,
                "Preuve 24 h · aucun mieux confirmé ×$noBetter",
                evaluated
            )
        }

        return MobileAssistEvidenceSummary(
            MobileAssistEvidenceTrend.MIXED,
            "Preuve 24 h · mieux=$sustained · rechute=$relapses · sans mieux=$noBetter",
            evaluated
        )
    }
}
