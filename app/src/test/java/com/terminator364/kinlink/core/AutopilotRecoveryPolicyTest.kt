package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AutopilotRecoveryPolicyTest {
    @Test fun neverActsOnCellular() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(transport = Transport.CELLULAR, internetState = InternetState.UNKNOWN),
            AutopilotProfile.MAXIMUM_STABILITY, 100, false, 0, Long.MAX_VALUE
        )
        assertEquals(AutomaticRecoveryAction.NONE, d.action)
    }

    @Test fun meteredWifiNeverGetsAutomaticProbe() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(transport = Transport.WIFI, internetState = InternetState.UNKNOWN, lanState = LanState.LINK_PRESENT, metered = true),
            AutopilotProfile.BALANCED, 70, false, 0, Long.MAX_VALUE
        )
        assertEquals(AutomaticRecoveryAction.NONE, d.action)
    }

    @Test fun unvalidatedUnmeteredWifiCanBeConfirmed() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(transport = Transport.WIFI, internetState = InternetState.UNKNOWN, lanState = LanState.LINK_PRESENT, metered = false),
            AutopilotProfile.BALANCED, 60, false, 0, Long.MAX_VALUE
        )
        assertEquals(AutomaticRecoveryAction.CONFIRM_WIFI, d.action)
    }

    @Test fun stableValidatedWifiIsLeftAlone() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(transport = Transport.WIFI, internetState = InternetState.VALIDATED, lanState = LanState.LINK_PRESENT),
            AutopilotProfile.BALANCED, 10, false, 0, Long.MAX_VALUE
        )
        assertEquals(AutomaticRecoveryAction.NONE, d.action)
    }

    @Test fun unstableValidatedWifiOnlyRefreshesMetrics() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(transport = Transport.WIFI, internetState = InternetState.VALIDATED, lanState = LanState.LINK_PRESENT),
            AutopilotProfile.MAXIMUM_STABILITY, 70, false, 0, Long.MAX_VALUE
        )
        assertEquals(AutomaticRecoveryAction.REFRESH_METRICS, d.action)
    }

    @Test fun persistentLowQualityValidatedWifiRefreshesOnlyMetrics() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                lanState = LanState.LINK_PRESENT,
                downstreamKbps = 2_000,
                upstreamKbps = 700
            ),
            AutopilotProfile.BALANCED,
            10,
            false,
            0,
            Long.MAX_VALUE,
            persistentLowQuality = true
        )
        assertEquals(AutomaticRecoveryAction.REFRESH_METRICS, d.action)
    }

    @Test fun singleLowQualityObservationDoesNotAct() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 2_000,
                upstreamKbps = 700
            ),
            AutopilotProfile.BALANCED,
            10,
            false,
            0,
            Long.MAX_VALUE,
            persistentLowQuality = false
        )
        assertEquals(AutomaticRecoveryAction.NONE, d.action)
    }

    @Test fun weakRadioSuppressesPointlessRecovery() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                signalStrengthDbm = -82,
                downstreamKbps = 20_000,
                upstreamKbps = 5_000
            ),
            AutopilotProfile.MAXIMUM_STABILITY,
            90,
            false,
            0,
            Long.MAX_VALUE,
            persistentLowQuality = true
        )
        assertEquals(AutomaticRecoveryAction.NONE, d.action)
    }

    @Test fun congestionSuspicionSuppressesRepeatedRefresh() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                signalStrengthDbm = -55,
                androidNotCongested = false,
                downstreamKbps = 2_000,
                upstreamKbps = 700
            ),
            AutopilotProfile.MAXIMUM_STABILITY,
            90,
            false,
            0,
            Long.MAX_VALUE,
            persistentLowQuality = true
        )
        assertEquals(AutomaticRecoveryAction.NONE, d.action)
    }

    @Test fun resourceConstraintBlocksRecovery() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(transport = Transport.WIFI, internetState = InternetState.UNKNOWN, lanState = LanState.LINK_PRESENT),
            AutopilotProfile.MAXIMUM_STABILITY, 100, true, 0, Long.MAX_VALUE
        )
        assertEquals(AutomaticRecoveryAction.NONE, d.action)
    }

    @Test fun repeatedSafetyAbortsOpenCircuitBreaker() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.UNKNOWN,
                lanState = LanState.LINK_PRESENT
            ),
            AutopilotProfile.MAXIMUM_STABILITY,
            90,
            false,
            0,
            Long.MAX_VALUE,
            persistentLowQuality = false,
            recentIneffectiveOutcomes = 0,
            recentSafetyAborts = 2
        )
        assertEquals(AutomaticRecoveryAction.NONE, d.action)
    }

    @Test fun repeatedIneffectiveOutcomesStopAutomaticRecovery() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 600,
                upstreamKbps = 180
            ),
            AutopilotProfile.MAXIMUM_STABILITY,
            90,
            false,
            0,
            Long.MAX_VALUE,
            persistentLowQuality = true,
            recentIneffectiveOutcomes = 2
        )
        assertEquals(AutomaticRecoveryAction.NONE, d.action)
    }

    @Test fun hourlyCapStopsRunawayRecovery() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(transport = Transport.WIFI, internetState = InternetState.UNKNOWN, lanState = LanState.LINK_PRESENT),
            AutopilotProfile.BALANCED, 100, false, 4, Long.MAX_VALUE
        )
        assertEquals(AutomaticRecoveryAction.NONE, d.action)
    }
}
