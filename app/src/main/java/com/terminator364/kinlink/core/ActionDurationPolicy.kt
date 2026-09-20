package com.terminator364.kinlink.core

enum class ActionDurationClass {
    FAST,
    BOUNDED,
    NEAR_WATCHDOG,
    OVER_WATCHDOG
}

object ActionDurationPolicy {
    fun classify(durationMillis: Long, watchdogMillis: Long = 5_000L): ActionDurationClass {
        val safe = durationMillis.coerceAtLeast(0L)
        return when {
            safe > watchdogMillis -> ActionDurationClass.OVER_WATCHDOG
            safe > watchdogMillis / 2L -> ActionDurationClass.NEAR_WATCHDOG
            safe <= 250L -> ActionDurationClass.FAST
            else -> ActionDurationClass.BOUNDED
        }
    }
}
