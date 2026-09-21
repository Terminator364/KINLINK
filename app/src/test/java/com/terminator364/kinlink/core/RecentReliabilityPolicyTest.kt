package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentReliabilityPolicyTest {
    @Test fun quietWhenNoInterruptions() {
        assertEquals(
            RecentReliabilityBurden.QUIET,
            RecentReliabilityPolicy.classify(0, 0L, 0L)
        )
    }

    @Test fun oneMicroInterruptionIsNoticeableOnly() {
        assertEquals(
            RecentReliabilityBurden.NOTICEABLE,
            RecentReliabilityPolicy.classify(1, 1_000L, 1_000L)
        )
    }

    @Test fun repeatedInterruptionsBecomeUnstable() {
        assertEquals(
            RecentReliabilityBurden.UNSTABLE,
            RecentReliabilityPolicy.classify(4, 12_000L, 4_000L)
        )
    }

    @Test fun repeatedSlowValidatedEpisodesBecomeUnstable() {
        assertEquals(
            RecentReliabilityBurden.UNSTABLE,
            RecentReliabilityPolicy.classify(
                interruptionCount = 0,
                cumulativeMillis = 0L,
                longestMillis = 0L,
                lowQualityEpisodeCount = 4,
                lowQualityCumulativeMillis = 8L * 60L * 1000L,
                lowQualityLongestMillis = 3L * 60L * 1000L
            )
        )
    }

    @Test fun prolongedSlowValidatedBurdenCanBeSevereWithoutOutage() {
        assertEquals(
            RecentReliabilityBurden.SEVERE,
            RecentReliabilityPolicy.classify(
                interruptionCount = 0,
                cumulativeMillis = 0L,
                longestMillis = 0L,
                lowQualityEpisodeCount = 2,
                lowQualityCumulativeMillis = 31L * 60L * 1000L,
                lowQualityLongestMillis = 9L * 60L * 1000L
            )
        )
    }

    @Test fun longOutageIsSevere() {
        assertEquals(
            RecentReliabilityBurden.SEVERE,
            RecentReliabilityPolicy.classify(1, 65_000L, 65_000L)
        )
    }
}
