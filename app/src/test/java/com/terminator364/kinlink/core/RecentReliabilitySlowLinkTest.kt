package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentReliabilitySlowLinkTest {
    @Test fun longSlowLinkBurdenCountsEvenWithoutOutage() {
        assertEquals(
            RecentReliabilityBurden.SEVERE,
            RecentReliabilityPolicy.classify(
                interruptionCount = 0,
                cumulativeMillis = 0L,
                longestMillis = 0L,
                lowQualityEpisodeCount = 1,
                lowQualityCumulativeMillis = 31L * 60L * 1000L,
                lowQualityLongestMillis = 12L * 60L * 1000L
            )
        )
    }

    @Test fun oneShortSlowEpisodeIsNoticeable() {
        assertEquals(
            RecentReliabilityBurden.NOTICEABLE,
            RecentReliabilityPolicy.classify(
                0, 0L, 0L,
                lowQualityEpisodeCount = 1,
                lowQualityCumulativeMillis = 60_000L,
                lowQualityLongestMillis = 60_000L
            )
        )
    }
}
