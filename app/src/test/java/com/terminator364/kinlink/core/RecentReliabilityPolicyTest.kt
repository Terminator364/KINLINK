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

    @Test fun longOutageIsSevere() {
        assertEquals(
            RecentReliabilityBurden.SEVERE,
            RecentReliabilityPolicy.classify(1, 65_000L, 65_000L)
        )
    }
}
