package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MobilePlanStoragePolicyTest {
    @Test fun decimalMbUsesHumanDecimalUnits() {
        assertEquals(
            5_000_000_000L,
            MobilePlanStoragePolicy.decimalBytes(5_000L)
        )
        assertEquals(
            5_000L,
            MobilePlanStoragePolicy.decimalMegabytes(5_000_000_000L)
        )
    }

    @Test fun decimalConversionRejectsOverflowAndNegative() {
        assertNull(MobilePlanStoragePolicy.decimalBytes(-1L))
        assertNull(
            MobilePlanStoragePolicy.decimalBytes(
                Long.MAX_VALUE / 1_000_000L + 1L
            )
        )
    }

    @Test fun storageValidationUsesVaultEnvelopeRules() {
        val valid = MobilePlanConfig(
            totalBytes = 2_000_000_000L,
            expiryAtEpochMillis = null,
            protectedReserveBytes = 500_000_000L,
            rescueAllowanceBytes = 200_000_000L,
            criticalInteractiveAllowanceBytes = 100_000_000L
        )
        assertTrue(MobilePlanStoragePolicy.validate(valid))
        assertFalse(
            MobilePlanStoragePolicy.validate(
                valid.copy(protectedReserveBytes = 2_000_000_000L)
            )
        )
    }

    @Test fun expiryFieldCanRepresentNoExpiryOrRealEpoch() {
        assertTrue(
            MobilePlanStoragePolicy.shouldAcceptExpiry(
                expiryAtEpochMillis = null,
                nowEpochMillis = 123L
            )
        )
        assertTrue(
            MobilePlanStoragePolicy.shouldAcceptExpiry(
                expiryAtEpochMillis = 1_000L,
                nowEpochMillis = 123L
            )
        )
        assertFalse(
            MobilePlanStoragePolicy.shouldAcceptExpiry(
                expiryAtEpochMillis = 0L,
                nowEpochMillis = 123L
            )
        )
    }
}
