package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkStabilityPolicyTest {
    @Test fun emptyWindowIsNeutral() {
        val a = NetworkStabilityPolicy.assess(0, 0, 0)
        assertEquals(0, a.score)
        assertFalse(a.flapping)
    }

    @Test fun validatedStableWindowStaysLow() {
        val a = NetworkStabilityPolicy.assess(events = 5, transitions = 0, validatedEvents = 5)
        assertTrue(a.score < 20)
        assertFalse(a.flapping)
    }

    @Test fun repeatedTransitionsBecomeFlapping() {
        val a = NetworkStabilityPolicy.assess(events = 6, transitions = 4, validatedEvents = 3)
        assertTrue(a.score >= 50)
        assertTrue(a.flapping)
    }
}
