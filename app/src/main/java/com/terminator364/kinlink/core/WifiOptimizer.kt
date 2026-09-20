package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

enum class WifiOptimizationAction {
    BLOCKED_NON_WIFI,
    CONFIRM_AND_REFRESH,
    REVALIDATE_AND_REFRESH,
    OBSERVE_ONLY
}

data class WifiOptimizationResult(
    val success: Boolean,
    val action: WifiOptimizationAction,
    val summary: String,
    val latencyMillis: Long? = null,
    val frameworkHintSent: Boolean = false,
    val bandwidthRefreshRequested: Boolean = false
)

object WifiOptimizerPolicy {
    fun action(isWifi: Boolean, probeSucceeded: Boolean?): WifiOptimizationAction = when {
        !isWifi -> WifiOptimizationAction.BLOCKED_NON_WIFI
        probeSucceeded == true -> WifiOptimizationAction.CONFIRM_AND_REFRESH
        probeSucceeded == false -> WifiOptimizationAction.REVALIDATE_AND_REFRESH
        else -> WifiOptimizationAction.OBSERVE_ONLY
    }
}

/**
 * Explicit Wi-Fi-only optimizer.
 *
 * It never toggles mobile data, never changes routes, never runs a speed test and never
 * creates a persistent network request. It uses a bounded micro-probe then gives Android
 * a connectivity hint and asks for refreshed bandwidth metrics.
 */
class WifiOptimizer(private val context: Context) {
    fun optimize(): WifiOptimizationResult {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork
            ?: return WifiOptimizationResult(
                false,
                WifiOptimizationAction.BLOCKED_NON_WIFI,
                "Aucun réseau actif : aucune action appliquée"
            )

        val caps = cm.getNetworkCapabilities(network)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        if (!isWifi) {
            return WifiOptimizationResult(
                false,
                WifiOptimizationAction.BLOCKED_NON_WIFI,
                "Optimisation bloquée : KINLINK ne touche jamais aux données mobiles"
            )
        }

        val probe = WifiDoctorProbe(context).run()
        val action = WifiOptimizerPolicy.action(isWifi = true, probeSucceeded = probe.success)

        val hintSent = runCatching {
            cm.reportNetworkConnectivity(network, probe.success)
            true
        }.getOrDefault(false)

        val bandwidthRefresh = runCatching {
            cm.requestBandwidthUpdate(network)
        }.getOrDefault(false)

        val summary = when (action) {
            WifiOptimizationAction.CONFIRM_AND_REFRESH ->
                "Wi-Fi confirmé en ${probe.latencyMillis ?: "?"} ms. Android a reçu l’état réel et KINLINK a demandé un rafraîchissement des métriques."
            WifiOptimizationAction.REVALIDATE_AND_REFRESH ->
                "Le Wi-Fi répond mal. KINLINK a demandé à Android de réévaluer la connectivité, sans forcer les données mobiles."
            WifiOptimizationAction.BLOCKED_NON_WIFI ->
                "Action bloquée hors Wi-Fi."
            WifiOptimizationAction.OBSERVE_ONLY ->
                "Aucune intervention réseau nécessaire."
        }

        return WifiOptimizationResult(
            success = probe.success,
            action = action,
            summary = summary,
            latencyMillis = probe.latencyMillis,
            frameworkHintSent = hintSent,
            bandwidthRefreshRequested = bandwidthRefresh
        )
    }
}
