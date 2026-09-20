package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TelemetryFingerprintQualityTest {
    @Test fun smallBandwidthChangesInsideSameTierDoNotCreateTelemetryNoise() {
        val a = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.VALIDATED,
            downstreamKbps = 2_000,
            upstreamKbps = 600
        )
        val b = a.copy(downstreamKbps = 2_500, upstreamKbps = 700)

        assertEquals(a.telemetryFingerprint(), b.telemetryFingerprint())
    }

    @Test fun qualityTierChangeCreatesMeaningfulTelemetryChange() {
        val constrained = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.VALIDATED,
            downstreamKbps = 700,
            upstreamKbps = 180
        )
        val comfortable = constrained.copy(
            downstreamKbps = 20_000,
            upstreamKbps = 5_000
        )

        assertNotEquals(
            constrained.telemetryFingerprint(),
            comfortable.telemetryFingerprint()
        )
    }
}
