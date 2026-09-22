package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MobileUsageProvenancePolicyTest {
    @Test fun balanceSemanticCannotBeMisreadAsCycleUsedBytes() {
        val result = MobileUsageReconciliationPolicy.reconcile(
            listOf(
                MobileUsageObservation(
                    usedBytes = 900_000_000L,
                    source = MobilePlanUsageSource.CARRIER_ADAPTER,
                    attributionScope = MobileUsageAttributionScope.PLAN_EXACT,
                    observedAtEpochMillis = 9_000L,
                    confidencePercent = 95,
                    semanticKind =
                        MobileUsageSemanticKind.PLAN_REMAINING_BYTES,
                    adapterId = "carrier-wallet",
                    adapterVersion = 2
                )
            ),
            nowEpochMillis = 10_000L
        )
        assertEquals(MobileUsageResolutionStatus.UNKNOWN, result.status)
        assertNull(result.usage)
    }

    @Test fun malformedProvenanceIsRejectedInsteadOfTrusted() {
        val result = MobileUsageReconciliationPolicy.reconcile(
            listOf(
                MobileUsageObservation(
                    usedBytes = 100L,
                    source = MobilePlanUsageSource.CARRIER_ADAPTER,
                    attributionScope = MobileUsageAttributionScope.PLAN_EXACT,
                    observedAtEpochMillis = 9_000L,
                    confidencePercent = 90,
                    adapterId = "",
                    adapterVersion = 0
                )
            ),
            nowEpochMillis = 10_000L
        )
        assertEquals(MobileUsageResolutionStatus.UNKNOWN, result.status)
        assertNull(result.usage)
    }
}
