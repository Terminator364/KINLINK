package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SuspendedNetworkPolicyTest {
    @Test fun suspendedWifiIsDiagnosed() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.VALIDATED,
            androidNotSuspended = false,
            signalStrengthDbm = -55,
            downstreamKbps = 20_000,
            upstreamKbps = 5_000
        )
        assertEquals(
            PassiveProblemCause.NETWORK_SUSPENDED,
            PassiveProblemClassifier.classify(truth).cause
        )
    }

    @Test fun autopilotDoesNotActOnSuspendedWifi() {
        val d = AutopilotRecoveryPolicy.decide(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                androidNotSuspended = false,
                downstreamKbps = 500,
                upstreamKbps = 100
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
}
