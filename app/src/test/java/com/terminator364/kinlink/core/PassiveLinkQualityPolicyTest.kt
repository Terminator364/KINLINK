package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PassiveLinkQualityPolicyTest {
    @Test fun unknownWhenAndroidProvidesNoEstimate() {
        assertEquals(
            PassiveLinkQuality.UNKNOWN,
            PassiveLinkQualityPolicy.assess(NetworkTruth()).quality
        )
    }

    @Test fun validatedCanStillBeConstrained() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.VALIDATED,
            downstreamKbps = 700,
            upstreamKbps = 180
        )
        assertEquals(PassiveLinkQuality.CONSTRAINED, PassiveLinkQualityPolicy.assess(truth).quality)
    }

    @Test fun healthyEstimateDoesNotOverrideValidationSemantics() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.VALIDATED,
            downstreamKbps = 20_000,
            upstreamKbps = 5_000
        )
        assertEquals(PassiveLinkQuality.COMFORTABLE, PassiveLinkQualityPolicy.assess(truth).quality)
    }
}
