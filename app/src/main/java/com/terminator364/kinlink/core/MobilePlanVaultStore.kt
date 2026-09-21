package com.terminator364.kinlink.core

import android.content.Context

data class StoredMobilePlan(
    val config: MobilePlanConfig,
    val updatedAtEpochMillis: Long,
    val schemaVersion: Int
)

object MobilePlanStoragePolicy {
    const val SCHEMA_VERSION = 1

    fun validate(config: MobilePlanConfig): Boolean =
        MobilePlanVaultPolicy.valid(config)

    fun shouldAcceptExpiry(
        expiryAtEpochMillis: Long?,
        nowEpochMillis: Long
    ): Boolean =
        expiryAtEpochMillis == null || expiryAtEpochMillis > 0L

    fun decimalBytes(megabytes: Long): Long? {
        if (megabytes < 0L) return null
        if (megabytes > Long.MAX_VALUE / 1_000_000L) return null
        return megabytes * 1_000_000L
    }

    fun decimalMegabytes(bytes: Long): Long =
        bytes.coerceAtLeast(0L) / 1_000_000L
}

/**
 * Versioned, local-only Mobile Vault plan configuration.
 *
 * This store deliberately does not migrate the legacy daily caution limit:
 * a daily threshold is not equivalent to a carrier plan/cycle budget.
 * No carrier identifier, phone number, SIM identifier or secret is persisted.
 */
class MobilePlanVaultStore(context: Context) {
    private val prefs =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun read(): StoredMobilePlan? {
        if (!prefs.getBoolean(KEY_CONFIGURED, false)) return null
        val schema = prefs.getInt(KEY_SCHEMA, -1)
        if (schema != MobilePlanStoragePolicy.SCHEMA_VERSION) return null

        val total = prefs.getLong(KEY_TOTAL_BYTES, -1L)
        val reserve = prefs.getLong(KEY_PROTECTED_RESERVE_BYTES, -1L)
        val rescue = prefs.getLong(KEY_RESCUE_BYTES, -1L)
        val critical = prefs.getLong(KEY_CRITICAL_BYTES, -1L)
        val expiryRaw = prefs.getLong(KEY_EXPIRY_EPOCH_MS, NO_EXPIRY)
        val updated = prefs.getLong(KEY_UPDATED_AT_EPOCH_MS, -1L)

        val config = MobilePlanConfig(
            totalBytes = total,
            expiryAtEpochMillis = expiryRaw.takeUnless { it == NO_EXPIRY },
            protectedReserveBytes = reserve,
            rescueAllowanceBytes = rescue,
            criticalInteractiveAllowanceBytes = critical
        )
        if (!MobilePlanStoragePolicy.validate(config)) return null
        if (!MobilePlanStoragePolicy.shouldAcceptExpiry(
                config.expiryAtEpochMillis,
                updated.coerceAtLeast(0L)
            )
        ) return null
        if (updated < 0L) return null

        return StoredMobilePlan(
            config = config,
            updatedAtEpochMillis = updated,
            schemaVersion = schema
        )
    }

    fun write(
        config: MobilePlanConfig,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (!MobilePlanStoragePolicy.validate(config)) return false
        if (!MobilePlanStoragePolicy.shouldAcceptExpiry(
                config.expiryAtEpochMillis,
                nowEpochMillis
            )
        ) return false
        if (nowEpochMillis < 0L) return false

        return prefs.edit()
            .putBoolean(KEY_CONFIGURED, true)
            .putInt(KEY_SCHEMA, MobilePlanStoragePolicy.SCHEMA_VERSION)
            .putLong(KEY_TOTAL_BYTES, config.totalBytes)
            .putLong(
                KEY_EXPIRY_EPOCH_MS,
                config.expiryAtEpochMillis ?: NO_EXPIRY
            )
            .putLong(
                KEY_PROTECTED_RESERVE_BYTES,
                config.protectedReserveBytes
            )
            .putLong(KEY_RESCUE_BYTES, config.rescueAllowanceBytes)
            .putLong(
                KEY_CRITICAL_BYTES,
                config.criticalInteractiveAllowanceBytes
            )
            .putLong(KEY_UPDATED_AT_EPOCH_MS, nowEpochMillis)
            .commit()
    }

    fun clear(): Boolean = prefs.edit().clear().commit()

    companion object {
        private const val PREFS = "kinlink_mobile_plan_vault_v1"
        private const val KEY_CONFIGURED = "configured"
        private const val KEY_SCHEMA = "schema_version"
        private const val KEY_TOTAL_BYTES = "total_bytes"
        private const val KEY_EXPIRY_EPOCH_MS = "expiry_epoch_ms"
        private const val KEY_PROTECTED_RESERVE_BYTES =
            "protected_reserve_bytes"
        private const val KEY_RESCUE_BYTES = "rescue_bytes"
        private const val KEY_CRITICAL_BYTES = "critical_bytes"
        private const val KEY_UPDATED_AT_EPOCH_MS = "updated_at_epoch_ms"
        private const val NO_EXPIRY = Long.MIN_VALUE
    }
}
