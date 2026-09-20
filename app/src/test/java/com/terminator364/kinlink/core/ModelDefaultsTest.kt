package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelDefaultsTest {
    @Test fun defaultTruthIsSafeOfflineUnknown() {
        val t = NetworkTruth()
        assertEquals(Transport.NONE, t.transport)
        assertEquals(InternetState.OFFLINE, t.internetState)
        assertEquals(BudgetState.BALANCE_UNKNOWN, t.budgetState)
    }

    @Test fun fingerprintChangesWhenTheInternetStateChanges() {
        val offline = NetworkTruth()
        val online = offline.copy(internetState = InternetState.VALIDATED)
        org.junit.Assert.assertNotEquals(offline.telemetryFingerprint(), online.telemetryFingerprint())
    }

    @Test fun fingerprintIsStableForEquivalentTruth() {
        val first = NetworkTruth(observedAtMillis = 1)
        val second = first.copy(observedAtMillis = 2)
        assertEquals(first.telemetryFingerprint(), second.telemetryFingerprint())
    }

    @Test fun wifiWithLocalLinkButNoWanIsNotOfflineLan() {
        val truth = ConnectivityTruthEngine.reduce(
            ConnectivityTruthEngine.Snapshot(
                transport = Transport.WIFI,
                interfaceName = "wlan0"
            )
        )
        assertEquals(InternetState.UNKNOWN, truth.internetState)
        assertEquals(LanState.LINK_PRESENT, truth.lanState)
        assertEquals(FailureDomain.ISP, truth.failureDomain)
    }

    @Test fun cellularWithNoValidatedInternetRemainsMeteredAndDoesNotInferLan() {
        val truth = ConnectivityTruthEngine.reduce(
            ConnectivityTruthEngine.Snapshot(transport = Transport.CELLULAR, metered = true)
        )
        assertEquals(LanState.UNKNOWN, truth.lanState)
        assertEquals(true, truth.metered)
        assertEquals(ContextType.MOBILE_RESILIENT, truth.context)
    }
}
