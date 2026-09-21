package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiOptimizationContinuationPolicyTest {
    @Test fun refreshRequiresSameActiveWifi() {
        assertTrue(WifiOptimizationContinuationPolicy.mayRefresh(true, true))
        assertFalse(WifiOptimizationContinuationPolicy.mayRefresh(false, true))
        assertFalse(WifiOptimizationContinuationPolicy.mayRefresh(true, false))
        assertFalse(WifiOptimizationContinuationPolicy.mayRefresh(false, false))
    }
}
