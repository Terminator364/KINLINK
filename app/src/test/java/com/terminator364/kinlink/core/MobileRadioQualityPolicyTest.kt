package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MobileRadioQualityPolicyTest {
    private fun mobile(dbm: Int?) = NetworkTruth(
        transport = Transport.CELLULAR,
        internetState = InternetState.VALIDATED,
        signalStrengthDbm = dbm
    )

    @Test fun unknownWhenAndroidDoesNotExposeSignal() {
        assertEquals(
            MobileRadioQuality.UNKNOWN,
            MobileRadioQualityPolicy.assess(mobile(null)).quality
        )
    }

    @Test fun weakThresholdIsConservative() {
        assertEquals(
            MobileRadioQuality.WEAK,
            MobileRadioQualityPolicy.assess(mobile(-110)).quality
        )
        assertEquals(
            MobileRadioQuality.WEAK,
            MobileRadioQualityPolicy.assess(mobile(-120)).quality
        )
    }

    @Test fun strongerSignalsAreNotCalledWeak() {
        assertEquals(
            MobileRadioQuality.USABLE,
            MobileRadioQualityPolicy.assess(mobile(-105)).quality
        )
        assertEquals(
            MobileRadioQuality.STRONG,
            MobileRadioQualityPolicy.assess(mobile(-95)).quality
        )
    }

    @Test fun wifiSignalIsNeverReinterpretedAsMobileRadio() {
        assertEquals(
            MobileRadioQuality.UNKNOWN,
            MobileRadioQualityPolicy.assess(
                NetworkTruth(
                    transport = Transport.WIFI,
                    signalStrengthDbm = -120
                )
            ).quality
        )
    }
}
