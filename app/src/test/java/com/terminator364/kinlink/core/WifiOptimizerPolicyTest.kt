package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WifiOptimizerPolicyTest {
    @Test fun blocksNonWifi() {
        assertEquals(
            WifiOptimizationAction.BLOCKED_NON_WIFI,
            WifiOptimizerPolicy.action(false, false, false, true)
        )
    }

    @Test fun captivePortalHasPriority() {
        assertEquals(
            WifiOptimizationAction.CAPTIVE_PORTAL_REQUIRED,
            WifiOptimizerPolicy.action(true, false, true, null)
        )
    }

    @Test fun androidValidatedCannotBeOverruledByFailedProbe() {
        assertEquals(
            WifiOptimizationAction.KEEP_VALIDATED_AND_REFRESH,
            WifiOptimizerPolicy.action(true, true, false, false)
        )
    }

    @Test fun unvalidatedWifiCanBeConfirmedByProbe() {
        assertEquals(
            WifiOptimizationAction.CONFIRM_AND_REFRESH,
            WifiOptimizerPolicy.action(true, false, false, true)
        )
    }

    @Test fun coherentNegativeEvidenceRequestsRevalidation() {
        val action = WifiOptimizerPolicy.action(true, false, false, false)
        assertEquals(WifiOptimizationAction.REVALIDATE_AND_REFRESH, action)
        assertEquals(false, WifiOptimizerPolicy.connectivityReport(action))
    }

    @Test fun validatedAndroidSendsNoContradictoryNegativeHint() {
        val action = WifiOptimizerPolicy.action(true, true, false, false)
        assertNull(WifiOptimizerPolicy.connectivityReport(action))
    }
}
