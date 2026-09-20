package com.terminator364.kinlink.core

import android.content.Context
import android.os.BatteryManager
import android.os.Debug
import android.os.SystemClock

data class RuntimeBudgetSnapshot(
    val elapsedMillis: Long,
    val processPssMiB: Int,
    val batteryPercent: Int?
)

data class RuntimeBudgetEvidence(
    val durationMillis: Long,
    val startPssMiB: Int,
    val endPssMiB: Int,
    val pssDeltaMiB: Int,
    val batteryDeltaPercent: Int?,
    val batteryPercentPerHour: Double?
)

object RuntimeBudgetPolicy {
    fun evidence(start: RuntimeBudgetSnapshot, end: RuntimeBudgetSnapshot): RuntimeBudgetEvidence {
        val duration = (end.elapsedMillis - start.elapsedMillis).coerceAtLeast(0L)
        val batteryDelta = if (start.batteryPercent != null && end.batteryPercent != null) {
            (start.batteryPercent - end.batteryPercent).coerceAtLeast(0)
        } else null
        val perHour = if (batteryDelta != null && duration >= 30L * 60L * 1000L) {
            batteryDelta.toDouble() * 3_600_000.0 / duration.toDouble()
        } else null

        return RuntimeBudgetEvidence(
            durationMillis = duration,
            startPssMiB = start.processPssMiB,
            endPssMiB = end.processPssMiB,
            pssDeltaMiB = end.processPssMiB - start.processPssMiB,
            batteryDeltaPercent = batteryDelta,
            batteryPercentPerHour = perHour
        )
    }
}

class RuntimeBudgetSampler(context: Context) {
    private val batteryManager = context.getSystemService(BatteryManager::class.java)

    fun sample(): RuntimeBudgetSnapshot {
        val battery = runCatching {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                .takeIf { it in 0..100 }
        }.getOrNull()

        return RuntimeBudgetSnapshot(
            elapsedMillis = SystemClock.elapsedRealtime(),
            processPssMiB = (Debug.getPss() / 1024L)
                .coerceIn(0L, Int.MAX_VALUE.toLong())
                .toInt(),
            batteryPercent = battery
        )
    }
}
