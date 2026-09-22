package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileCounterResetReconciliationTest {
    @Test fun repeatedCounterResetsNeverReduceProvenCycleUsage() {
        val first =
            MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
                null,
                1_000L
            )!!
        val grow =
            MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
                first.state,
                1_700L
            )!!
        val resetOne =
            MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
                grow.state,
                100L
            )!!
        val growAgain =
            MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
                resetOne.state,
                500L
            )!!
        val resetTwo =
            MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
                growAgain.state,
                50L
            )!!

        assertTrue(resetOne.resetDetected)
        assertTrue(resetTwo.resetDetected)
        assertEquals(1_100L, resetTwo.state.provenCycleBytes)
        assertEquals(2, resetTwo.state.generation)
    }
}
