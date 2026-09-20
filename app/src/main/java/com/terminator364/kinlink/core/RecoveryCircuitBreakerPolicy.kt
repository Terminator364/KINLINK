package com.terminator364.kinlink.core

object RecoveryCircuitBreakerPolicy {
    const val SAFETY_ABORT_THRESHOLD = 2
    const val WINDOW_MS = 30L * 60L * 1000L

    fun open(recentSafetyAborts: Int): Boolean =
        recentSafetyAborts >= SAFETY_ABORT_THRESHOLD
}
