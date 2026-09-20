package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.HttpURLConnection
import java.net.URL

data class WifiProbeResult(val success: Boolean, val summary: String, val latencyMillis: Long?)

/** Explicit, bounded Wi-Fi-only micro-diagnostic. Never runs on cellular or in the background. */
class WifiDoctorProbe(private val context: Context) {
    fun run(): WifiProbeResult {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork ?: return WifiProbeResult(false, "Aucun réseau actif", null)
        val caps = cm.getNetworkCapabilities(network)
        if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) != true) {
            return WifiProbeResult(false, "Diagnostic bloqué : KINLINK ne teste jamais les données mobiles", null)
        }
        val started = System.nanoTime()
        return runCatching {
            val connection = network.openConnection(URL("https://connectivitycheck.gstatic.com/generate_204")) as HttpURLConnection
            connection.connectTimeout = 2_500
            connection.readTimeout = 2_500
            connection.instanceFollowRedirects = false
            connection.requestMethod = "GET"
            val code = connection.responseCode
            connection.disconnect()
            val elapsed = (System.nanoTime() - started) / 1_000_000
            if (code in 200..399) WifiProbeResult(true, "Wi-Fi joignable : réponse $code en ${elapsed} ms", elapsed)
            else WifiProbeResult(false, "Wi-Fi connecté, mais réponse Internet $code", elapsed)
        }.getOrElse {
            val elapsed = (System.nanoTime() - started) / 1_000_000
            WifiProbeResult(false, "Wi-Fi local présent, Internet non confirmé (${it.javaClass.simpleName})", elapsed)
        }
    }
}
