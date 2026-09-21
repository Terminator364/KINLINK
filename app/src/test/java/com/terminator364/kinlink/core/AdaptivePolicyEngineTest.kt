package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AdaptivePolicyEngineTest {
    @Test fun healthyWifiIsLeftAlone() {
        val decision = AdaptivePolicyEngine.evaluate(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                budgetState = BudgetState.BALANCE_UNKNOWN
            ),
            instabilityScore = 10
        )
        assertEquals(AutopilotIntent.HOLD_STEADY, decision.intent)
        assertFalse(decision.allowAutomaticProbe)
        assertFalse(decision.allowMobileAssist)
    }

    @Test fun lowMobileBudgetAlwaysWins() {
        val decision = AdaptivePolicyEngine.evaluate(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                budgetState = BudgetState.BUNDLE_LOW
            ),
            instabilityScore = 0
        )
        assertEquals(AutopilotIntent.PROTECT_MOBILE, decision.intent)
        assertFalse(decision.allowMobileAssist)
    }

    @Test fun unstableButValidatedWifiDoesNotTriggerHiddenProbe() {
        val decision = AdaptivePolicyEngine.evaluate(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED
            ),
            instabilityScore = 80
        )
        assertEquals(AutopilotIntent.OBSERVE_WIFI, decision.intent)
        assertFalse(decision.allowAutomaticProbe)
    }

    @Test fun validatedButConstrainedWifiIsNotCalledSteady() {
        val decision = AdaptivePolicyEngine.evaluate(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 600,
                upstreamKbps = 180
            ),
            instabilityScore = 5
        )
        assertEquals(AutopilotIntent.OBSERVE_WIFI, decision.intent)
        assertFalse(decision.allowAutomaticProbe)
        assertFalse(decision.allowMobileAssist)
    }

    @Test fun degradedValidatedCellularEnablesOnlyZeroProbeMobileAssist() {
        val decision = AdaptivePolicyEngine.evaluate(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 700,
                upstreamKbps = 200,
                androidNotCongested = true,
                budgetState = BudgetState.BALANCE_UNKNOWN
            ),
            instabilityScore = 5
        )
        assertEquals(AutopilotIntent.MOBILE_ASSIST, decision.intent)
        assertFalse(decision.allowAutomaticProbe)
        assertEquals(true, decision.allowMobileAssist)
    }

    @Test fun healthyValidatedCellularIsLeftSteady() {
        val decision = AdaptivePolicyEngine.evaluate(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 20_000,
                upstreamKbps = 5_000,
                budgetState = BudgetState.BALANCE_UNKNOWN
            ),
            instabilityScore = 5
        )
        assertEquals(AutopilotIntent.HOLD_STEADY, decision.intent)
        assertFalse(decision.allowAutomaticProbe)
        assertFalse(decision.allowMobileAssist)
    }

    @Test fun captivePortalRequiresUserAction() {
        val decision = AdaptivePolicyEngine.evaluate(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.CAPTIVE_PORTAL
            ),
            instabilityScore = 30
        )
        assertEquals(AutopilotIntent.CAPTIVE_PORTAL_ACTION, decision.intent)
    }
}
