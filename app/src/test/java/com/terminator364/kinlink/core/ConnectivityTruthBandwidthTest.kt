package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectivityTruthBandwidthTest {
    @Test fun passiveBandwidthSurvivesPureReduction() {
        val truth = ConnectivityTruthEngine.reduce(
            ConnectivityTruthEngine.Snapshot(
                transport = Transport.WIFI,
                validated = true,
                interfaceName = "wlan0",
                downstreamKbps = 2400,
                upstreamKbps = 700
            )
        )

        assertEquals(Transport.WIFI, truth.transport)
        assertEquals(InternetState.VALIDATED, truth.internetState)
        assertEquals(2400, truth.downstreamKbps)
        assertEquals(700, truth.upstreamKbps)
    }

    @Test fun negativeEstimatesAreClamped() {
        val truth = ConnectivityTruthEngine.reduce(
            ConnectivityTruthEngine.Snapshot(
                transport = Transport.WIFI,
                validated = true,
                interfaceName = "wlan0",
                downstreamKbps = -1,
                upstreamKbps = -10
            )
        )

        assertEquals(0, truth.downstreamKbps)
        assertEquals(0, truth.upstreamKbps)
        assertTrue(truth.confidence > 0.0)
    }
}
