package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionHealthPolicyTest {
    @Test fun healthyValidatedWifiIsHealthy() {
        val a = SessionHealthPolicy.assess(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 20_000,
                upstreamKbps = 5_000,
                dnsServerCount = 2
            ),
            instabilityScore = 5,
            flapping = false
        )
        assertEquals(SessionHealth.HEALTHY, a.health)
    }

    @Test fun lowCapacityIsWatchNotCritical() {
        val a = SessionHealthPolicy.assess(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 700,
                upstreamKbps = 180
            ),
            instabilityScore = 10,
            flapping = false
        )
        assertEquals(SessionHealth.WATCH, a.health)
    }

    @Test fun flappingIsDegraded() {
        val a = SessionHealthPolicy.assess(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 20_000,
                upstreamKbps = 5_000
            ),
            instabilityScore = 80,
            flapping = true
        )
        assertEquals(SessionHealth.DEGRADED, a.health)
    }

    @Test fun offlineIsCritical() {
        val a = SessionHealthPolicy.assess(
            NetworkTruth(
                transport = Transport.NONE,
                internetState = InternetState.OFFLINE
            ),
            instabilityScore = 0,
            flapping = false
        )
        assertEquals(SessionHealth.CRITICAL, a.health)
    }
}
