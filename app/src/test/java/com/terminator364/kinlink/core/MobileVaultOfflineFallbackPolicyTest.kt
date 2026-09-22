package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class MobileVaultOfflineFallbackPolicyTest {
    private val plan = MobilePlanConfig(
        totalBytes = 5_000_000_000L,
        expiryAtEpochMillis = 50_000L,
        protectedReserveBytes = 500_000_000L,
        rescueAllowanceBytes = 200_000_000L,
        criticalInteractiveAllowanceBytes = 100_000_000L,
        cycleStartAtEpochMillis = 1_000L
    )

    @Test fun configuredLocalPlanWorksWithoutAnyExternalUsageSource() {
        val decision = MobileVaultOfflineFallbackPolicy.evaluate(
            config = plan,
            userReconciledUsedBytes = null,
            observedAtEpochMillis = null,
            nowEpochMillis = 10_000L
        )
        assertEquals(
            MobileVaultOfflineFallbackStatus.CONFIGURED_USAGE_UNKNOWN,
            decision.status
        )
        assertEquals(MobileVaultZone.UNKNOWN, decision.assessment?.zone)
        assertFalse(decision.assessment?.autonomousMobileActionAllowed ?: true)
    }

    @Test fun userEnteredUsageMakesOfflinePlanActionableWithoutCarrierAdapter() {
        val decision = MobileVaultOfflineFallbackPolicy.evaluate(
            config = plan,
            userReconciledUsedBytes = 1_000_000_000L,
            observedAtEpochMillis = 9_000L,
            nowEpochMillis = 10_000L
        )
        assertEquals(
            MobileVaultOfflineFallbackStatus.READY_WITH_USER_USAGE,
            decision.status
        )
        assertNotNull(decision.assessment)
        assertEquals(MobileVaultZone.NORMAL, decision.assessment?.zone)
    }

    @Test fun invalidLocalPlanFailsSafe() {
        val decision = MobileVaultOfflineFallbackPolicy.evaluate(
            config = plan.copy(totalBytes = 0L),
            userReconciledUsedBytes = 0L,
            observedAtEpochMillis = 9_000L,
            nowEpochMillis = 10_000L
        )
        assertEquals(
            MobileVaultOfflineFallbackStatus.INVALID_CONFIGURATION,
            decision.status
        )
        assertEquals(MobileVaultZone.UNKNOWN, decision.assessment?.zone)
    }
}
