package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DegradedQualityEpisodeTrackerTest {
    private fun wifi(t: Long, down: Int, up: Int) = NetworkTruth(
        transport = Transport.WIFI,
        internetState = InternetState.VALIDATED,
        downstreamKbps = down,
        upstreamKbps = up,
        observedAtMillis = t
    )

    @Test fun measuresValidatedSlowWifiEpisode() {
        val tracker = DegradedQualityEpisodeTracker()
        assertNull(tracker.observe(wifi(1_000, 2_000, 700)))
        assertNull(tracker.observe(wifi(3_000, 700, 180)))
        val result = tracker.observe(wifi(6_000, 20_000, 5_000))
        assertEquals(5_000L, result?.durationMillis)
        assertEquals(DegradedQualitySeverity.CONSTRAINED, result?.severity)
    }

    @Test fun healthyStartupDoesNotInventEpisode() {
        val tracker = DegradedQualityEpisodeTracker()
        assertNull(tracker.observe(wifi(1_000, 20_000, 5_000)))
    }

    @Test fun handoffEndsCurrentSlowWifiEpisode() {
        val tracker = DegradedQualityEpisodeTracker()
        tracker.observe(wifi(1_000, 2_000, 700))
        val result = tracker.observe(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                observedAtMillis = 4_500
            )
        )
        assertEquals(3_500L, result?.durationMillis)
    }
}
