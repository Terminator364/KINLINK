package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveRecoveryPolicyTest {
    @Test fun automaticWifiAllowsBoundedRecovery() {
        assertTrue(ActiveRecoveryPolicy.allowed(RecoveryMode.AUTOMATIC, Transport.WIFI))
    }

    @Test fun observationOnlyBlocksWifiRecovery() {
        assertFalse(ActiveRecoveryPolicy.allowed(RecoveryMode.OBSERVATION_ONLY, Transport.WIFI))
    }

    @Test fun cellularNeverAllowsActiveRecovery() {
        assertFalse(ActiveRecoveryPolicy.allowed(RecoveryMode.AUTOMATIC, Transport.CELLULAR))
        assertFalse(ActiveRecoveryPolicy.allowed(RecoveryMode.OBSERVATION_ONLY, Transport.CELLULAR))
    }

    @Test fun unknownNeverAllowsActiveRecovery() {
        assertFalse(ActiveRecoveryPolicy.allowed(RecoveryMode.AUTOMATIC, Transport.UNKNOWN))
    }
}
