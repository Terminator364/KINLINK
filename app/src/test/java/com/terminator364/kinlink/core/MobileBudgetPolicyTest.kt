package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MobileBudgetPolicyTest {
    private val mib = 1024L * 1024L

    @Test fun unknownWhenNoLimitConfigured() {
        assertEquals(
            BudgetState.BALANCE_UNKNOWN,
            MobileBudgetPolicy.state(true, 50L * mib, null)
        )
    }

    @Test fun unknownWhenCountersUnsupported() {
        assertEquals(
            BudgetState.BALANCE_UNKNOWN,
            MobileBudgetPolicy.state(false, 0L, 100L * mib)
        )
    }

    @Test fun okBelowEightyPercent() {
        assertEquals(
            BudgetState.BUNDLE_OK,
            MobileBudgetPolicy.state(true, 79L * mib, 100L * mib)
        )
    }

    @Test fun lowAtEightyPercent() {
        assertEquals(
            BudgetState.BUNDLE_LOW,
            MobileBudgetPolicy.state(true, 80L * mib, 100L * mib)
        )
    }

    @Test fun exhaustedAtLimit() {
        assertEquals(
            BudgetState.BUNDLE_EXHAUSTED,
            MobileBudgetPolicy.state(true, 100L * mib, 100L * mib)
        )
    }
}
