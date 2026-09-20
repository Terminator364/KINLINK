package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveRecoveryPolicyTest {
    @Test fun automaticWifiAllowsBoundedRecovery() {
        assertTrue(ActiveRecoveryPolicy.allowed(RecoveryMode.AUTOMATIC, Transport.WIFI))
        assertEquals(
            RecoveryBlockReason.NONE,
            ActiveRecoveryPolicy.blockReason(RecoveryMode.AUTOMATIC, Transport.WIFI)
        )
    }

    @Test fun observationOnlyBlocksWifiRecoveryWithExactReason() {
        assertFalse(ActiveRecoveryPolicy.allowed(RecoveryMode.OBSERVATION_ONLY, Transport.WIFI))
        assertEquals(
            RecoveryBlockReason.OBSERVATION_ONLY,
            ActiveRecoveryPolicy.blockReason(RecoveryMode.OBSERVATION_ONLY, Transport.WIFI)
        )
    }

    @Test fun cellularNeverAllowsActiveRecovery() {
        assertFalse(ActiveRecoveryPolicy.allowed(RecoveryMode.AUTOMATIC, Transport.CELLULAR))
        assertEquals(
            RecoveryBlockReason.NON_WIFI,
            ActiveRecoveryPolicy.blockReason(RecoveryMode.AUTOMATIC, Transport.CELLULAR)
        )
    }

    @Test fun unknownNeverAllowsActiveRecovery() {
        assertFalse(ActiveRecoveryPolicy.allowed(RecoveryMode.AUTOMATIC, Transport.UNKNOWN))
    }
}
