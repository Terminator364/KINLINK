package com.terminator364.kinlink.core

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager

data class ResourceGuardSnapshot(
    val powerSaveMode: Boolean,
    val thermalSevereOrWorse: Boolean,
    val lowMemory: Boolean
) {
    val constrained: Boolean
        get() = ResourceGuardPolicy.constrained(
            powerSaveMode = powerSaveMode,
            thermalSevereOrWorse = thermalSevereOrWorse,
            lowMemory = lowMemory
        )
}

object ResourceGuardPolicy {
    fun constrained(
        powerSaveMode: Boolean,
        thermalSevereOrWorse: Boolean,
        lowMemory: Boolean = false
    ): Boolean = powerSaveMode || thermalSevereOrWorse || lowMemory
}

class DeviceResourceGuard(context: Context) {
    private val powerManager = context.getSystemService(PowerManager::class.java)
    private val activityManager = context.getSystemService(ActivityManager::class.java)

    fun snapshot(): ResourceGuardSnapshot {
        val thermalSevere = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            powerManager.currentThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE
        } else false

        val memoryInfo = ActivityManager.MemoryInfo()
        val lowMemory = runCatching {
            activityManager.getMemoryInfo(memoryInfo)
            memoryInfo.lowMemory
        }.getOrDefault(false)

        return ResourceGuardSnapshot(
            powerSaveMode = powerManager.isPowerSaveMode,
            thermalSevereOrWorse = thermalSevere,
            lowMemory = lowMemory
        )
    }
}
