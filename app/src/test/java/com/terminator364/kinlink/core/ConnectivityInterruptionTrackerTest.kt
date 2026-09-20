package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConnectivityInterruptionTrackerTest {
    private fun truth(state: InternetState, t: Long, transport: Transport = Transport.WIFI) =
        NetworkTruth(
            transport = transport,
            internetState = state,
            observedAtMillis = t
        )

    @Test fun microInterruptionIsMeasuredOnRecovery() {
        val tracker = ConnectivityInterruptionTracker()
        assertNull(tracker.observe(truth(InternetState.VALIDATED, 1_000)))
        assertNull(tracker.observe(truth(InternetState.UNKNOWN, 2_000)))
        val result = tracker.observe(truth(InternetState.VALIDATED, 3_200))
        assertEquals(InterruptionSeverity.MICRO, result?.severity)
        assertEquals(1_200L, result?.durationMillis)
    }

    @Test fun longInterruptionIsMeasured() {
        val tracker = ConnectivityInterruptionTracker()
        tracker.observe(truth(InternetState.VALIDATED, 1_000))
        tracker.observe(truth(InternetState.OFFLINE, 2_000, Transport.NONE))
        val result = tracker.observe(truth(InternetState.VALIDATED, 45_000))
        assertEquals(InterruptionSeverity.LONG, result?.severity)
    }

    @Test fun initialUnknownDoesNotInventOutage() {
        val tracker = ConnectivityInterruptionTracker()
        assertNull(tracker.observe(truth(InternetState.UNKNOWN, 1_000)))
        assertNull(tracker.observe(truth(InternetState.VALIDATED, 2_000)))
    }
}
