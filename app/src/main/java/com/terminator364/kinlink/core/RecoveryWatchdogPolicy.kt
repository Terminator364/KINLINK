package com.terminator364.kinlink.core

enum class RecoveryWatchdogAction {
    KEEP_RUNNING,
    FREEZE_NEW_ACTIONS,
    FAIL_OPEN
}

object RecoveryWatchdogPolicy {
    fun action(
        nowElapsedMillis: Long,
        lastHeartbeatElapsedMillis: Long?,
        hardDeadlineMillis: Long = 5_000L
    ): RecoveryWatchdogAction {
        if (lastHeartbeatElapsedMillis == null) return RecoveryWatchdogAction.FAIL_OPEN
        val age = (nowElapsedMillis - lastHeartbeatElapsedMillis).coerceAtLeast(0L)
        return when {
            age > hardDeadlineMillis -> RecoveryWatchdogAction.FAIL_OPEN
            age > hardDeadlineMillis / 2L -> RecoveryWatchdogAction.FREEZE_NEW_ACTIONS
            else -> RecoveryWatchdogAction.KEEP_RUNNING
        }
    }
}
