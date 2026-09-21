package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference

object WifiProbeHttpPolicy {
    fun confirmsInternet(code: Int): Boolean = code == 204
}

object WifiProbeDeadlinePolicy {
    const val MAX_ENDPOINTS = 2
    const val CONNECT_TIMEOUT_MS = 900
    const val READ_TIMEOUT_MS = 900
    const val ATTEMPT_HARD_TIMEOUT_MS = 1_500
    const val WORST_CASE_HTTP_WAIT_MS =
        MAX_ENDPOINTS * ATTEMPT_HARD_TIMEOUT_MS

    fun fitsWithin(hardDeadlineMillis: Long): Boolean =
        WORST_CASE_HTTP_WAIT_MS < hardDeadlineMillis
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
 * The probe is never run on cellular. Each endpoint attempt has both socket-level
 * timeouts and an outer wall-clock deadline. On timeout KINLINK disconnects the
 * active HttpURLConnection before cancelling the worker so DNS/socket stalls
 * cannot hold the recovery control path past its configured envelope.
 */
class WifiDoctorProbe(private val context: Context) {
    private data class Endpoint(val label: String, val url: String)
    private data class EndpointAttempt(
        val code: Int?,
        val elapsedMillis: Long,
        val failure: String?
    )

    private val endpoints = listOf(
        Endpoint("primary", "https://connectivitycheck.gstatic.com/generate_204"),
        Endpoint("fallback", "https://cp.cloudflare.com/generate_204")
    )

    fun run(expectedNetwork: android.net.Network? = null): WifiProbeResult {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = expectedNetwork ?: cm.activeNetwork
            ?: return WifiProbeResult(false, "Aucun réseau actif", null)

        if (cm.activeNetwork != network) {
            return WifiProbeResult(
                false,
                "Diagnostic bloqué : le réseau actif a changé avant le micro-test.",
                null
            )
        }

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
            val stillWifi =
                currentCaps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
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
            val attempt = runEndpointAttempt(network, endpoint)
            lastLatency = attempt.elapsedMillis

            val code = attempt.code
            if (code != null && WifiProbeHttpPolicy.confirmsInternet(code)) {
                return WifiProbeResult(
                    true,
                    "Accès Internet confirmé par un micro-test borné en ${attempt.elapsedMillis} ms",
                    attempt.elapsedMillis,
                    attempts = attempts,
                    successes = 1,
                    failures = failures
                )
            }

            failures += 1
            lastFailure = when {
                code != null && code in 200..399 ->
                    "réponse HTTP $code (portail/proxy possible)"
                code != null -> "réponse HTTP $code"
                else -> attempt.failure ?: "non confirmé"
            }
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

    private fun runEndpointAttempt(
        network: android.net.Network,
        endpoint: Endpoint
    ): EndpointAttempt {
        val executor = Executors.newSingleThreadExecutor()
        val connectionRef = AtomicReference<HttpURLConnection?>(null)
        val started = System.nanoTime()
        val future = executor.submit<EndpointAttempt> {
            var connection: HttpURLConnection? = null
            try {
                connection =
                    network.openConnection(URL(endpoint.url)) as HttpURLConnection
                connectionRef.set(connection)
                connection.connectTimeout = WifiProbeDeadlinePolicy.CONNECT_TIMEOUT_MS
                connection.readTimeout = WifiProbeDeadlinePolicy.READ_TIMEOUT_MS
                connection.instanceFollowRedirects = false
                connection.requestMethod = "GET"
                connection.useCaches = false
                val code = connection.responseCode
                EndpointAttempt(
                    code = code,
                    elapsedMillis = elapsedMillisSince(started),
                    failure = null
                )
            } catch (t: Throwable) {
                EndpointAttempt(
                    code = null,
                    elapsedMillis = elapsedMillisSince(started),
                    failure = t.javaClass.simpleName
                )
            } finally {
                connectionRef.compareAndSet(connection, null)
                runCatching { connection?.disconnect() }
            }
        }

        return try {
            future.get(
                WifiProbeDeadlinePolicy.ATTEMPT_HARD_TIMEOUT_MS.toLong(),
                TimeUnit.MILLISECONDS
            )
        } catch (_: TimeoutException) {
            runCatching { connectionRef.getAndSet(null)?.disconnect() }
            future.cancel(true)
            EndpointAttempt(
                code = null,
                elapsedMillis = elapsedMillisSince(started),
                failure = "hard-timeout"
            )
        } catch (t: Throwable) {
            runCatching { connectionRef.getAndSet(null)?.disconnect() }
            future.cancel(true)
            EndpointAttempt(
                code = null,
                elapsedMillis = elapsedMillisSince(started),
                failure = t.javaClass.simpleName
            )
        } finally {
            executor.shutdownNow()
        }
    }

    private fun elapsedMillisSince(startedNanos: Long): Long =
        ((System.nanoTime() - startedNanos) / 1_000_000L).coerceAtLeast(0L)
}
