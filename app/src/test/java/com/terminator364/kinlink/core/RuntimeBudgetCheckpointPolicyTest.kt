package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeBudgetCheckpointPolicyTest {
    @Test fun waitsForThirtyMinutes() {
        assertFalse(RuntimeBudgetCheckpointPolicy.shouldCheckpoint(0L, 1_799_999L, false))
        assertTrue(RuntimeBudgetCheckpointPolicy.shouldCheckpoint(0L, 1_800_000L, false))
    }

    @Test fun writesOnlyOncePerServiceSession() {
        assertFalse(RuntimeBudgetCheckpointPolicy.shouldCheckpoint(0L, 3_600_000L, true))
    }
}
