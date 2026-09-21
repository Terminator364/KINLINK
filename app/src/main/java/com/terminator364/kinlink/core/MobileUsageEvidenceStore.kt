package com.terminator364.kinlink.core

import android.content.Context

data class StoredMobileUsageEvidence(
    val userReconciledUsedBytes: Long?,
    val userReconciledObservedAtEpochMillis: Long?,
    val aggregateCounterState: CounterSegmentState?,
    val schemaVersion: Int
)

object MobileUsageEvidenceStoragePolicy {
    const val SCHEMA_VERSION = 1

    fun validUserEvidence(bytes: Long?, observedAtEpochMillis: Long?): Boolean =
        (bytes == null && observedAtEpochMillis == null) ||
            (
                bytes != null &&
                    bytes >= 0L &&
                    observedAtEpochMillis != null &&
                    observedAtEpochMillis > 0L
                )
}

class MobileUsageEvidenceStore(context: Context) {
    private val prefs =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun read(): StoredMobileUsageEvidence? {
        val schema = prefs.getInt(KEY_SCHEMA, SCHEMA_VERSION)
        if (schema != SCHEMA_VERSION) return null

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

        return StoredMobileUsageEvidence(
            userReconciledUsedBytes = userBytes,
            userReconciledObservedAtEpochMillis = userAt,
            aggregateCounterState = counter,
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
            .putInt(KEY_SCHEMA, SCHEMA_VERSION)
            .putBoolean(KEY_HAS_USER, true)
            .putLong(KEY_USER_BYTES, usedBytes)
            .putLong(KEY_USER_AT, observedAtEpochMillis)
            .commit()
    }

    fun clearUserReconciled(): Boolean =
        prefs.edit()
            .putInt(KEY_SCHEMA, SCHEMA_VERSION)
            .remove(KEY_HAS_USER)
            .remove(KEY_USER_BYTES)
            .remove(KEY_USER_AT)
            .commit()

    fun advanceAggregateCounter(rawTotalBytes: Long): CounterSegmentAdvance? {
        val previous = read()?.aggregateCounterState
        val advanced =
            MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
                previous,
                rawTotalBytes
            ) ?: return null
        val ok = prefs.edit()
            .putInt(KEY_SCHEMA, SCHEMA_VERSION)
            .putBoolean(KEY_HAS_COUNTER, true)
            .putLong(KEY_COUNTER_RAW, advanced.state.lastRawTotalBytes)
            .putLong(KEY_COUNTER_PROVEN, advanced.state.provenCycleBytes)
            .putInt(KEY_COUNTER_GENERATION, advanced.state.generation)
            .commit()
        return advanced.takeIf { ok }
    }

    companion object {
        private const val PREFS = "kinlink_mobile_usage_evidence_v1"
        private const val KEY_SCHEMA = "schema_version"
        private const val KEY_HAS_USER = "has_user_reconciled"
        private const val KEY_USER_BYTES = "user_used_bytes"
        private const val KEY_USER_AT = "user_observed_at_epoch_ms"
        private const val KEY_HAS_COUNTER = "has_aggregate_counter"
        private const val KEY_COUNTER_RAW = "aggregate_counter_raw"
        private const val KEY_COUNTER_PROVEN = "aggregate_counter_proven"
        private const val KEY_COUNTER_GENERATION = "aggregate_counter_generation"
    }
}
