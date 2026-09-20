package com.terminator364.kinlink.core

import android.content.Context

data class LifecycleGuardDecision(
    val recoveryAllowed: Boolean,
    val startsInWindow: Int,
    val reason: String
)

object LifecycleGuardPolicy {
    const val WINDOW_MS = 10L * 60L * 1000L
    const val MAX_STARTS_WITH_RECOVERY = 3

    fun decide(startsInWindow: Int): LifecycleGuardDecision =
        if (startsInWindow <= MAX_STARTS_WITH_RECOVERY) {
            LifecycleGuardDecision(
                recoveryAllowed = true,
                startsInWindow = startsInWindow,
                reason = "Cycle de service normal."
            )
        } else {
            LifecycleGuardDecision(
                recoveryAllowed = false,
                startsInWindow = startsInWindow,
                reason = "Relances répétées détectées : récupération active suspendue, observation conservée."
            )
        }
}

class LifecycleSafetyGuard(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun noteStart(nowWallMs: Long = System.currentTimeMillis()): LifecycleGuardDecision {
        val windowStarted = prefs.getLong(KEY_WINDOW_START, -1L)
        val previousCount = prefs.getInt(KEY_START_COUNT, 0)
        val sameWindow = windowStarted >= 0L &&
            nowWallMs >= windowStarted &&
            nowWallMs - windowStarted <= LifecycleGuardPolicy.WINDOW_MS

        val nextWindowStart = if (sameWindow) windowStarted else nowWallMs
        val count = if (sameWindow) previousCount + 1 else 1

        prefs.edit()
            .putLong(KEY_WINDOW_START, nextWindowStart)
            .putInt(KEY_START_COUNT, count)
            .apply()

        return LifecycleGuardPolicy.decide(count)
    }

    companion object {
        private const val PREFS = "kinlink_lifecycle_guard"
        private const val KEY_WINDOW_START = "window_start"
        private const val KEY_START_COUNT = "start_count"
    }
}
