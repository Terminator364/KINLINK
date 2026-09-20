package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FrameworkInfluencePolicyTest {
    @Test fun positiveProbeNeverReportsConnectivityBackToAndroid() {
        val action = WifiOptimizerPolicy.action(
            isWifi = true,
            androidValidated = false,
            captivePortal = false,
            probeSucceeded = true
        )
        assertEquals(WifiOptimizationAction.CONFIRM_AND_REFRESH, action)
        assertNull(WifiOptimizerPolicy.connectivityReport(action))
    }

    @Test fun negativeProbeNeverReportsConnectivityBackToAndroid() {
        val action = WifiOptimizerPolicy.action(
            isWifi = true,
            androidValidated = false,
            captivePortal = false,
            probeSucceeded = false
        )
        assertEquals(WifiOptimizationAction.NEGATIVE_EVIDENCE_REFRESH, action)
        assertNull(WifiOptimizerPolicy.connectivityReport(action))
    }

    @Test fun cellularAlwaysStaysOutsideWifiRecovery() {
        val decision = AutopilotRecoveryPolicy.decide(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.UNKNOWN
            ),
            AutopilotProfile.MAXIMUM_STABILITY,
            instabilityScore = 100,
            resourceConstrained = false,
            recentAutomaticActions = 0,
            millisSinceLastAutomaticAction = Long.MAX_VALUE
        )
        assertEquals(AutomaticRecoveryAction.NONE, decision.action)
    }
}
