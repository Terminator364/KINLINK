package com.terminator364.kinlink.core

import android.content.Context

data class StoredNetworkStatsUsageEvidence(
    val usedBytes: Long,
    val cycleStartAtEpochMillis: Long,
    val observedAtEpochMillis: Long,
    val confidencePercent: Int,
    val adapterVersion: Int
)

data class StoredMobileUsageEvidence(
    val userReconciledUsedBytes: Long?,
    val userReconciledObservedAtEpochMillis: Long?,
    val aggregateCounterState: CounterSegmentState?,
    val networkStatsEvidence: StoredNetworkStatsUsageEvidence?,
    val schemaVersion: Int
)

object MobileUsageEvidenceStoragePolicy {
    const val SCHEMA_VERSION = 2

    fun validUserEvidence(bytes: Long?, observedAtEpochMillis: Long?): Boolean =
        (bytes == null && observedAtEpochMillis == null) ||
            (
                bytes != null &&
                    bytes >= 0L &&
                    observedAtEpochMillis != null &&
                    observedAtEpochMillis > 0L
                )

    fun validNetworkStatsEvidence(
        evidence: StoredNetworkStatsUsageEvidence?
    ): Boolean =
        evidence == null ||
            (
                evidence.usedBytes >= 0L &&
                    evidence.cycleStartAtEpochMillis > 0L &&
                    evidence.observedAtEpochMillis > evidence.cycleStartAtEpochMillis &&
                    evidence.confidencePercent in 0..100 &&
                    evidence.adapterVersion > 0
                )
}

class MobileUsageEvidenceStore(context: Context) {
    private val prefs =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun read(): StoredMobileUsageEvidence? {
        val schema = prefs.getInt(KEY_SCHEMA, LEGACY_SCHEMA_VERSION)
        if (schema !in LEGACY_SCHEMA_VERSION..MobileUsageEvidenceStoragePolicy.SCHEMA_VERSION) {
            return null
        }

        val hasUser = prefs.getBoolean(KEY_HAS_USER, false)
        val userBytes = if (hasUser) prefs.getLong(KEY_USER_BYTES, -1L) else null
        val userAt = if (hasUser) prefs.getLong(KEY_USER_AT, -1L) else null
        if (!MobileUsageEvidenceStoragePolicy.validUserEvidence(userBytes, userAt)) {
            return null
        }

        val hasCounter = prefs.getBoolean(KEY_HAS_COUNTER, false)
        val counter = if (hasCounter) {
            val state = CounterSegmentState(
                lastRawTotalBytes = prefs.getLong(KEY_COUNTER_RAW, -1L),
                provenCycleBytes = prefs.getLong(KEY_COUNTER_PROVEN, -1L),
                generation = prefs.getInt(KEY_COUNTER_GENERATION, -1)
            )
            if (
                state.lastRawTotalBytes < 0L ||
                state.provenCycleBytes < 0L ||
                state.generation < 0
            ) return null
            state
        } else null

        val networkStats =
            if (schema >= 2 && prefs.getBoolean(KEY_HAS_NETWORK_STATS, false)) {
                StoredNetworkStatsUsageEvidence(
                    usedBytes = prefs.getLong(KEY_NETWORK_STATS_BYTES, -1L),
                    cycleStartAtEpochMillis =
                        prefs.getLong(KEY_NETWORK_STATS_CYCLE_START, -1L),
                    observedAtEpochMillis =
                        prefs.getLong(KEY_NETWORK_STATS_OBSERVED_AT, -1L),
                    confidencePercent =
                        prefs.getInt(KEY_NETWORK_STATS_CONFIDENCE, -1),
                    adapterVersion =
                        prefs.getInt(KEY_NETWORK_STATS_ADAPTER_VERSION, -1)
                )
            } else null

        if (!MobileUsageEvidenceStoragePolicy.validNetworkStatsEvidence(networkStats)) {
            return null
        }

        return StoredMobileUsageEvidence(
            userReconciledUsedBytes = userBytes,
            userReconciledObservedAtEpochMillis = userAt,
            aggregateCounterState = counter,
            networkStatsEvidence = networkStats,
            schemaVersion = schema
        )
    }

    fun writeUserReconciled(
        usedBytes: Long,
        observedAtEpochMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (!MobileUsageEvidenceStoragePolicy.validUserEvidence(
                usedBytes,
                observedAtEpochMillis
            )
        ) return false
        return prefs.edit()
            .putInt(KEY_SCHEMA, MobileUsageEvidenceStoragePolicy.SCHEMA_VERSION)
            .putBoolean(KEY_HAS_USER, true)
            .putLong(KEY_USER_BYTES, usedBytes)
            .putLong(KEY_USER_AT, observedAtEpochMillis)
            .commit()
    }

