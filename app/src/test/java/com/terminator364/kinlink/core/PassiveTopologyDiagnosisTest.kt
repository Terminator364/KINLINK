package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PassiveTopologyDiagnosisTest {
    @Test fun missingAddressHasPriorityOverDnsSuspicion() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.UNKNOWN,
                lanState = LanState.LINK_PRESENT,
                dnsServerCount = 0,
                hasIpv4Address = false,
                hasIpv6Address = false,
                hasIpv4DefaultRoute = false,
                hasIpv6DefaultRoute = false
            )
        )
        assertEquals(PassiveProblemCause.ADDRESSING_SUSPECT, a.cause)
    }

    @Test fun missingDefaultRouteIsSeparateFromDns() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.UNKNOWN,
                lanState = LanState.LINK_PRESENT,
                dnsServerCount = 2,
                hasIpv4Address = true,
                hasIpv4DefaultRoute = false,
                hasIpv6DefaultRoute = false
            )
        )
        assertEquals(PassiveProblemCause.ROUTE_CONFIGURATION_SUSPECT, a.cause)
    }
}
