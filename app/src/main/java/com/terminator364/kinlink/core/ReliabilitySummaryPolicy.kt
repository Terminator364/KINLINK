package com.terminator364.kinlink.core

object ReliabilitySummaryPolicy {
    fun label(
        interruptionCount: Int,
        cumulativeMillis: Long,
        longestMillis: Long,
        lowQualityEpisodeCount: Int,
        dominantCause: String?
    ): String {
        if (interruptionCount <= 0 && lowQualityEpisodeCount <= 0) {
            return "24 h · aucune coupure mesurée dans l’historique retenu"
        }

        val parts = mutableListOf<String>()
        if (interruptionCount > 0) {
            parts += "coupures=" + interruptionCount
            parts += "cumul=" + formatDuration(cumulativeMillis)
            parts += "max=" + formatDuration(longestMillis)
        }
        if (lowQualityEpisodeCount > 0) {
            parts += "Wi-Fi lent=" + lowQualityEpisodeCount + " épisode(s)"
        }
        dominantCause?.takeIf { it.isNotBlank() }?.let {
            parts += "cause dominante=" + it
        }
        return "24 h · " + parts.joinToString(" · ")
    }

    private fun formatDuration(ms: Long): String {
        val safe = ms.coerceAtLeast(0L)
        return when {
            safe < 1_000L -> safe.toString() + " ms"
            safe < 60_000L -> (safe / 1_000L).toString() + " s"
            else -> (safe / 60_000L).toString() + " min " + ((safe % 60_000L) / 1_000L).toString() + " s"
        }
    }
}
