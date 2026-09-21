package com.terminator364.kinlink.core

import android.content.Context
import android.net.TrafficStats
import android.os.SystemClock
import java.time.LocalDate
import kotlin.math.max

data class MobileBudgetSnapshot(
    val supported: Boolean,
    val usedTodayBytes: Long,
    val dailyLimitBytes: Long?,
    val state: BudgetState,
    val counterResetDetected: Boolean = false
) {
    val usedTodayMiB: Double
        get() = usedTodayBytes.toDouble() / (1024.0 * 1024.0)

    val dailyLimitMiB: Long?
        get() = dailyLimitBytes?.div(1024L * 1024L)

    val usedTodayMB: Double
        get() = usedTodayBytes.toDouble() / 1_000_000.0

    val dailyLimitMB: Long?
        get() = dailyLimitBytes?.div(1_000_000L)
}

object MobileBudgetSamplingPolicy {
    const val MIN_SAMPLE_INTERVAL_MS = 15_000L

    fun shouldReuse(
        nowElapsedMillis: Long,
        lastSampleElapsedMillis: Long?,
        sameEpochDay: Boolean
    ): Boolean {
        if (!sameEpochDay || lastSampleElapsedMillis == null) return false
        val age = (nowElapsedMillis - lastSampleElapsedMillis).coerceAtLeast(0L)
        return age < MIN_SAMPLE_INTERVAL_MS
    }
}

object MobileBudgetPolicy {
    fun state(
        supported: Boolean,
        usedBytes: Long,
        limitBytes: Long?
    ): BudgetState {
        if (!supported || limitBytes == null || limitBytes <= 0L) {
            return BudgetState.BALANCE_UNKNOWN
        }

        val safeUsed = max(0L, usedBytes)
        return when {
            safeUsed >= limitBytes -> BudgetState.BUNDLE_EXHAUSTED
            safeUsed >= (limitBytes * 80L) / 100L -> BudgetState.BUNDLE_LOW
            else -> BudgetState.BUNDLE_OK
        }
    }
}

/**
 * Low-overhead device-wide mobile byte guard.
 *
 * Android TrafficStats counters are sampled only when KINLINK already receives a network
 * event or renders the cockpit. No polling loop is created. Counter rollback (for example
 * after reboot) resets the baseline without inventing traffic.
 */
class MobileBudgetTracker(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private var cachedSnapshot: MobileBudgetSnapshot? = null
    private var cachedEpochDay: Long? = null
    private var lastSampleElapsedMillis: Long? = null

    fun setDailyLimitMiB(limitMiB: Int?) {
        prefs.edit().apply {
            if (limitMiB == null || limitMiB <= 0) remove(KEY_LIMIT_BYTES)
            else putLong(KEY_LIMIT_BYTES, limitMiB.toLong() * 1024L * 1024L)
        }.apply()
        synchronized(SAMPLE_LOCK) {
            cachedSnapshot = null
            cachedEpochDay = null
            lastSampleElapsedMillis = null
        }
    }

    fun configuredDailyLimitMiB(): Long? {
        val bytes = prefs.getLong(KEY_LIMIT_BYTES, 0L)
        return if (bytes > 0L) bytes / (1024L * 1024L) else null
    }

    fun setDailyLimitMB(limitMB: Int?) {
        prefs.edit().apply {
            if (limitMB == null || limitMB <= 0) remove(KEY_LIMIT_BYTES)
            else putLong(KEY_LIMIT_BYTES, limitMB.toLong() * 1_000_000L)
        }.apply()
        synchronized(SAMPLE_LOCK) {
            cachedSnapshot = null
            cachedEpochDay = null
            lastSampleElapsedMillis = null
        }
    }

    fun configuredDailyLimitMB(): Long? {
        val bytes = prefs.getLong(KEY_LIMIT_BYTES, 0L)
        return if (bytes > 0L) bytes / 1_000_000L else null
    }

    fun sample(
        epochDay: Long = LocalDate.now().toEpochDay(),
        nowElapsedMillis: Long = SystemClock.elapsedRealtime(),
        force: Boolean = false
    ): MobileBudgetSnapshot = synchronized(SAMPLE_LOCK) {
        if (!force &&
            MobileBudgetSamplingPolicy.shouldReuse(
                nowElapsedMillis,
                lastSampleElapsedMillis,
                cachedEpochDay == epochDay
            )
        ) {
            cachedSnapshot?.let { return it }
        }

        val rx = TrafficStats.getMobileRxBytes()
        val tx = TrafficStats.getMobileTxBytes()
        val unsupported = TrafficStats.UNSUPPORTED.toLong()
        val supported = rx != unsupported && tx != unsupported

        if (!supported) {
            val snapshot = MobileBudgetSnapshot(
                supported = false,
                usedTodayBytes = 0L,
                dailyLimitBytes = configuredLimitBytes(),
                state = BudgetState.BALANCE_UNKNOWN
            )
            cachedSnapshot = snapshot
            cachedEpochDay = epochDay
            lastSampleElapsedMillis = nowElapsedMillis
            return snapshot
        }

        val total = safeAdd(rx, tx)
        val previousDay = prefs.getLong(KEY_EPOCH_DAY, Long.MIN_VALUE)
        val previousTotal = prefs.getLong(KEY_LAST_TOTAL_BYTES, -1L)
        var used = prefs.getLong(KEY_USED_TODAY_BYTES, 0L)
        var resetDetected = false

        if (previousDay != epochDay) {
            used = 0L
        } else if (previousTotal >= 0L) {
            if (total >= previousTotal) {
                used = safeAdd(used, total - previousTotal)
            } else {
                resetDetected = true
            }
        }

        prefs.edit()
            .putLong(KEY_EPOCH_DAY, epochDay)
            .putLong(KEY_LAST_TOTAL_BYTES, total)
            .putLong(KEY_USED_TODAY_BYTES, used)
            .apply()

        val limit = configuredLimitBytes()
        val snapshot = MobileBudgetSnapshot(
            supported = true,
            usedTodayBytes = used,
            dailyLimitBytes = limit,
            state = MobileBudgetPolicy.state(true, used, limit),
            counterResetDetected = resetDetected
        )
        cachedSnapshot = snapshot
        cachedEpochDay = epochDay
        lastSampleElapsedMillis = nowElapsedMillis
        return snapshot
    }

    private fun configuredLimitBytes(): Long? {
        val value = prefs.getLong(KEY_LIMIT_BYTES, 0L)
        return value.takeIf { it > 0L }
    }

    private fun safeAdd(a: Long, b: Long): Long {
        if (a < 0L || b < 0L) return 0L
        return if (Long.MAX_VALUE - a < b) Long.MAX_VALUE else a + b
    }

    companion object {
        private const val PREFS = "kinlink_mobile_budget"
        private const val KEY_EPOCH_DAY = "epoch_day"
        private const val KEY_LAST_TOTAL_BYTES = "last_total_mobile_bytes"
        private const val KEY_USED_TODAY_BYTES = "used_today_mobile_bytes"
        private const val KEY_LIMIT_BYTES = "daily_limit_bytes"
        private val SAMPLE_LOCK = Any()
    }
}
