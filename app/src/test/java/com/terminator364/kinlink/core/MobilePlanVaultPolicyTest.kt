package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MobilePlanVaultPolicyTest {
    private val mb = 1_000_000L
    private val config = MobilePlanConfig(
        totalBytes = 5_000L * mb,
        expiryAtEpochMillis = 10_000L,
        protectedReserveBytes = 1_000L * mb,
        rescueAllowanceBytes = 300L * mb,
        criticalInteractiveAllowanceBytes = 100L * mb
    )

    private fun usage(remainingMb: Long) = MobilePlanUsage(
        usedBytes = config.totalBytes - remainingMb * mb,
        source = MobilePlanUsageSource.USER_RECONCILED,
        observedAtEpochMillis = 1_000L
    )

    @Test fun computesNormalAllowance() {
        val a = MobilePlanVaultPolicy.evaluate(config, usage(2_000L), 2_000L)
        assertEquals(3_600L * mb, a.normalAllowanceBytes)
        assertEquals(MobileVaultZone.NORMAL, a.zone)
        assertTrue(a.autonomousMobileActionAllowed)
    }

    @Test fun protectsReserveBeforeRescue() {
        val a = MobilePlanVaultPolicy.evaluate(config, usage(1_200L), 2_000L)
        assertEquals(MobileVaultZone.PROTECTED_RESERVE, a.zone)
        assertFalse(a.autonomousMobileActionAllowed)
        assertFalse(MobilePlanVaultPolicy.maySpend(a.zone, MobileSpendClass.NORMAL))
    }

    @Test fun rescueAndCriticalAreSeparated() {
        val rescue = MobilePlanVaultPolicy.evaluate(config, usage(250L), 2_000L)
        assertEquals(MobileVaultZone.RESCUE_ONLY, rescue.zone)
        assertTrue(MobilePlanVaultPolicy.maySpend(rescue.zone, MobileSpendClass.RESCUE))
        assertFalse(MobilePlanVaultPolicy.maySpend(rescue.zone, MobileSpendClass.NORMAL))

        val critical = MobilePlanVaultPolicy.evaluate(config, usage(80L), 2_000L)
        assertEquals(MobileVaultZone.CRITICAL_INTERACTIVE_ONLY, critical.zone)
        assertFalse(MobilePlanVaultPolicy.maySpend(critical.zone, MobileSpendClass.RESCUE))
        assertTrue(MobilePlanVaultPolicy.maySpend(critical.zone, MobileSpendClass.CRITICAL_INTERACTIVE))
    }

    @Test fun exhaustedExpiredAndUnknownFailSafe() {
        assertEquals(
            MobileVaultZone.EXHAUSTED,
            MobilePlanVaultPolicy.evaluate(config, usage(0L), 2_000L).zone
        )
        assertEquals(
            MobileVaultZone.EXPIRED,
            MobilePlanVaultPolicy.evaluate(config, usage(2_000L), 10_000L).zone
        )
        val unknown = MobilePlanVaultPolicy.evaluate(config, null, 2_000L)
        assertEquals(MobileVaultZone.UNKNOWN, unknown.zone)
        assertFalse(unknown.autonomousMobileActionAllowed)
    }

    @Test fun invalidProtectedEnvelopesCannotOversubscribePlan() {
        val invalid = config.copy(
            protectedReserveBytes = 4_900L * mb,
            rescueAllowanceBytes = 300L * mb
        )
        val a = MobilePlanVaultPolicy.evaluate(invalid, usage(2_000L), 2_000L)
        assertFalse(a.configurationValid)
        assertEquals(MobileVaultZone.UNKNOWN, a.zone)
        assertFalse(a.autonomousMobileActionAllowed)
    }
}
