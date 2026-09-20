package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class WifiOptimizerPolicyTest {
    @Test fun blocksNonWifi() {
        assertEquals(
            WifiOptimizationAction.BLOCKED_NON_WIFI,
            WifiOptimizerPolicy.action(isWifi = false, probeSucceeded = true)
        )
    }

    @Test fun confirmsHealthyWifi() {
        assertEquals(
            WifiOptimizationAction.CONFIRM_AND_REFRESH,
            WifiOptimizerPolicy.action(isWifi = true, probeSucceeded = true)
        )
    }

    @Test fun revalidatesFailingWifi() {
        assertEquals(
            WifiOptimizationAction.REVALIDATE_AND_REFRESH,
            WifiOptimizerPolicy.action(isWifi = true, probeSucceeded = false)
        )
    }
}
