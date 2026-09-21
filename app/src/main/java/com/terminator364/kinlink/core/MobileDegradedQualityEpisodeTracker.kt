package com.terminator364.kinlink.core

data class MobileDegradedQualityEpisode(
    val severity: DegradedQualitySeverity,
    val durationMillis: Long,
    val summary: String
)

/**
 * Measures slow-but-validated cellular episodes from Android passive bandwidth
 * estimates only. No timer, speed test, DNS query or HTTP request is created.
 */
class MobileDegradedQualityEpisodeTracker {
    private var startedAt: Long? = null
    private var worstSeverity: DegradedQualitySeverity? = null

    fun observe(truth: NetworkTruth): MobileDegradedQualityEpisode? {
        val quality = PassiveLinkQualityPolicy.assess(truth).quality
        val degraded =
            truth.transport == Transport.CELLULAR &&
                truth.internetState == InternetState.VALIDATED &&
                (
                    quality == PassiveLinkQuality.LIMITED ||
                        quality == PassiveLinkQuality.CONSTRAINED
                )

        if (degraded) {
            if (startedAt == null) startedAt = truth.observedAtMillis
            val currentSeverity =
                if (quality == PassiveLinkQuality.CONSTRAINED)
                    DegradedQualitySeverity.CONSTRAINED
                else
                    DegradedQualitySeverity.LIMITED
            worstSeverity = when {
                worstSeverity == DegradedQualitySeverity.CONSTRAINED ->
                    DegradedQualitySeverity.CONSTRAINED
                currentSeverity == DegradedQualitySeverity.CONSTRAINED ->
                    DegradedQualitySeverity.CONSTRAINED
                else -> DegradedQualitySeverity.LIMITED
            }
            return null
        }

        val start = startedAt ?: return null
        val severity = worstSeverity ?: DegradedQualitySeverity.LIMITED
        val duration = (truth.observedAtMillis - start).coerceAtLeast(0L)
        startedAt = null
        worstSeverity = null
        return MobileDegradedQualityEpisode(
            severity = severity,
            durationMillis = duration,
            summary = "Épisode mobile lent terminé après ${duration} ms; sévérité max=${severity.name}."
        )
    }
}
