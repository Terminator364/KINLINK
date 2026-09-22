package com.terminator364.kinlink.core

enum class NetworkStatsMobileEvidenceStatus {
    AVAILABLE,
    MAIN_THREAD_BLOCKED,
    USAGE_ACCESS_REQUIRED,
    CYCLE_START_REQUIRED,
    INVALID_WINDOW,
    PLATFORM_REJECTED
}

data class NetworkStatsMobileUsageRequest(
    val cycleStartAtEpochMillis: Long?,
    val endAtEpochMillis: Long
)

data class NetworkStatsMobileUsageEvidence(
    val status: NetworkStatsMobileEvidenceStatus,
    val usedBytes: Long?,
    val cycleStartAtEpochMillis: Long?,
    val observedAtEpochMillis: Long,
    val confidencePercent: Int,
    val adapterVersion: Int = NetworkStatsMobileUsagePolicy.ADAPTER_VERSION,
    val reason: String
)

object NetworkStatsMobileUsagePolicy {
    const val ADAPTER_VERSION = 1

    fun validationStatus(request: NetworkStatsMobileUsageRequest): NetworkStatsMobileEvidenceStatus? {
        val start = request.cycleStartAtEpochMillis
            ?: return NetworkStatsMobileEvidenceStatus.CYCLE_START_REQUIRED
        if (start <= 0L || request.endAtEpochMillis <= 0L || request.endAtEpochMillis <= start) {
            return NetworkStatsMobileEvidenceStatus.INVALID_WINDOW
        }
        return null
    }

    fun safeTotalBytes(rxBytes: Long, txBytes: Long): Long? {
        if (rxBytes < 0L || txBytes < 0L) return null
        return if (Long.MAX_VALUE - rxBytes < txBytes) Long.MAX_VALUE else rxBytes + txBytes
    }

    fun toObservation(evidence: NetworkStatsMobileUsageEvidence): MobileUsageObservation? {
        val used = evidence.usedBytes ?: return null
        if (evidence.status != NetworkStatsMobileEvidenceStatus.AVAILABLE) return null
        if (used < 0L || evidence.observedAtEpochMillis <= 0L || evidence.confidencePercent !in 0..100) return null
        return MobileUsageObservation(
            usedBytes = used,
            source = MobilePlanUsageSource.NETWORK_STATS_OPTIONAL,
            attributionScope = MobileUsageAttributionScope.DEVICE_MOBILE_AGGREGATE,
            observedAtEpochMillis = evidence.observedAtEpochMillis,
            confidencePercent = evidence.confidencePercent
        )
    }
}
