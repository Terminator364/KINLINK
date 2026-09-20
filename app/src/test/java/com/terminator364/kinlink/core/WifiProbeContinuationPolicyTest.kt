package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiProbeContinuationPolicyTest {
    @Test fun onlySameActiveWifiMayContinue() {
        assertTrue(WifiProbeContinuationPolicy.mayContinue(true, true))
        assertFalse(WifiProbeContinuationPolicy.mayContinue(false, true))
        assertFalse(WifiProbeContinuationPolicy.mayContinue(true, false))
        assertFalse(WifiProbeContinuationPolicy.mayContinue(false, false))
    }
}
