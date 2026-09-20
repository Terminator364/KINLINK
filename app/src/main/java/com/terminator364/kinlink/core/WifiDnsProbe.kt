package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class WifiDnsProbeResult(
    val success: Boolean,
    val latencyMillis: Long?,
    val summary: String
)

class WifiDnsProbe(
    private val context: Context,
    private val network: Network
) {
    fun run(): WifiDnsProbeResult {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val caps = cm.getNetworkCapabilities(network)
        val sameActiveWifi =
            cm.activeNetwork == network &&
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (!sameActiveWifi) {
            return WifiDnsProbeResult(
                false,
                null,
                "DNS non testé : le Wi-Fi actif a changé."
            )
        }

        val executor = Executors.newSingleThreadExecutor()
        val started = System.nanoTime()
        val future = executor.submit<Boolean> {
            runCatching {
                network.getAllByName(HOST).isNotEmpty()
            }.getOrDefault(false)
        }

        return try {
            val success = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            val elapsed = (System.nanoTime() - started) / 1_000_000L
            WifiDnsProbeResult(
                success,
                elapsed,
                if (success)
                    "Résolution DNS Wi-Fi confirmée en ${elapsed} ms."
                else
                    "Résolution DNS Wi-Fi non confirmée."
            )
        } catch (_: Exception) {
            future.cancel(true)
            val elapsed = (System.nanoTime() - started) / 1_000_000L
            WifiDnsProbeResult(
                false,
                elapsed,
                "Résolution DNS Wi-Fi non confirmée dans la fenêtre bornée."
            )
        } finally {
            executor.shutdownNow()
        }
    }

    companion object {
        private const val HOST = "connectivitycheck.gstatic.com"
        private const val TIMEOUT_MS = 1_200L
    }
}
