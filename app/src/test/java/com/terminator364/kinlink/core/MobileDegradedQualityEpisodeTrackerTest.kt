package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileDegradedQualityEpisodeTrackerTest {
    @Test fun validatedSlowCellularEpisodeEndsOnRecovery() {
        val t = MobileDegradedQualityEpisodeTracker()
        assertNull(
            t.observe(
                NetworkTruth(
                    transport = Transport.CELLULAR,
                    internetState = InternetState.VALIDATED,
                    downstreamKbps = 700,
                    upstreamKbps = 200,
                    observedAtMillis = 1_000L
                )
            )
        )
        val episode = t.observe(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 12_000,
                upstreamKbps = 3_000,
                observedAtMillis = 4_000L
            )
        )
        requireNotNull(episode)
        assertEquals(3_000L, episode.durationMillis)
        assertEquals(DegradedQualitySeverity.CONSTRAINED, episode.severity)
    }

    @Test fun cellularEpisodeEndsInconspicuouslyOnTransportChange() {
        val t = MobileDegradedQualityEpisodeTracker()
        t.observe(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 2_000,
                upstreamKbps = 700,
                observedAtMillis = 10L
            )
        )
        val episode = t.observe(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 20_000,
                upstreamKbps = 5_000,
                observedAtMillis = 510L
            )
        )
        requireNotNull(episode)
        assertTrue(episode.durationMillis >= 500L)
    }
}
