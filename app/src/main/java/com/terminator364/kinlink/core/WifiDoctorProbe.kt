package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.HttpURLConnection
import java.net.URL

object WifiProbeHttpPolicy {
    fun confirmsInternet(code: Int): Boolean = code == 204
}

data class WifiProbeResult(
    val success: Boolean,
    val summary: String,
    val latencyMillis: Long?,
    val attempts: Int = 0,
    val successes: Int = 0,
    val failures: Int = 0
)

/**
 * Explicit and bounded Wi-Fi-only micro-diagnostic.
 *
 * The probe is never run on cellular and never runs automatically in the background.
 * A fallback endpoint exists so one blocked/unreachable test server cannot become a
 * false diagnosis of a broken Internet connection.
 */
class WifiDoctorProbe(private val context: Context) {
    private data class Endpoint(val label: String, val url: String)

    private val endpoints = listOf(
        Endpoint("primary", "https://connectivitycheck.gstatic.com/generate_204"),
        Endpoint("fallback", "https://cp.cloudflare.com/generate_204")
    )

    fun run(): WifiProbeResult {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork
            ?: return WifiProbeResult(false, "Aucun réseau actif", null)

        val caps = cm.getNetworkCapabilities(network)
        if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) != true) {
            return WifiProbeResult(
                false,
                "Diagnostic bloqué : KINLINK ne teste jamais les données mobiles",
                null
            )
        }

        var attempts = 0
        var failures = 0
        var lastLatency: Long? = null
        var lastFailure = "non confirmé"

        for (endpoint in endpoints) {
            val stillActive = cm.activeNetwork == network
            val currentCaps = cm.getNetworkCapabilities(network)
            val stillWifi = currentCaps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            if (!WifiProbeContinuationPolicy.mayContinue(stillActive, stillWifi)) {
                return WifiProbeResult(
                    false,
                    "Diagnostic interrompu : le Wi-Fi actif a changé pendant le handoff.",
                    lastLatency,
                    attempts = attempts,
                    successes = 0,
                    failures = failures
                )
            }

            attempts += 1
            val started = System.nanoTime()
            val attempt = runCatching {
                val connection = network.openConnection(URL(endpoint.url)) as HttpURLConnection
                connection.connectTimeout = 1_600
                connection.readTimeout = 1_600
                connection.instanceFollowRedirects = false
                connection.requestMethod = "GET"
                connection.useCaches = false
                val code = connection.responseCode
                connection.disconnect()
                val elapsed = (System.nanoTime() - started) / 1_000_000
                lastLatency = elapsed

                if (WifiProbeHttpPolicy.confirmsInternet(code)) {
                    WifiProbeResult(
                        true,
                        "Accès Internet confirmé par un micro-test borné en $elapsed ms",
                        elapsed,
                        attempts = attempts,
                        successes = 1,
                        failures = failures
                    )
                } else {
                    failures += 1
                    lastFailure = if (code in 200..399) "réponse HTTP $code (portail/proxy possible)" else "réponse HTTP $code"
                    null
                }
            }.getOrElse {
                failures += 1
                lastLatency = (System.nanoTime() - started) / 1_000_000
                lastFailure = it.javaClass.simpleName
                null
            }

            if (attempt != null) return attempt
        }

        return WifiProbeResult(
            false,
            "Les $attempts micro-tests n’ont pas confirmé Internet ($lastFailure)",
            lastLatency,
            attempts = attempts,
            successes = 0,
            failures = failures
        )
    }
}
