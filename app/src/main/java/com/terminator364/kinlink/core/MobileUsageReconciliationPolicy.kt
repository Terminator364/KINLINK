package com.terminator364.kinlink.core

enum class MobileUsageAttributionScope {
    PLAN_EXACT,
    DEVICE_MOBILE_AGGREGATE,
    UNKNOWN
}

data class MobileUsageObservation(
    val usedBytes: Long,
    val source: MobilePlanUsageSource,
    val attributionScope: MobileUsageAttributionScope,
    val observedAtEpochMillis: Long,
    val confidencePercent: Int
)

enum class MobileUsageResolutionStatus {
    RESOLVED,
    HOLD_CONFLICT,
    HOLD_UNATTRIBUTED,
    UNKNOWN
}

data class MobileUsageResolution(
    val status: MobileUsageResolutionStatus,
    val usage: MobilePlanUsage?,
    val evidenceCount: Int,
    val reason: String
)

data class CounterSegmentState(
    val lastRawTotalBytes: Long,
    val provenCycleBytes: Long,
    val generation: Int
)

data class CounterSegmentAdvance(
    val state: CounterSegmentState,
    val resetDetected: Boolean,
    val addedBytes: Long
)

object MobileUsageReconciliationPolicy {
    private const val MIN_DISAGREEMENT_BYTES = 5_000_000L

    fun reconcile(
        observations: List<MobileUsageObservation>,
        nowEpochMillis: Long,
        maxAgeMillis: Long = 24L * 60L * 60L * 1000L
    ): MobileUsageResolution {
        val valid = observations.filter {
            it.usedBytes >= 0L &&
                it.observedAtEpochMillis > 0L &&
                it.observedAtEpochMillis <= nowEpochMillis &&
                nowEpochMillis - it.observedAtEpochMillis <= maxAgeMillis &&
                it.confidencePercent in 0..100
        }
        if (valid.isEmpty()) {
            return MobileUsageResolution(
                MobileUsageResolutionStatus.UNKNOWN,
                null,
                0,
                "Aucune preuve récente de consommation du cycle."
            )
        }

        val exact = valid.filter {
            it.attributionScope == MobileUsageAttributionScope.PLAN_EXACT
        }
        if (exact.isEmpty()) {
            val aggregate = valid.count {
                it.attributionScope == MobileUsageAttributionScope.DEVICE_MOBILE_AGGREGATE
            }
            return MobileUsageResolution(
                if (aggregate > 0) MobileUsageResolutionStatus.HOLD_UNATTRIBUTED
                else MobileUsageResolutionStatus.UNKNOWN,
                null,
                valid.size,
                if (aggregate > 0)
                    "Consommation mobile globale observée mais non attribuable au forfait; zone Mobile Vault maintenue UNKNOWN."
                else
                    "Preuves présentes mais attribution forfait insuffisante."
            )
        }

        val min = exact.minOf { it.usedBytes }
        val max = exact.maxOf { it.usedBytes }
        val tolerance = maxOf(
            MIN_DISAGREEMENT_BYTES,
            (max / 50L).coerceAtLeast(0L)
        )
        if (max - min > tolerance) {
            return MobileUsageResolution(
                MobileUsageResolutionStatus.HOLD_CONFLICT,
                null,
                exact.size,
                "Sources attribuées au forfait en conflit; aucune moyenne arbitraire."
            )
        }

        val chosen = exact.maxWithOrNull(
            compareBy<MobileUsageObservation>(
                { sourcePriority(it.source) },
                { it.confidencePercent },
                { it.observedAtEpochMillis }
            )
        ) ?: return MobileUsageResolution(
            MobileUsageResolutionStatus.UNKNOWN,
            null,
            exact.size,
            "Aucune source attribuée sélectionnable."
        )

        return MobileUsageResolution(
            MobileUsageResolutionStatus.RESOLVED,
            MobilePlanUsage(
                usedBytes = max,
                source = chosen.source,
                observedAtEpochMillis = chosen.observedAtEpochMillis
            ),
            exact.size,
            "Consommation du cycle réconciliée sans conflit; valeur conservatrice maximale retenue."
        )
    }

    fun advanceDeviceAggregateCounter(
        previous: CounterSegmentState?,
        rawTotalBytes: Long
    ): CounterSegmentAdvance? {
        if (rawTotalBytes < 0L) return null
        if (previous == null) {
            return CounterSegmentAdvance(
                CounterSegmentState(
                    lastRawTotalBytes = rawTotalBytes,
                    provenCycleBytes = 0L,
                    generation = 0
                ),
                resetDetected = false,
                addedBytes = 0L
            )
        }
        if (
            previous.lastRawTotalBytes < 0L ||
            previous.provenCycleBytes < 0L ||
            previous.generation < 0
        ) return null

        return if (rawTotalBytes >= previous.lastRawTotalBytes) {
            val delta = rawTotalBytes - previous.lastRawTotalBytes
            CounterSegmentAdvance(
                CounterSegmentState(
                    lastRawTotalBytes = rawTotalBytes,
                    provenCycleBytes = safeAdd(previous.provenCycleBytes, delta),
                    generation = previous.generation
                ),
                resetDetected = false,
                addedBytes = delta
            )
        } else {
            CounterSegmentAdvance(
                CounterSegmentState(
                    lastRawTotalBytes = rawTotalBytes,
                    provenCycleBytes = previous.provenCycleBytes,
                    generation = previous.generation + 1
                ),
                resetDetected = true,
                addedBytes = 0L
            )
        }
    }

    private fun sourcePriority(source: MobilePlanUsageSource): Int = when (source) {
        MobilePlanUsageSource.USER_RECONCILED -> 4
        MobilePlanUsageSource.CARRIER_ADAPTER -> 3
        MobilePlanUsageSource.NETWORK_STATS_OPTIONAL -> 2
        MobilePlanUsageSource.ANDROID_DEVICE_WIDE_COUNTER -> 1
        MobilePlanUsageSource.UNKNOWN -> 0
    }

    private fun safeAdd(a: Long, b: Long): Long =
        if (Long.MAX_VALUE - a < b) Long.MAX_VALUE else a + b
}
