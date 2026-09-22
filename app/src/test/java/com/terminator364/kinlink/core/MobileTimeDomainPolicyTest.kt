package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileTimeDomainPolicyTest {
    @Test fun planCycleUsesWallClockAndControlUsesMonotonicTime() {
        assertEquals(
            MobileTimeDomain.WALL_CLOCK_CALENDAR,
            MobileTimeDomainPolicy.planCycleDomain()
        )
        assertEquals(
            MobileTimeDomain.MONOTONIC_ELAPSED,
            MobileTimeDomainPolicy.controlLoopDomain()
        )
    }

    @Test fun planExpiryFollowsCalendarEpoch() {
        assertFalse(MobileTimeDomainPolicy.planExpired(10_000L, 9_999L))
        assertTrue(MobileTimeDomainPolicy.planExpired(10_000L, 10_000L))
    }

    @Test fun controlDeadlineDependsOnlyOnMonotonicElapsedValues() {
        assertFalse(
            MobileTimeDomainPolicy.deadlineReached(
                startedAtElapsedMillis = 1_000L,
                nowElapsedMillis = 4_999L,
                timeoutMillis = 4_000L
            )
        )
        assertTrue(
            MobileTimeDomainPolicy.deadlineReached(
                startedAtElapsedMillis = 1_000L,
                nowElapsedMillis = 5_000L,
                timeoutMillis = 4_000L
            )
        )
    }

    @Test fun backwardMonotonicSampleCannotFakeElapsedTime() {
        assertEquals(
            null,
            MobileTimeDomainPolicy.elapsedControlMillis(5_000L, 4_000L)
        )
    }
}
