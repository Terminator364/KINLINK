package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CriticalScenarioMatrixTest {
    private fun decide(
        truth: NetworkTruth,
        instabilityScore: Int = 0,
        resourceConstrained: Boolean = false
    ): AutomaticRecoveryDecision =
        AutopilotRecoveryPolicy.decide(
            truth = truth,
            profile = AutopilotProfile.BALANCED,
            instabilityScore = instabilityScore,
            resourceConstrained = resourceConstrained,
            recentAutomaticActions = 0,
            millisSinceLastAutomaticAction = Long.MAX_VALUE,
            persistentLowQuality = false,
            recentIneffectiveOutcomes = 0,
            recentSafetyAborts = 0
        )

    @Test fun cellularIsObservationOnlyAcrossCentralAndAutopilotGates() {
        val truth = NetworkTruth(
            transport = Transport.CELLULAR,
            internetState = InternetState.VALIDATED
        )
        assertFalse(ActiveRecoveryPolicy.allowed(RecoveryMode.AUTOMATIC, Transport.CELLULAR))
        assertEquals(AutomaticRecoveryAction.NONE, decide(truth).action)
    }

    @Test fun observationOnlyBlocksEvenHealthyWifiRecoverySurface() {
        assertFalse(
            ActiveRecoveryPolicy.allowed(
                RecoveryMode.OBSERVATION_ONLY,
                Transport.WIFI
            )
        )
    }

    @Test fun meteredWifiNeverAuthorizesAutomaticConfirmationProbe() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.UNKNOWN,
            lanState = LanState.LINK_PRESENT,
            metered = true
        )
        assertEquals(AutomaticRecoveryAction.NONE, decide(truth).action)
    }

    @Test fun unmeteredLanWithoutWanCanUseBoundedWifiConfirmation() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.UNKNOWN,
            lanState = LanState.LINK_PRESENT,
            metered = false
        )
        assertEquals(AutomaticRecoveryAction.CONFIRM_WIFI, decide(truth).action)
    }

    @Test fun captivePortalNeverTriggersAutomaticRecovery() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.CAPTIVE_PORTAL,
            lanState = LanState.LINK_PRESENT
        )
        assertEquals(AutomaticRecoveryAction.NONE, decide(truth).action)
    }

    @Test fun healthyValidatedWifiIsLeftAlone() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.VALIDATED,
            lanState = LanState.HEALTHY,
            downstreamKbps = 50_000,
            upstreamKbps = 10_000
        )
        assertEquals(AutomaticRecoveryAction.NONE, decide(truth).action)
    }

    @Test fun unstableValidatedWifiMayRefreshMetricsWithoutProbe() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.VALIDATED,
            lanState = LanState.HEALTHY,
            downstreamKbps = 50_000,
            upstreamKbps = 10_000
        )
        assertEquals(
            AutomaticRecoveryAction.REFRESH_METRICS,
            decide(truth, instabilityScore = 80).action
        )
    }

    @Test fun resourcePressureSuppressesAutomaticRecovery() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.VALIDATED,
            lanState = LanState.HEALTHY
        )
        assertEquals(
            AutomaticRecoveryAction.NONE,
            decide(truth, instabilityScore = 90, resourceConstrained = true).action
        )
    }

    @Test fun lanWithoutWanRemainsVisibleAsLanInsteadOfOffline() {
        val assessment = ConnectivityStateClassifier.classify(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.UNKNOWN,
                lanState = LanState.LINK_PRESENT
            )
        )
        assertEquals(OperationalState.LAN_OK_WAN_DOWN, assessment.state)
        assertTrue(assessment.preserveLan)
        assertTrue(assessment.avoidAutomaticMobileUse)
    }

    @Test fun exhaustedMobileBudgetNeverInvitesAutomaticDataUse() {
        val assessment = ConnectivityStateClassifier.classify(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                budgetState = BudgetState.BUNDLE_EXHAUSTED
            )
        )
        assertEquals(OperationalState.DATA_EXHAUSTED, assessment.state)
        assertTrue(assessment.avoidAutomaticMobileUse)
    }
}
