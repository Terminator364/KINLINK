package com.terminator364.kinlink.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentReliabilityWindowDataTest {
    @Test fun lowQualityDefaultsKeepBackwardConstructionSafe() {
        val window = RecentReliabilityWindow(
            interruptionCount = 1,
            cumulativeMillis = 1000L,
            longestMillis = 1000L,
            dominantCause = null
        )
        assertEquals(0, window.lowQualityEpisodeCount)
        assertEquals(0L, window.lowQualityCumulativeMillis)
        assertEquals(0L, window.lowQualityLongestMillis)
    }
}
