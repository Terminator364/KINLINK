package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PassiveRadioDiagnosisTest {
    @Test fun weakWifiSignalHasPriority() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                lanState = LanState.LINK_PRESENT,
                signalStrengthDbm = -82,
                downstreamKbps = 20_000,
                upstreamKbps = 5_000
            )
        )
        assertEquals(PassiveProblemCause.WEAK_WIFI_SIGNAL, a.cause)
    }

    @Test fun constrainedCongestedSignalIsSeparatedFromGenericLowCapacity() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                lanState = LanState.LINK_PRESENT,
                signalStrengthDbm = -60,
                androidNotCongested = false,
                downstreamKbps = 2_000,
                upstreamKbps = 700
            )
        )
        assertEquals(PassiveProblemCause.CONGESTION_SUSPECT, a.cause)
    }
}
