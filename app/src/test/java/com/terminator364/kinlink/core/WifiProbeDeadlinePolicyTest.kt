package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiProbeDeadlinePolicyTest {
    @Test fun worstCaseHttpWaitFitsRecoveryDeadline() {
        assertEquals(3_600, WifiProbeDeadlinePolicy.WORST_CASE_HTTP_WAIT_MS)
        assertTrue(
            WifiProbeDeadlinePolicy.fitsWithin(
                AutopilotRecoveryController.RECOVERY_DEADLINE_MS
            )
        )
    }
}
