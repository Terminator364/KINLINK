package com.terminator364.kinlink.data

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Privacy-safe structured diagnostic bundle.
 *
 * This intentionally exports only fields already approved by DiagnosticSummary.
 * It never serializes NetworkTruth wholesale, so raw interface/gateway values
 * cannot leak into the bundle accidentally.
 */
object DiagnosticBundleBuilder {
    const val FORMAT_VERSION = 1

    fun write(zipFile: File, summary: DiagnosticSummary) {
        zipFile.parentFile?.mkdirs()
        FileOutputStream(zipFile, false).use { output ->
            ZipOutputStream(output).use { zip ->
                entry(zip, "manifest.json", manifestJson(summary))
                entry(zip, "summary.json", summaryJson(summary))
                entry(zip, "recent_actions.jsonl", recentActionsJsonl(summary))
                entry(zip, "report.txt", DiagnosticReportBuilder.build(summary))
            }
        }
    }

    private fun entry(zip: ZipOutputStream, name: String, text: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(text.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    internal fun manifestJson(summary: DiagnosticSummary): String =
        "{" +
            "\"product\":\"KINLINK\"," +
            "\"format_version\":$FORMAT_VERSION," +
            "\"generated_at_ms\":${summary.generatedAtMillis}," +
            "\"privacy\":\"no_ssid_sim_ip_gateway_payload_token\"" +
        "}\n"

    internal fun summaryJson(summary: DiagnosticSummary): String {
        val truth = summary.currentTruth
        return "{" +
            "\"generated_at_ms\":${summary.generatedAtMillis}," +
            "\"total_events\":${summary.totalEvents}," +
            "\"transport\":\"${escape(truth.transport.name)}\"," +
            "\"internet_state\":\"${escape(truth.internetState.name)}\"," +
            "\"lan_state\":\"${escape(truth.lanState.name)}\"," +
            "\"budget_state\":\"${escape(truth.budgetState.name)}\"," +
            "\"metered\":${truth.metered}," +
            "\"dns_server_count\":${truth.dnsServerCount}," +
            "\"private_dns_active\":${truth.privateDnsActive}," +
            "\"ipv4_address_present\":${truth.hasIpv4Address}," +
            "\"ipv6_address_present\":${truth.hasIpv6Address}," +
            "\"ipv4_default_route_present\":${truth.hasIpv4DefaultRoute}," +
            "\"ipv6_default_route_present\":${truth.hasIpv6DefaultRoute}," +
            "\"instability_score\":${summary.instabilityScore}," +
            "\"flapping\":${summary.flapping}," +
            "\"field_qualification\":\"${escape(summary.currentFieldQualificationVerdict)}\"," +
            "\"field_missing\":[${summary.currentFieldQualificationMissing.sorted().joinToString(",") { "\"${escape(it)}\"" }}]," +
            "\"runtime_resource_passes\":${summary.runtimeResourcePasses}," +
            "\"runtime_resource_inconclusive\":${summary.runtimeResourceInconclusive}," +
            "\"runtime_resource_blocked\":${summary.runtimeResourceBlocked}," +
            "\"handoff_events\":${summary.handoffEvents}," +
            "\"mobile_validated_outcomes\":${summary.mobileValidatedOutcomes}," +
            "\"cellular_to_wifi_returns\":${summary.cellularToWifiReturns}," +
            "\"recent24h_interruptions\":${summary.recent24hInterruptionCount}," +
            "\"recent24h_interruption_ms\":${summary.recent24hInterruptionMillis}," +
            "\"recent24h_low_quality_episodes\":${summary.recent24hLowQualityEpisodeCount}," +
            "\"recent24h_low_quality_ms\":${summary.recent24hLowQualityMillis}," +
            "\"recent24h_mobile_low_quality_episodes\":${summary.recent24hMobileLowQualityEpisodeCount}," +
            "\"recent24h_mobile_low_quality_ms\":${summary.recent24hMobileLowQualityMillis}," +
            "\"recent_mobile_assist_actions\":${summary.recentActions.count { it.action.startsWith("MOBILE_ASSIST_ACTION_") }}," +
            "\"recent_mobile_assist_outcomes\":${summary.recentActions.count { it.action.startsWith("MOBILE_ASSIST_OUTCOME_") }}" +
        "}\n"
    }

    internal fun recentActionsJsonl(summary: DiagnosticSummary): String =
        summary.recentActions.joinToString(
            separator = "\n",
            postfix = if (summary.recentActions.isEmpty()) "" else "\n"
        ) { receipt ->
            "{" +
                "\"ts_wall_ms\":${receipt.tsWallMs}," +
                "\"action\":\"${escape(receipt.action)}\"," +
                "\"success\":${receipt.success}," +
                "\"summary\":\"${escape(receipt.summary)}\"" +
            "}"
        }

    private fun escape(value: String): String = buildString(value.length + 8) {
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (ch.code < 0x20) {
                    append("\\u")
                    append(ch.code.toString(16).padStart(4, '0'))
                } else append(ch)
            }
        }
    }
}
