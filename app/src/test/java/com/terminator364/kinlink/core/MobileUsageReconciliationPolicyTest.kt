package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileUsageReconciliationPolicyTest {
    private val now = 2_000_000L

    @Test fun aggregateOnlyCannotDriveOnePlanZone() {
        val r = MobileUsageReconciliationPolicy.reconcile(
            listOf(
                MobileUsageObservation(
                    usedBytes = 600_000_000L,
                    source = MobilePlanUsageSource.ANDROID_DEVICE_WIDE_COUNTER,
                    attributionScope = MobileUsageAttributionScope.DEVICE_MOBILE_AGGREGATE,
                    observedAtEpochMillis = now - 1_000L,
                    confidencePercent = 90
                )
            ),
            now
        )
        assertEquals(MobileUsageResolutionStatus.HOLD_UNATTRIBUTED, r.status)
        assertNull(r.usage)
    }

    @Test fun oneDeviceAggregateCannotBecomeEitherOfTwoPlanBalances() {
        val aggregate = MobileUsageObservation(
            usedBytes = 900_000_000L,
            source = MobilePlanUsageSource.NETWORK_STATS_OPTIONAL,
            attributionScope = MobileUsageAttributionScope.DEVICE_MOBILE_AGGREGATE,
            observedAtEpochMillis = now - 1_000L,
            confidencePercent = 75
        )
        val resolution = MobileUsageReconciliationPolicy.reconcile(
            listOf(aggregate),
            now
        )
        val planOne = MobilePlanConfig(
            totalBytes = 2_000_000_000L,
            expiryAtEpochMillis = null,
            protectedReserveBytes = 200_000_000L,
            rescueAllowanceBytes = 100_000_000L,
            criticalInteractiveAllowanceBytes = 50_000_000L
        )
        val planTwo = MobilePlanConfig(
            totalBytes = 5_000_000_000L,
            expiryAtEpochMillis = null,
            protectedReserveBytes = 500_000_000L,
            rescueAllowanceBytes = 200_000_000L,
            criticalInteractiveAllowanceBytes = 100_000_000L
        )

        assertEquals(
            MobileUsageResolutionStatus.HOLD_UNATTRIBUTED,
            resolution.status
        )
        assertNull(resolution.usage)
        assertEquals(
            MobileVaultZone.UNKNOWN,
            MobilePlanVaultPolicy.evaluate(planOne, resolution.usage, now).zone
        )
        assertEquals(
            MobileVaultZone.UNKNOWN,
            MobilePlanVaultPolicy.evaluate(planTwo, resolution.usage, now).zone
        )
    }

    @Test fun exactSourcesInLargeConflictHoldInsteadOfAverage() {
        val r = MobileUsageReconciliationPolicy.reconcile(
            listOf(
                MobileUsageObservation(
                    usedBytes = 200_000_000L,
                    source = MobilePlanUsageSource.USER_RECONCILED,
                    attributionScope = MobileUsageAttributionScope.PLAN_EXACT,
                    observedAtEpochMillis = now - 1_000L,
                    confidencePercent = 100
                ),
                MobileUsageObservation(
                    usedBytes = 500_000_000L,
                    source = MobilePlanUsageSource.NETWORK_STATS_OPTIONAL,
                    attributionScope = MobileUsageAttributionScope.PLAN_EXACT,
                    observedAtEpochMillis = now - 2_000L,
                    confidencePercent = 90
                )
            ),
            now
        )
        assertEquals(MobileUsageResolutionStatus.HOLD_CONFLICT, r.status)
        assertNull(r.usage)
    }

    @Test fun closeExactSourcesUseConservativeMaximum() {
        val r = MobileUsageReconciliationPolicy.reconcile(
            listOf(
                MobileUsageObservation(
                    usedBytes = 500_000_000L,
                    source = MobilePlanUsageSource.USER_RECONCILED,
                    attributionScope = MobileUsageAttributionScope.PLAN_EXACT,
                    observedAtEpochMillis = now - 1_000L,
                    confidencePercent = 100
                ),
                MobileUsageObservation(
                    usedBytes = 504_000_000L,
                    source = MobilePlanUsageSource.CARRIER_ADAPTER,
                    attributionScope = MobileUsageAttributionScope.PLAN_EXACT,
                    observedAtEpochMillis = now - 2_000L,
                    confidencePercent = 95
                )
            ),
            now
        )
        assertEquals(MobileUsageResolutionStatus.RESOLVED, r.status)
        assertEquals(504_000_000L, r.usage?.usedBytes)
        assertEquals(MobilePlanUsageSource.USER_RECONCILED, r.usage?.source)
    }

    @Test fun rebootCounterResetNeverCreatesFreeQuota() {
        val first = MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
            null,
            1_000L
        )!!
        val second = MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
            first.state,
            1_600L
        )!!
        assertEquals(600L, second.state.provenCycleBytes)
        val afterReset = MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
            second.state,
            100L
        )!!
        assertTrue(afterReset.resetDetected)
        assertEquals(600L, afterReset.state.provenCycleBytes)
        val afterMore = MobileUsageReconciliationPolicy.advanceDeviceAggregateCounter(
            afterReset.state,
            300L
        )!!
        assertEquals(800L, afterMore.state.provenCycleBytes)
        assertEquals(1, afterMore.state.generation)
    }
}
