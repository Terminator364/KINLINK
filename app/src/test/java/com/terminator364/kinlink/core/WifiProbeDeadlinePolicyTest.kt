package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiProbeDeadlinePolicyTest {
    @Test fun hardOuterProbeEnvelopeFitsRecoveryDeadline() {
        assertEquals(1_500, WifiProbeDeadlinePolicy.ATTEMPT_HARD_TIMEOUT_MS)
        assertEquals(3_000, WifiProbeDeadlinePolicy.WORST_CASE_HTTP_WAIT_MS)
        assertTrue(
            WifiProbeDeadlinePolicy.fitsWithin(
                AutopilotRecoveryController.RECOVERY_DEADLINE_MS
            )
        )
    }

    @Test fun socketTimeoutsRemainBelowOuterRecoveryDeadline() {
        val configuredSocketEnvelope =
            WifiProbeDeadlinePolicy.MAX_ENDPOINTS *
                (WifiProbeDeadlinePolicy.CONNECT_TIMEOUT_MS +
                    WifiProbeDeadlinePolicy.READ_TIMEOUT_MS)
        assertEquals(3_600, configuredSocketEnvelope)
        assertTrue(
            configuredSocketEnvelope <
                AutopilotRecoveryController.RECOVERY_DEADLINE_MS
        )
    }
}
