package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class NameResolutionPolicyTest {
    @Test fun primaryWins() {
        assertEquals(
            NameResolutionPath.PRIMARY,
            NameResolutionPolicy.choose(
                NameResolutionInput(true, true, true, false, BudgetState.BALANCE_UNKNOWN)
            )
        )
    }

    @Test fun freshCacheWinsAfterPrimaryFailure() {
        assertEquals(
            NameResolutionPath.FRESH_CACHE,
            NameResolutionPolicy.choose(
                NameResolutionInput(false, true, true, false, BudgetState.BALANCE_UNKNOWN)
            )
        )
    }

    @Test fun lowPaidBudgetAvoidsSecondaryQuery() {
        assertEquals(
            NameResolutionPath.SYSTEM_FAIL_OPEN,
            NameResolutionPolicy.choose(
                NameResolutionInput(false, false, true, true, BudgetState.BUNDLE_LOW)
            )
        )
    }
}
