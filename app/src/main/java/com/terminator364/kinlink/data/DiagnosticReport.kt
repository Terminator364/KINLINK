package com.terminator364.kinlink.data

import com.terminator364.kinlink.core.NetworkTruth
import com.terminator364.kinlink.core.PassiveLinkQualityPolicy
import com.terminator364.kinlink.core.PassiveProblemClassifier
import com.terminator364.kinlink.core.PassiveGuidancePolicy
import com.terminator364.kinlink.core.SessionHealthPolicy

/** Privacy-safe diagnostic summary: no SSID, SIM identifier, IP address, gateway or payload. */
data class DiagnosticSummary(
    val generatedAtMillis: Long,
    val totalEvents: Int,
    val currentTruth: NetworkTruth,
    val stateCounts: Map<String, Int>,
    val qualityCounts: Map<String, Int> = emptyMap(),
    val weeklyEvents: Int = totalEvents,
    val recentTransitions: Int = 0,
    val instabilityScore: Int = 0,
    val flapping: Boolean = false,
    val recentActions: List<ActionReceipt> = emptyList(),
    val handoffEvents: Int = 0,
    val mobileValidatedOutcomes: Int = 0,
    val mobilePendingOutcomes: Int = 0,
    val watchdogAborts: Int = 0,
    val recoveryMode: String = "UNKNOWN",
    val recoveryImproved: Int = 0,
    val recoveryUnchanged: Int = 0,
    val recoveryDegraded: Int = 0,
    val recoveryInconclusive: Int = 0,
    val microInterruptions: Int = 0,
    val shortInterruptions: Int = 0,
    val longInterruptions: Int = 0,
    val totalInterruptionMillis: Long = 0L,
    val longestInterruptionMillis: Long = 0L,
    val passiveCauseCounts: Map<String, Int> = emptyMap(),
    val coreSelfTestPasses: Int = 0,
    val observerSelfTestPasses: Int = 0,
    val runtimeBudgetSessions: Int = 0
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
        val passiveQuality = PassiveLinkQualityPolicy.assess(summary.currentTruth)
        val passiveProblem = PassiveProblemClassifier.classify(
            summary.currentTruth,
            summary.instabilityScore,
            summary.flapping
        )
        appendLine("- Recovery mode: ${summary.recoveryMode}")
        appendLine("- Android passive capacity: down=${summary.currentTruth.downstreamKbps} kbps, up=${summary.currentTruth.upstreamKbps} kbps")
        appendLine("- Passive quality: ${passiveQuality.quality.name} — ${passiveQuality.summary}")
        val guidance = PassiveGuidancePolicy.guidance(passiveProblem)
        val sessionHealth = SessionHealthPolicy.assess(
            summary.currentTruth,
            summary.instabilityScore,
            summary.flapping,
            passiveProblem
        )
        appendLine("- Passive cause: ${passiveProblem.cause.name} — ${passiveProblem.summary} (confidence ${passiveProblem.confidence}%)")
        appendLine("- Suggested handling: ${guidance.title} — ${guidance.message}")
        appendLine("- Session health: ${sessionHealth.health.name} — ${sessionHealth.summary}")
        appendLine("- Android DNS servers exposed: ${summary.currentTruth.dnsServerCount}")
        appendLine("- Android private DNS active: ${summary.currentTruth.privateDnsActive}")
        appendLine()

        appendLine("Runtime self-test evidence")
        appendLine("- Core self-test PASS receipts: ${summary.coreSelfTestPasses}")
        appendLine("- Observer callback self-test PASS receipts: ${summary.observerSelfTestPasses}")
        appendLine("- Completed runtime budget sessions: ${summary.runtimeBudgetSessions}")
        appendLine()

        appendLine("Recent stability")
        appendLine("- Instability score: ${summary.instabilityScore}/100")
        appendLine("- State transitions in bounded window: ${summary.recentTransitions}")
        appendLine("- Flapping detected: ${if (summary.flapping) "yes" else "no"}")
        appendLine()

        appendLine("Passive cause transitions")
        if (summary.passiveCauseCounts.isEmpty()) {
            appendLine("- No passive cause transition retained yet")
        } else {
            summary.passiveCauseCounts.toSortedMap().forEach { (cause, count) ->
                appendLine("- $cause: $count")
            }
        }
        appendLine()

        appendLine("Observed interruptions")
        appendLine("- Micro (<2 s): ${summary.microInterruptions}")
        appendLine("- Short (2–30 s): ${summary.shortInterruptions}")
        appendLine("- Long (>=30 s): ${summary.longInterruptions}")
        appendLine("- Cumulative interruption time: ${summary.totalInterruptionMillis} ms")
        appendLine("- Longest retained interruption: ${summary.longestInterruptionMillis} ms")
        appendLine()

        appendLine("Recovery effectiveness")
        appendLine("- Improved: ${summary.recoveryImproved}")
        appendLine("- Unchanged: ${summary.recoveryUnchanged}")
        appendLine("- Degraded: ${summary.recoveryDegraded}")
        appendLine("- Inconclusive: ${summary.recoveryInconclusive}")
        appendLine()

        appendLine("Handoff evidence")
        appendLine("- Handoff events retained: ${summary.handoffEvents}")
        appendLine("- Mobile handoffs validated by Android: ${summary.mobileValidatedOutcomes}")
        appendLine("- Mobile present but not yet validated receipts: ${summary.mobilePendingOutcomes}")
        appendLine("- Watchdog/transport abort receipts: ${summary.watchdogAborts}")
        appendLine()

        appendLine("This week")
        appendLine("- Observed network-state changes: ${summary.weeklyEvents}")
        appendLine("- Autopilot result: bounded observation/recovery only; no forced mobile routing")
        appendLine()

        appendLine("Recent KINLINK actions")
        if (summary.recentActions.isEmpty()) {
            appendLine("- No explicit KINLINK action recorded yet")
        } else {
            summary.recentActions.forEach { receipt ->
                appendLine("- ${receipt.tsWallMs} · ${receipt.action} · ${if (receipt.success) "PASS" else "FAIL"} · ${receipt.summary}")
            }
        }
        appendLine()

        appendLine("Passive quality history")
        if (summary.qualityCounts.isEmpty()) {
            appendLine("- No passive quality history yet")
        } else {
            summary.qualityCounts.toSortedMap().forEach { (quality, count) ->
                appendLine("- $quality: $count")
            }
        }
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
        appendLine("- Passive bandwidth values come from Android NetworkCapabilities estimates, not a speed test.")
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
