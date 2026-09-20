package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiProbeHttpPolicyTest {
    @Test fun only204ConfirmsKnown204Endpoint() {
        assertTrue(WifiProbeHttpPolicy.confirmsInternet(204))
        assertFalse(WifiProbeHttpPolicy.confirmsInternet(200))
        assertFalse(WifiProbeHttpPolicy.confirmsInternet(302))
        assertFalse(WifiProbeHttpPolicy.confirmsInternet(500))
    }
}
