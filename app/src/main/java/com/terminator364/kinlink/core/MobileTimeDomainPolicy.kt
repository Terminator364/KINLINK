package com.terminator364.kinlink.core

enum class MobileTimeDomain {
    WALL_CLOCK_CALENDAR,
    MONOTONIC_ELAPSED
}

/**
 * B97: calendar plan semantics and control-loop timing are deliberately split.
 */
object MobileTimeDomainPolicy {
    fun planCycleDomain(): MobileTimeDomain =
        MobileTimeDomain.WALL_CLOCK_CALENDAR

    fun controlLoopDomain(): MobileTimeDomain =
        MobileTimeDomain.MONOTONIC_ELAPSED

    fun planExpired(
        expiryAtEpochMillis: Long?,
        nowEpochMillis: Long
    ): Boolean =
        expiryAtEpochMillis != null &&
            expiryAtEpochMillis > 0L &&
            nowEpochMillis >= expiryAtEpochMillis

    fun elapsedControlMillis(
        startedAtElapsedMillis: Long,
        nowElapsedMillis: Long
    ): Long? {
        if (startedAtElapsedMillis < 0L) return null
        if (nowElapsedMillis < startedAtElapsedMillis) return null
        return nowElapsedMillis - startedAtElapsedMillis
    }

    fun deadlineReached(
        startedAtElapsedMillis: Long,
        nowElapsedMillis: Long,
        timeoutMillis: Long
    ): Boolean {
        if (timeoutMillis < 0L) return false
        val elapsed =
            elapsedControlMillis(startedAtElapsedMillis, nowElapsedMillis)
                ?: return false
        return elapsed >= timeoutMillis
    }
}
