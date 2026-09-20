package com.terminator364.kinlink.core

object TransportSettlingPolicy {
    const val DEFAULT_QUIET_PERIOD_MS = 5_000L

    fun recoveryAllowed(
        nowElapsedMillis: Long,
        lastTransitionElapsedMillis: Long?,
        quietPeriodMillis: Long = DEFAULT_QUIET_PERIOD_MS
    ): Boolean {
        if (lastTransitionElapsedMillis == null) return true
        val age = (nowElapsedMillis - lastTransitionElapsedMillis).coerceAtLeast(0L)
        return age >= quietPeriodMillis
    }
}
