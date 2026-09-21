package com.terminator364.kinlink.core

import android.net.ConnectivityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformCapabilityPolicyTest {
    @Test fun android17LocalNetworkPermissionIsExplicit() {
        assertEquals(
            LocalNetworkAccessState.NOT_GRANTED,
            PlatformCapabilityPolicy.localNetworkAccessState(37, false)
        )
        assertEquals(
            LocalNetworkAccessState.GRANTED,
            PlatformCapabilityPolicy.localNetworkAccessState(37, true)
        )
    }

    @Test fun olderAndroidDoesNotInventNewRuntimePermission() {
        assertEquals(
            LocalNetworkAccessState.NOT_REQUIRED_PRE_API_37,
            PlatformCapabilityPolicy.localNetworkAccessState(36, false)
        )
    }

    @Test fun vpnExcludeRouteNeedsApi33() {
        assertFalse(PlatformCapabilityPolicy.vpnExcludeRouteSupported(32))
        assertTrue(PlatformCapabilityPolicy.vpnExcludeRouteSupported(33))
    }

    @Test fun dataSaverMapsToExplicitStates() {
        assertEquals(
            DataSaverState.ENABLED,
            PlatformCapabilityPolicy.dataSaverState(
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
            )
        )
        assertEquals(
            DataSaverState.DISABLED,
            PlatformCapabilityPolicy.dataSaverState(
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED
            )
        )
    }

    @Test fun liteObserverNeverPretendsConnectivityDiagnosticsEligibility() {
        assertFalse(PlatformCapabilityPolicy.connectivityDiagnosticsEligibleInLite())
    }
}
