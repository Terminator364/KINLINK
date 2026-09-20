package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PassiveProblemClassifierTest {
    @Test fun detectsDnsConfigurationSuspicionWithoutProbe() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.UNKNOWN,
                lanState = LanState.LINK_PRESENT,
                dnsServerCount = 0,
                hasIpv4Address = true,
                hasIpv4DefaultRoute = true
            )
        )
        assertEquals(PassiveProblemCause.DNS_CONFIGURATION_SUSPECT, a.cause)
    }

    @Test fun doesNotCallDnsBrokenWhenAndroidProvidesDns() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.UNKNOWN,
                lanState = LanState.LINK_PRESENT,
                dnsServerCount = 2,
                hasIpv4Address = true,
                hasIpv4DefaultRoute = true
            )
        )
        assertEquals(PassiveProblemCause.WAN_UNVALIDATED, a.cause)
    }

    @Test fun constrainedValidatedWifiIsLowCapacity() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                lanState = LanState.LINK_PRESENT,
                downstreamKbps = 700,
                upstreamKbps = 180,
                dnsServerCount = 2
            )
        )
        assertEquals(PassiveProblemCause.LOW_CAPACITY, a.cause)
    }

    @Test fun mobileUnvalidatedRemainsObservationOnlyDiagnosis() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.UNKNOWN
            )
        )
        assertEquals(PassiveProblemCause.MOBILE_UNVALIDATED, a.cause)
    }
}
