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

    @Test fun reportIncludesBoundedActionReceipts() {
        val report = DiagnosticReportBuilder.build(
            DiagnosticSummary(
                generatedAtMillis = 1L,
                totalEvents = 1,
                currentTruth = NetworkTruth(transport = Transport.WIFI),
                stateCounts = emptyMap(),
                recentActions = listOf(
                    ActionReceipt(
                        tsWallMs = 123L,
                        action = "WIFI_OPTIMIZE",
                        success = true,
                        summary = "Android validated network retained"
                    )
                )
            )
        )

        assertTrue(report.contains("WIFI_OPTIMIZE"))
        assertTrue(report.contains("PASS"))
        assertTrue(report.contains("Android validated network retained"))
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
    @Test fun reportSeparatesMobileSlowLinkBurdenFromWifi() {
        val report = DiagnosticReportBuilder.build(
            DiagnosticSummary(
                generatedAtMillis = 1L,
                totalEvents = 0,
                currentTruth = NetworkTruth(
                    transport = Transport.CELLULAR,
                    internetState = InternetState.VALIDATED
                ),
                stateCounts = emptyMap(),
                recent24hMobileLowQualityEpisodeCount = 2,
                recent24hMobileLowQualityMillis = 6_000L,
                recent24hMobileLowQualityLongestMillis = 4_000L,
                mobileLowQualityEpisodes = 2,
                totalMobileLowQualityMillis = 6_000L,
                longestMobileLowQualityMillis = 4_000L
            )
        )
        assertTrue(report.contains("Slow-but-validated mobile 24h: 2"))
        assertTrue(report.contains("Observed slow-but-validated mobile episodes"))
    }

}
