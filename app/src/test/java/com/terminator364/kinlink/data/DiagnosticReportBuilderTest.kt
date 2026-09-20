package com.terminator364.kinlink.data

import com.terminator364.kinlink.core.InternetState
import com.terminator364.kinlink.core.LanState
import com.terminator364.kinlink.core.NetworkTruth
import com.terminator364.kinlink.core.Transport
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticReportBuilderTest {
    @Test fun reportExplainsLocalLinkWithoutExposingNetworkIdentifiers() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.UNKNOWN,
            lanState = LanState.LINK_PRESENT,
            interfaceName = "wlan0",
            gateway = "192.168.1.1"
        )
        val report = DiagnosticReportBuilder.build(
            DiagnosticSummary(1L, 3, truth, mapOf("WIFI_UNKNOWN" to 3))
        )

        assertTrue(report.contains("A local link exists"))
        assertTrue(report.contains("WIFI_UNKNOWN: 3"))
        assertFalse(report.contains("wlan0"))
        assertFalse(report.contains("192.168.1.1"))
    }

    @Test fun reportStatesThatNoAutomaticMobileProbeWasUsed() {
        val report = DiagnosticReportBuilder.build(
            DiagnosticSummary(
                1L,
                0,
                NetworkTruth(transport = Transport.CELLULAR, metered = true),
                emptyMap()
            )
        )

        assertTrue(report.contains("no automatic data test"))
        assertTrue(report.contains("did not run a speed test"))
    }
}
