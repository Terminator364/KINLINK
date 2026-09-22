package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageCallbackContinuityPolicyTest {
    @Test fun missingCallbackNeverMeansZeroUsage() {
        assertNull(
            UsageCallbackContinuityPolicy.usageImpliedByMissingCallbackBytes()
        )
    }

    @Test fun processRestartRequiresFreshReconciliationEvenAfterPriorCallback() {
        var old = UsageCallbackContinuityPolicy.startProcess(3L)
        old = UsageCallbackContinuityPolicy.observeCallback(old)
        old = UsageCallbackContinuityPolicy.recordFreshReconciliation(
            old,
            10_000L
        )
        assertTrue(UsageCallbackContinuityPolicy.mayUseUsageForSpending(old))

        val restarted = UsageCallbackContinuityPolicy.startProcess(
            old.processGeneration
        )
        assertFalse(restarted.callbackObservedThisProcess)
        assertEquals(
            UsageCallbackContinuityStatus.FRESH_RECONCILIATION_REQUIRED,
            UsageCallbackContinuityPolicy.status(restarted)
        )
        assertFalse(
            UsageCallbackContinuityPolicy.mayUseUsageForSpending(restarted)
        )
    }

    @Test fun callbackAloneDoesNotAuthorizeSpendingEvidence() {
        val state = UsageCallbackContinuityPolicy.observeCallback(
            UsageCallbackContinuityPolicy.startProcess(0L)
        )
        assertTrue(state.callbackObservedThisProcess)
        assertFalse(
            UsageCallbackContinuityPolicy.mayUseUsageForSpending(state)
        )
    }

    @Test fun freshReconciliationAuthorizesCurrentProcessOnly() {
        val state = UsageCallbackContinuityPolicy.recordFreshReconciliation(
            UsageCallbackContinuityPolicy.startProcess(0L),
            20_000L
        )
        assertEquals(
            UsageCallbackContinuityStatus.RECONCILED_THIS_PROCESS,
            UsageCallbackContinuityPolicy.status(state)
        )
        assertTrue(UsageCallbackContinuityPolicy.mayUseUsageForSpending(state))
    }
}
