package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryWatchdogPolicyTest {
    @Test fun healthyHeartbeatKeepsRunning() {
        assertEquals(
            RecoveryWatchdogAction.KEEP_RUNNING,
            RecoveryWatchdogPolicy.action(10_000L, 8_000L, 5_000L)
        )
    }

    @Test fun agingHeartbeatFreezesNewActions() {
        assertEquals(
            RecoveryWatchdogAction.FREEZE_NEW_ACTIONS,
            RecoveryWatchdogPolicy.action(10_000L, 6_500L, 5_000L)
        )
    }

    @Test fun expiredHeartbeatFailsOpen() {
        assertEquals(
            RecoveryWatchdogAction.FAIL_OPEN,
            RecoveryWatchdogPolicy.action(10_000L, 4_000L, 5_000L)
        )
    }

    @Test fun exactDeadlineDoesNotFailOpenEarly() {
        assertEquals(
            RecoveryWatchdogAction.FREEZE_NEW_ACTIONS,
            RecoveryWatchdogPolicy.action(10_000L, 5_000L, 5_000L)
        )
    }

    @Test fun missingHeartbeatFailsOpen() {
        assertEquals(
            RecoveryWatchdogAction.FAIL_OPEN,
            RecoveryWatchdogPolicy.action(10_000L, null, 5_000L)
        )
    }
}
