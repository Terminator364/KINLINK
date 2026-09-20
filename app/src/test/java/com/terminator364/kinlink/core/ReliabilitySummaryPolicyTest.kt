package com.terminator364.kinlink.core

import org.junit.Assert.assertTrue
import org.junit.Test

class ReliabilitySummaryPolicyTest {
    @Test fun quietWindowIsReadable() {
        assertTrue(
            ReliabilitySummaryPolicy.label(0, 0L, 0L, 0, null)
                .contains("aucune coupure")
        )
    }

    @Test fun busyWindowContainsDurationsAndCause() {
        val label = ReliabilitySummaryPolicy.label(
            interruptionCount = 3,
            cumulativeMillis = 65_000L,
            longestMillis = 40_000L,
            lowQualityEpisodeCount = 2,
            dominantCause = "LOW_CAPACITY"
        )
        assertTrue(label.contains("coupures=3"))
        assertTrue(label.contains("1 min 5 s"))
        assertTrue(label.contains("LOW_CAPACITY"))
    }
}
