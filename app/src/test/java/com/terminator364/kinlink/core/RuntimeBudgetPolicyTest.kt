package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeBudgetPolicyTest {
    @Test fun calculatesPssDelta() {
        val e = RuntimeBudgetPolicy.evidence(
            RuntimeBudgetSnapshot(0L, 12, 80, false),
            RuntimeBudgetSnapshot(3_600_000L, 16, 79, false)
        )
        assertEquals(4, e.pssDeltaMiB)
        assertEquals(1, e.batteryDeltaPercent)
        assertTrue(e.batteryPercentPerHour!! in 0.99..1.01)
    }

    @Test fun shortSessionDoesNotPretendBatteryRate() {
        val e = RuntimeBudgetPolicy.evidence(
            RuntimeBudgetSnapshot(0L, 12, 80, false),
            RuntimeBudgetSnapshot(60_000L, 13, 79, false)
        )
        assertNull(e.batteryPercentPerHour)
    }

    @Test fun pluggedAndFlatBatteryStillCannotPretendZeroDrain() {
        val e = RuntimeBudgetPolicy.evidence(
            RuntimeBudgetSnapshot(0L, 12, 100, true),
            RuntimeBudgetSnapshot(3_600_000L, 12, 100, true)
        )
        assertNull(e.batteryDeltaPercent)
        assertNull(e.batteryPercentPerHour)
    }

    @Test fun chargingMakesBatteryEvidenceInconclusive() {
        val e = RuntimeBudgetPolicy.evidence(
            RuntimeBudgetSnapshot(0L, 12, 50, true),
            RuntimeBudgetSnapshot(3_600_000L, 12, 60, true)
        )
        assertNull(e.batteryDeltaPercent)
        assertNull(e.batteryPercentPerHour)
    }
}
