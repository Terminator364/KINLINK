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
    @Test fun constrainedCellularIsClassifiedSeparately() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 700,
                upstreamKbps = 200,
                androidNotCongested = true
            )
        )
        assertEquals(PassiveProblemCause.MOBILE_LOW_CAPACITY, a.cause)
    }

    @Test fun congestedCellularHasPriorityOverGenericMobileLowCapacity() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 2_000,
                upstreamKbps = 700,
                androidNotCongested = false
            )
        )
        assertEquals(PassiveProblemCause.MOBILE_CONGESTION_SUSPECT, a.cause)
    }

    @Test fun suspendedCellularIsNeverMistakenForCongestion() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 700,
                upstreamKbps = 200,
                androidNotCongested = false,
                androidNotSuspended = false
            )
        )
        assertEquals(PassiveProblemCause.MOBILE_NETWORK_SUSPENDED, a.cause)
    }

    @Test fun weakCellularSignalHasPriorityOverGenericMobileLowCapacity() {
        val a = PassiveProblemClassifier.classify(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                signalStrengthDbm = -115,
                downstreamKbps = 600,
                upstreamKbps = 200,
                androidNotCongested = false
            )
        )
        assertEquals(PassiveProblemCause.MOBILE_WEAK_SIGNAL, a.cause)
    }

}
