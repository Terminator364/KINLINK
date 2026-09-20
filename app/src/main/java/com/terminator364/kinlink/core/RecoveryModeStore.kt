package com.terminator364.kinlink.core

import android.content.Context

enum class RecoveryMode {
    AUTOMATIC,
    OBSERVATION_ONLY
}

class RecoveryModeStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun current(): RecoveryMode =
        runCatching {
            RecoveryMode.valueOf(
                prefs.getString(KEY_MODE, RecoveryMode.AUTOMATIC.name)
                    ?: RecoveryMode.AUTOMATIC.name
            )
        }.getOrDefault(RecoveryMode.AUTOMATIC)

    fun set(mode: RecoveryMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    fun toggle(): RecoveryMode {
        val next = if (current() == RecoveryMode.AUTOMATIC) {
            RecoveryMode.OBSERVATION_ONLY
        } else {
            RecoveryMode.AUTOMATIC
        }
        set(next)
        return next
    }

    companion object {
        private const val PREFS = "kinlink_recovery_mode"
        private const val KEY_MODE = "mode"
    }
}
