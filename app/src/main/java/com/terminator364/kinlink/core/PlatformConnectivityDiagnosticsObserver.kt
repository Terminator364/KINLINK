package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityDiagnosticsManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest

data class PlatformDiagnosticEvent(
    val action: String,
    val success: Boolean,
    val summary: String
)

object PlatformDiagnosticPolicy {
    fun shouldPersistConnectivityReport(
        validated: Boolean,
        captivePortal: Boolean,
        notSuspended: Boolean
    ): Boolean = !validated || captivePortal || !notSuspended

    fun stallMethodLabel(method: Int): String {
        val parts = mutableListOf<String>()
        if (
            method and ConnectivityDiagnosticsManager.DataStallReport.DETECTION_METHOD_DNS_EVENTS != 0
        ) {
            parts += "DNS"
        }
        if (
            method and ConnectivityDiagnosticsManager.DataStallReport.DETECTION_METHOD_TCP_METRICS != 0
        ) {
            parts += "TCP"
        }
        return if (parts.isEmpty()) "UNKNOWN" else parts.joinToString("+")
    }
}

/**
 * Zero-extra-traffic observer for Android platform connectivity diagnostics.
 *
 * KINLINK only consumes reports already produced by Android. It never reports
 * connectivity back to the framework and never starts an HTTP/DNS/speed test.
 */
class PlatformConnectivityDiagnosticsObserver(
    context: Context,
    private val onEvent: (PlatformDiagnosticEvent) -> Unit
) {
    private val appContext = context.applicationContext
    private val manager =
        appContext.getSystemService(ConnectivityDiagnosticsManager::class.java)
    private var registered = false

    private val callback =
        object : ConnectivityDiagnosticsManager.ConnectivityDiagnosticsCallback() {
            override fun onConnectivityReportAvailable(
                report: ConnectivityDiagnosticsManager.ConnectivityReport
            ) {
                val caps = report.networkCapabilities
                val validated =
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val captive =
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)
                val notSuspended =
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)

                if (
                    !PlatformDiagnosticPolicy.shouldPersistConnectivityReport(
                        validated,
                        captive,
                        notSuspended
                    )
                ) {
                    return
                }

                onEvent(
                    PlatformDiagnosticEvent(
                        action = "PLATFORM_CONNECTIVITY_REPORT",
                        success = validated && !captive && notSuspended,
                        summary =
                            "transport=${transportLabel(caps)}; validated=$validated; " +
                                "captive=$captive; notSuspended=$notSuspended; " +
                                "source=Android ConnectivityDiagnostics; no KINLINK probe"
                    )
                )
            }

            override fun onDataStallSuspected(
                report: ConnectivityDiagnosticsManager.DataStallReport
            ) {
                val caps = report.networkCapabilities
                onEvent(
                    PlatformDiagnosticEvent(
                        action = "PLATFORM_DATA_STALL",
                        success = false,
                        summary =
                            "method=${PlatformDiagnosticPolicy.stallMethodLabel(report.detectionMethod)}; " +
                                "transport=${transportLabel(caps)}; " +
                                "validated=${caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}; " +
                                "source=Android ConnectivityDiagnostics; no KINLINK probe"
                    )
                )
            }
        }

    fun start(): Boolean {
        if (registered) return true
        return runCatching {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            manager.registerConnectivityDiagnosticsCallback(
                request,
                appContext.mainExecutor,
                callback
            )
            registered = true
            true
        }.getOrDefault(false)
    }

    fun stop() {
        if (!registered) return
        runCatching { manager.unregisterConnectivityDiagnosticsCallback(callback) }
        registered = false
    }

    private fun transportLabel(caps: NetworkCapabilities): String = when {
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
        else -> "OTHER"
    }
}
