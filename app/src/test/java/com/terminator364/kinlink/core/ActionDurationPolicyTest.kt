package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionDurationPolicyTest {
    @Test fun classifiesControlPathDurationWithoutCallingItNetworkLatency() {
        assertEquals(ActionDurationClass.FAST, ActionDurationPolicy.classify(100))
        assertEquals(ActionDurationClass.BOUNDED, ActionDurationPolicy.classify(1000))
        assertEquals(ActionDurationClass.NEAR_WATCHDOG, ActionDurationPolicy.classify(3000))
        assertEquals(ActionDurationClass.OVER_WATCHDOG, ActionDurationPolicy.classify(6000))
    }

    @Test fun watchdogFreezesAfterHalfBudget() {
        assertEquals(
            RecoveryWatchdogAction.FREEZE_NEW_ACTIONS,
            RecoveryWatchdogPolicy.action(
                nowElapsedMillis = 3_000L,
                lastHeartbeatElapsedMillis = 0L,
                hardDeadlineMillis = 5_000L
            )
        )
    }
}
