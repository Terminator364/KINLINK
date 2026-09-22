package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkStatsQuerySessionGateTest {
    @Test
    fun onlyOneQueryCanBeInFlight() {
        val gate = NetworkStatsQuerySessionGate()
        val first = gate.tryStart()
        val second = gate.tryStart()

        assertEquals(NetworkStatsQueryGateStatus.STARTED, first.status)
        assertNotNull(first.token)
        assertEquals(NetworkStatsQueryGateStatus.IN_FLIGHT, second.status)
        assertEquals(null, second.token)
    }

    @Test
    fun completionAllowsANewLeaseAndStaleTimeoutCannotDisableIt() {
        val gate = NetworkStatsQuerySessionGate()
        val first = gate.tryStart()
        val firstToken = requireNotNull(first.token)
        assertTrue(gate.complete(firstToken))

        val second = gate.tryStart()
        val secondToken = requireNotNull(second.token)
        assertNotEquals(firstToken, secondToken)
        assertFalse(gate.timeoutAndDisable(firstToken))
        assertFalse(gate.isSessionDisabled())
        assertTrue(gate.complete(secondToken))
    }

    @Test
    fun timeoutDisablesOptionalPathForRestOfSession() {
        val gate = NetworkStatsQuerySessionGate()
        val started = gate.tryStart()
        val token = requireNotNull(started.token)

        assertTrue(gate.timeoutAndDisable(token))
        assertTrue(gate.isSessionDisabled())
        assertEquals(
            NetworkStatsQueryGateStatus.SESSION_DISABLED,
            gate.tryStart().status
        )
        assertFalse(gate.complete(token))
    }

    @Test
    fun explicitSessionDisableRejectsFurtherQueries() {
        val gate = NetworkStatsQuerySessionGate()
        gate.disableForSession()

        assertTrue(gate.isSessionDisabled())
        assertEquals(
            NetworkStatsQueryGateStatus.SESSION_DISABLED,
            gate.tryStart().status
        )
    }

    @Test
    fun latencyBudgetIsPositiveAndBelowRecoveryDeadline() {
        assertTrue(NetworkStatsQuerySessionGate.DEFAULT_TIMEOUT_MILLIS > 0L)
        assertTrue(NetworkStatsQuerySessionGate.DEFAULT_TIMEOUT_MILLIS < 5_000L)
    }
}
