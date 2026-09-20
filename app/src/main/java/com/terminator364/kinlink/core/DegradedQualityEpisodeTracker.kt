package com.terminator364.kinlink.core

enum class DegradedQualitySeverity {
    LIMITED,
    CONSTRAINED
}

data class DegradedQualityEpisode(
    val severity: DegradedQualitySeverity,
    val durationMillis: Long,
    val summary: String
)

/**
 * Measures slow-but-validated Wi-Fi episodes without generating traffic.
 */
class DegradedQualityEpisodeTracker {
    private var startedAt: Long? = null
    private var worstSeverity: DegradedQualitySeverity? = null

    fun observe(truth: NetworkTruth): DegradedQualityEpisode? {
        val quality = PassiveLinkQualityPolicy.assess(truth).quality
        val degraded = truth.transport == Transport.WIFI &&
            truth.internetState == InternetState.VALIDATED &&
            (quality == PassiveLinkQuality.LIMITED || quality == PassiveLinkQuality.CONSTRAINED)

        if (degraded) {
            if (startedAt == null) startedAt = truth.observedAtMillis
            val currentSeverity = if (quality == PassiveLinkQuality.CONSTRAINED) {
                DegradedQualitySeverity.CONSTRAINED
            } else {
                DegradedQualitySeverity.LIMITED
            }
            worstSeverity = when {
                worstSeverity == DegradedQualitySeverity.CONSTRAINED -> DegradedQualitySeverity.CONSTRAINED
                currentSeverity == DegradedQualitySeverity.CONSTRAINED -> DegradedQualitySeverity.CONSTRAINED
                else -> DegradedQualitySeverity.LIMITED
            }
            return null
        }

        val start = startedAt ?: return null
        val severity = worstSeverity ?: DegradedQualitySeverity.LIMITED
        val duration = (truth.observedAtMillis - start).coerceAtLeast(0L)
        startedAt = null
        worstSeverity = null
        return DegradedQualityEpisode(
            severity = severity,
            durationMillis = duration,
            summary = "Épisode Wi-Fi lent terminé après ${duration} ms; sévérité max=${severity.name}."
        )
    }
}
