package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransportSettlingPolicyTest {
    @Test fun noKnownTransitionAllowsRecovery() {
        assertTrue(TransportSettlingPolicy.recoveryAllowed(10_000L, null))
    }

    @Test fun recentTransitionBlocksRecovery() {
        assertFalse(TransportSettlingPolicy.recoveryAllowed(10_000L, 8_000L, 5_000L))
    }

    @Test fun exactQuietPeriodAllowsRecovery() {
        assertTrue(TransportSettlingPolicy.recoveryAllowed(10_000L, 5_000L, 5_000L))
    }

    @Test fun clockAnomalyFailsClosedForRecoveryOnly() {
        assertFalse(TransportSettlingPolicy.recoveryAllowed(5_000L, 10_000L, 5_000L))
    }
}
