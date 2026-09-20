package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectivityTopologyTruthTest {
    @Test fun pureSnapshotCarriesTopologyBooleansWithoutAddresses() {
        val truth = ConnectivityTruthEngine.reduce(
            ConnectivityTruthEngine.Snapshot(
                transport = Transport.WIFI,
                validated = false,
                interfaceName = "wlan0",
                hasIpv4Address = true,
                hasIpv6Address = false,
                hasIpv4DefaultRoute = true,
                hasIpv6DefaultRoute = false
            )
        )
        assertTrue(truth.hasIpv4Address)
        assertTrue(truth.hasIpv4DefaultRoute)
        assertFalse(truth.hasIpv6Address)
        assertFalse(truth.hasIpv6DefaultRoute)
    }
}
