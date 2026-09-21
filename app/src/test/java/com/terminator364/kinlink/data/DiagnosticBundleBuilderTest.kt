package com.terminator364.kinlink.data

import com.terminator364.kinlink.core.InternetState
import com.terminator364.kinlink.core.LanState
import com.terminator364.kinlink.core.NetworkTruth
import com.terminator364.kinlink.core.Transport
import java.nio.file.Files
import java.util.zip.ZipFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticBundleBuilderTest {
    @Test fun bundleHasStructuredCoreAndDoesNotLeakRawNetworkIdentifiers() {
        val truth = NetworkTruth(
            transport = Transport.WIFI,
            internetState = InternetState.VALIDATED,
            lanState = LanState.HEALTHY,
            interfaceName = "wlan-secret",
            gateway = "192.168.77.1",
            hasIpv4Address = true,
            hasIpv4DefaultRoute = true
        )
        val summary = DiagnosticSummary(
            generatedAtMillis = 123L,
            totalEvents = 2,
            currentTruth = truth,
            stateCounts = mapOf("WIFI_VALIDATED" to 2),
            recentActions = listOf(
                ActionReceipt(100L, "TEST_ACTION", true, "privacy-safe")
            )
        )

        val file = Files.createTempFile("kinlink-diagnostic-", ".zip").toFile()
        try {
            DiagnosticBundleBuilder.write(file, summary)
            ZipFile(file).use { zip ->
                val names = zip.entries().asSequence().map { it.name }.toSet()
                assertTrue(names.contains("manifest.json"))
                assertTrue(names.contains("summary.json"))
                assertTrue(names.contains("recent_actions.jsonl"))
                assertTrue(names.contains("report.txt"))

                val text = names.joinToString("\n") { name ->
                    zip.getInputStream(zip.getEntry(name)).bufferedReader().use { it.readText() }
                }
                assertFalse(text.contains("wlan-secret"))
                assertFalse(text.contains("192.168.77.1"))
                assertTrue(text.contains("\"ipv4_address_present\":true"))
                assertTrue(text.contains("TEST_ACTION"))
            }
        } finally {
            file.delete()
        }
    }
}
