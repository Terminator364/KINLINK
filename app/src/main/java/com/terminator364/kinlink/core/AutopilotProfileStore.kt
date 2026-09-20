package com.terminator364.kinlink.core

import android.content.Context

class AutopilotProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun current(): AutopilotProfile {
        val raw = prefs.getString(KEY_PROFILE, AutopilotProfile.BALANCED.name)
        return runCatching { AutopilotProfile.valueOf(raw ?: AutopilotProfile.BALANCED.name) }
            .getOrDefault(AutopilotProfile.BALANCED)
    }

    fun set(profile: AutopilotProfile) {
        prefs.edit().putString(KEY_PROFILE, profile.name).apply()
    }

    fun cycle(): AutopilotProfile {
        val next = when (current()) {
            AutopilotProfile.CONSERVATIVE -> AutopilotProfile.BALANCED
            AutopilotProfile.BALANCED -> AutopilotProfile.MAXIMUM_STABILITY
            AutopilotProfile.MAXIMUM_STABILITY -> AutopilotProfile.CONSERVATIVE
        }
        set(next)
        return next
    }

    companion object {
        private const val PREFS = "kinlink_autopilot_profile"
        private const val KEY_PROFILE = "profile"
    }
}