    fun clearUserReconciled(): Boolean =
        prefs.edit()
            .putInt(KEY_SCHEMA, MobileUsageEvidenceStoragePolicy.SCHEMA_VERSION)
            .remove(KEY_HAS_USER)
            .remove(KEY_USER_BYTES)
            .remove(KEY_USER_AT)
            .commit()

    fun writeNetworkStats(
        evidence: NetworkStatsMobileUsageEvidence
    ): Boolean {
        val observation =
            NetworkStatsMobileUsagePolicy.toObservation(evidence) ?: return false
        val cycleStart = evidence.cycleStartAtEpochMillis ?: return false
        val stored = StoredNetworkStatsUsageEvidence(
            usedBytes = observation.usedBytes,
            cycleStartAtEpochMillis = cycleStart,
            observedAtEpochMillis = observation.observedAtEpochMillis,
            confidencePercent = observation.confidencePercent,
            adapterVersion = evidence.adapterVersion
        )
        if (!MobileUsageEvidenceStoragePolicy.validNetworkStatsEvidence(stored)) {
            return false
        }
        return prefs.edit()
            .putInt(KEY_SCHEMA, MobileUsageEvidenceStoragePolicy.SCHEMA_VERSION)
            .putBoolean(KEY_HAS_NETWORK_STATS, true)
            .putLong(KEY_NETWORK_STATS_BYTES, stored.usedBytes)
            .putLong(KEY_NETWORK_STATS_CYCLE_START, stored.cycleStartAtEpochMillis)
            .putLong(KEY_NETWORK_STATS_OBSERVED_AT, stored.observedAtEpochMillis)
            .putInt(KEY_NETWORK_STATS_CONFIDENCE, stored.confidencePercent)
            .putInt(KEY_NETWORK_STATS_ADAPTER_VERSION, stored.adapterVersion)
            .commit()
    }

    fun clearNetworkStats(): Boolean =
        prefs.edit()
            .putInt(KEY_SCHEMA, MobileUsageEvidenceStoragePolicy.SCHEMA_VERSION)
            .remove(KEY_HAS_NETWORK_STATS)
            .remove(KEY_NETWORK_STATS_BYTES)
            .remove(KEY_NETWORK_STATS_CYCLE_START)
            .remove(KEY_NETWORK_STATS_OBSERVED_AT)
            .remove(KEY_NETWORK_STATS_CONFIDENCE)
            .remove(KEY_NETWORK_STATS_ADAPTER_VERSION)
            .commit()

    fun advanceAggregateCounter(rawTotalBytes: Long): CounterSegmentAdvance? {
        val previous = read()?.aggregateCounterState
        val advanced =
            MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
                previous,
                rawTotalBytes
            ) ?: return null
        val ok = prefs.edit()
            .putInt(KEY_SCHEMA, MobileUsageEvidenceStoragePolicy.SCHEMA_VERSION)
            .putBoolean(KEY_HAS_COUNTER, true)
            .putLong(KEY_COUNTER_RAW, advanced.state.lastRawTotalBytes)
            .putLong(KEY_COUNTER_PROVEN, advanced.state.provenCycleBytes)
            .putInt(KEY_COUNTER_GENERATION, advanced.state.generation)
            .commit()
        return advanced.takeIf { ok }
    }

    companion object {
        private const val LEGACY_SCHEMA_VERSION = 1
        private const val PREFS = "kinlink_mobile_usage_evidence_v1"
        private const val KEY_SCHEMA = "schema_version"
        private const val KEY_HAS_USER = "has_user_reconciled"
        private const val KEY_USER_BYTES = "user_used_bytes"
        private const val KEY_USER_AT = "user_observed_at_epoch_ms"
        private const val KEY_HAS_COUNTER = "has_aggregate_counter"
        private const val KEY_COUNTER_RAW = "aggregate_counter_raw"
        private const val KEY_COUNTER_PROVEN = "aggregate_counter_proven"
        private const val KEY_COUNTER_GENERATION = "aggregate_counter_generation"
        private const val KEY_HAS_NETWORK_STATS = "has_network_stats"
        private const val KEY_NETWORK_STATS_BYTES = "network_stats_bytes"
        private const val KEY_NETWORK_STATS_CYCLE_START = "network_stats_cycle_start"
        private const val KEY_NETWORK_STATS_OBSERVED_AT = "network_stats_observed_at"
        private const val KEY_NETWORK_STATS_CONFIDENCE = "network_stats_confidence"
        private const val KEY_NETWORK_STATS_ADAPTER_VERSION = "network_stats_adapter_version"
    }
}
