package com.terminator364.kinlink.data

import com.terminator364.kinlink.core.NetworkTruth

/** Privacy-safe diagnostic summary: no SSID, SIM identifier, IP address, gateway or payload. */
data class DiagnosticSummary(
    val generatedAtMillis: Long,
    val totalEvents: Int,
    val currentTruth: NetworkTruth,
    val stateCounts: Map<String, Int>,
    val weeklyEvents: Int = totalEvents,
    val recentTransitions: Int = 0,
    val instabilityScore: Int = 0,
    val flapping: Boolean = false
)

object DiagnosticReportBuilder {
    fun build(summary: DiagnosticSummary): String = buildString {
        appendLine("KINLINK DIAGNOSTIC")
        appendLine("Format: local observer report")
        appendLine("Generated at (UTC epoch ms): ${summary.generatedAtMillis}")
        appendLine()

        appendLine("Current state")
        appendLine("- Connection: ${transportLabel(summary.currentTruth)}")
        appendLine("- Internet: ${internetLabel(summary.currentTruth)}")
        appendLine("- Local network: ${lanLabel(summary.currentTruth)}")
        appendLine("- Mobile-data policy: ${mobileLabel(summary.currentTruth)}")
        appendLine("- Mobile budget state: ${summary.currentTruth.budgetState.name}")
        appendLine("- Explanation: ${explanation(summary.currentTruth)}")
        appendLine()

        appendLine("Recent stability")
        appendLine("- Instability score: ${summary.instabilityScore}/100")
        appendLine("- State transitions in bounded window: ${summary.recentTransitions}")
        appendLine("- Flapping detected: ${if (summary.flapping) "yes" else "no"}")
        appendLine()

        appendLine("This week")
        appendLine("- Observed network-state changes: ${summary.weeklyEvents}")
        appendLine("- Autopilot result: bounded observation/recovery only; no forced mobile routing")
        appendLine()

        appendLine("Observed state changes retained locally: ${summary.totalEvents}")
        if (summary.stateCounts.isEmpty()) {
            appendLine("- No state change recorded yet")
        } else {
            summary.stateCounts.toSortedMap().forEach { (state, count) ->
                appendLine("- $state: $count")
            }
        }

        appendLine()
        appendLine("Privacy")
        appendLine("- No SSID, SIM identifier, IP address, gateway, app traffic, password or token is included.")
        appendLine("- KINLINK did not run a speed test or an automatic mobile-data probe for this report.")
    }

    private fun transportLabel(truth: NetworkTruth) = when (truth.transport.name) {
        "WIFI" -> "Wi-Fi"
        "CELLULAR" -> "Mobile data"
        "ETHERNET" -> "Ethernet"
        "VPN" -> "VPN"
        else -> "No active connection"
    }

    private fun internetLabel(truth: NetworkTruth) = when (truth.internetState.name) {
        "VALIDATED" -> "Available"
        "CAPTIVE_PORTAL" -> "Sign-in required"
        "PARTIAL" -> "Limited"
        "OFFLINE" -> "Offline"
        else -> "Not confirmed"
    }

    private fun lanLabel(truth: NetworkTruth) = when (truth.lanState.name) {
        "HEALTHY", "LINK_PRESENT" -> "Present"
        "DOWN" -> "Unavailable"
        else -> "Not verified"
    }

    private fun mobileLabel(truth: NetworkTruth) = when {
        truth.transport.name == "WIFI" -> "Preserved while Wi-Fi is active"
        truth.metered -> "Metered connection detected; no automatic data test"
        else -> "No automatic mobile-data action"
    }

    private fun explanation(truth: NetworkTruth) = when {
        truth.internetState.name == "VALIDATED" -> "Android reports Internet access."
        truth.lanState.name == "LINK_PRESENT" ->
            "A local link exists, but Internet is not confirmed. Local LAN is kept separate."
        truth.internetState.name == "OFFLINE" -> "No active network was reported by Android."
        else -> "KINLINK is observing. It will not force a mobile-data retry."
    }
}
