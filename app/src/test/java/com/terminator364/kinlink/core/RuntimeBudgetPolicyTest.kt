package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeBudgetPolicyTest {
    @Test fun calculatesPssDelta() {
        val e = RuntimeBudgetPolicy.evidence(
            RuntimeBudgetSnapshot(0L, 12, 80),
            RuntimeBudgetSnapshot(3_600_000L, 16, 79)
        )
        assertEquals(4, e.pssDeltaMiB)
        assertEquals(1, e.batteryDeltaPercent)
        assertTrue(e.batteryPercentPerHour!! in 0.99..1.01)
    }

    @Test fun shortSessionDoesNotPretendBatteryRate() {
        val e = RuntimeBudgetPolicy.evidence(
            RuntimeBudgetSnapshot(0L, 12, 80),
            RuntimeBudgetSnapshot(60_000L, 13, 79)
        )
        assertNull(e.batteryPercentPerHour)
    }

    @Test fun chargingOrCapacityIncreaseDoesNotBecomeNegativeDrain() {
        val e = RuntimeBudgetPolicy.evidence(
            RuntimeBudgetSnapshot(0L, 12, 50),
            RuntimeBudgetSnapshot(3_600_000L, 12, 60)
        )
        assertEquals(0, e.batteryDeltaPercent)
    }
}
