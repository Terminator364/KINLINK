package com.terminator364.kinlink.core

import android.content.Context
import android.os.Build
import android.os.PowerManager

data class ResourceGuardSnapshot(
    val powerSaveMode: Boolean,
    val thermalSevereOrWorse: Boolean
) {
    val constrained: Boolean
        get() = ResourceGuardPolicy.constrained(powerSaveMode, thermalSevereOrWorse)
}

object ResourceGuardPolicy {
    fun constrained(powerSaveMode: Boolean, thermalSevereOrWorse: Boolean): Boolean =
        powerSaveMode || thermalSevereOrWorse
}

class DeviceResourceGuard(context: Context) {
    private val powerManager = context.getSystemService(PowerManager::class.java)

    fun snapshot(): ResourceGuardSnapshot {
        val thermalSevere = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            powerManager.currentThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE
        } else false
        return ResourceGuardSnapshot(
            powerSaveMode = powerManager.isPowerSaveMode,
            thermalSevereOrWorse = thermalSevere
        )
    }
}
