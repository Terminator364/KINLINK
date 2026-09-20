package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileBudgetSamplingPolicyTest {
    @Test fun freshSameDaySampleCanBeReused() {
        assertTrue(
            MobileBudgetSamplingPolicy.shouldReuse(
                nowElapsedMillis = 20_000L,
                lastSampleElapsedMillis = 10_000L,
                sameEpochDay = true
            )
        )
    }

    @Test fun staleSampleMustRefresh() {
        assertFalse(
            MobileBudgetSamplingPolicy.shouldReuse(
                nowElapsedMillis = 30_000L,
                lastSampleElapsedMillis = 10_000L,
                sameEpochDay = true
            )
        )
    }

    @Test fun newDayNeverReusesOldSample() {
        assertFalse(
            MobileBudgetSamplingPolicy.shouldReuse(
                nowElapsedMillis = 11_000L,
                lastSampleElapsedMillis = 10_000L,
                sameEpochDay = false
            )
        )
    }
}
