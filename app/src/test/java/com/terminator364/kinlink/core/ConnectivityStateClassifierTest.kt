package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectivityStateClassifierTest {
    @Test fun wifiWithValidatedInternetPreservesMobileData() {
        val result = ConnectivityStateClassifier.classify(
            NetworkTruth(transport = Transport.WIFI, internetState = InternetState.VALIDATED, lanState = LanState.LINK_PRESENT)
        )
        assertEquals(OperationalState.WIFI_HEALTHY, result.state)
        assertTrue(result.avoidAutomaticMobileUse)
    }

    @Test fun validatedButConstrainedWifiIsVisibleAsDegraded() {
        val result = ConnectivityStateClassifier.classify(
            NetworkTruth(
                transport = Transport.WIFI,
                internetState = InternetState.VALIDATED,
                lanState = LanState.LINK_PRESENT,
                downstreamKbps = 700,
                upstreamKbps = 180
            )
        )
        assertEquals(OperationalState.WIFI_DEGRADED, result.state)
        assertTrue(result.preserveLan)
        assertTrue(result.avoidAutomaticMobileUse)
    }

    @Test fun localWifiWithoutValidatedWanKeepsLanSeparate() {
        val result = ConnectivityStateClassifier.classify(
            NetworkTruth(transport = Transport.WIFI, internetState = InternetState.UNKNOWN, lanState = LanState.LINK_PRESENT)
        )
        assertEquals(OperationalState.LAN_OK_WAN_DOWN, result.state)
        assertTrue(result.preserveLan)
    }

    @Test fun exhaustedBundleIsNotClassifiedAsARecoverableNetworkFailure() {
        val result = ConnectivityStateClassifier.classify(
            NetworkTruth(transport = Transport.CELLULAR, budgetState = BudgetState.BUNDLE_EXHAUSTED)
        )
        assertEquals(OperationalState.DATA_EXHAUSTED, result.state)
        assertTrue(result.avoidAutomaticMobileUse)
    }
}
