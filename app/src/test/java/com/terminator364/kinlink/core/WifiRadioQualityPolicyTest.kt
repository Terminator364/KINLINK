package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class WifiRadioQualityPolicyTest {
    @Test fun classifiesWeakFairAndGood() {
        assertEquals(
            WifiRadioQuality.WEAK,
            WifiRadioQualityPolicy.assess(
                NetworkTruth(transport = Transport.WIFI, signalStrengthDbm = -82)
            ).quality
        )
        assertEquals(
            WifiRadioQuality.FAIR,
            WifiRadioQualityPolicy.assess(
                NetworkTruth(transport = Transport.WIFI, signalStrengthDbm = -70)
            ).quality
        )
        assertEquals(
            WifiRadioQuality.GOOD,
            WifiRadioQualityPolicy.assess(
                NetworkTruth(transport = Transport.WIFI, signalStrengthDbm = -55)
            ).quality
        )
    }
}
