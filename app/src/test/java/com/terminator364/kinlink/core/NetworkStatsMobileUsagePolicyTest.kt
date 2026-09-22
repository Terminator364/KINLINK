package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class NetworkStatsMobileUsagePolicyTest {
    @Test
    fun missingCycleStartBlocksQuerySemantics() {
        val request = NetworkStatsMobileUsageRequest(
            cycleStartAtEpochMillis = null,
            endAtEpochMillis = 2_000L
        )

        assertEquals(
            NetworkStatsMobileEvidenceStatus.CYCLE_START_REQUIRED,
            NetworkStatsMobileUsagePolicy.validationStatus(request)
        )
    }

    @Test
    fun invalidWindowIsRejected() {
        val request = NetworkStatsMobileUsageRequest(
            cycleStartAtEpochMillis = 2_000L,
            endAtEpochMillis = 2_000L
        )

        assertEquals(
            NetworkStatsMobileEvidenceStatus.INVALID_WINDOW,
            NetworkStatsMobileUsagePolicy.validationStatus(request)
        )
    }

    @Test
    fun availableNetworkStatsEvidenceAlwaysStaysDeviceAggregate() {
        val evidence = NetworkStatsMobileUsageEvidence(
            status = NetworkStatsMobileEvidenceStatus.AVAILABLE,
            usedBytes = 123_000_000L,
            cycleStartAtEpochMillis = 1_000L,
            observedAtEpochMillis = 2_000L,
            confidencePercent = 75,
            reason = "test"
        )

        val observation =
            NetworkStatsMobileUsagePolicy.toObservation(evidence)

        assertNotNull(observation)
        assertEquals(
            MobilePlanUsageSource.NETWORK_STATS_OPTIONAL,
            observation?.source
        )
        assertEquals(
            MobileUsageAttributionScope.DEVICE_MOBILE_AGGREGATE,
            observation?.attributionScope
        )
    }

    @Test
    fun unavailableEvidenceCannotEnterReconciliation() {
        val evidence = NetworkStatsMobileUsageEvidence(
            status = NetworkStatsMobileEvidenceStatus.USAGE_ACCESS_REQUIRED,
            usedBytes = null,
            cycleStartAtEpochMillis = 1_000L,
            observedAtEpochMillis = 2_000L,
            confidencePercent = 0,
            reason = "test"
        )

        assertNull(
            NetworkStatsMobileUsagePolicy.toObservation(evidence)
        )
    }

    @Test
    fun safeByteAdditionSaturatesAndRejectsInvalidCounters() {
        assertEquals(
            Long.MAX_VALUE,
            NetworkStatsMobileUsagePolicy.safeTotalBytes(
                Long.MAX_VALUE - 1L,
                10L
            )
        )
        assertNull(
            NetworkStatsMobileUsagePolicy.safeTotalBytes(-1L, 10L)
        )
    }
}
