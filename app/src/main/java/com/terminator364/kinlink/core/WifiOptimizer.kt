package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

enum class WifiOptimizationAction {
    BLOCKED_NON_WIFI,
    CAPTIVE_PORTAL_REQUIRED,
    KEEP_VALIDATED_AND_REFRESH,
    CONFIRM_AND_REFRESH,
    NEGATIVE_EVIDENCE_REFRESH,
    INCONCLUSIVE_REFRESH
}

data class WifiOptimizationResult(
    val success: Boolean,
    val action: WifiOptimizationAction,
    val summary: String,
    val latencyMillis: Long? = null,
    val frameworkHintSent: Boolean = false,
    val bandwidthRefreshRequested: Boolean = false,
    val androidValidated: Boolean = false,
    val probeAttempts: Int = 0
)

object WifiOptimizerPolicy {
    fun action(
        isWifi: Boolean,
        androidValidated: Boolean,
        captivePortal: Boolean,
        probeSucceeded: Boolean?
    ): WifiOptimizationAction = when {
        !isWifi -> WifiOptimizationAction.BLOCKED_NON_WIFI
        captivePortal -> WifiOptimizationAction.CAPTIVE_PORTAL_REQUIRED
        androidValidated -> WifiOptimizationAction.KEEP_VALIDATED_AND_REFRESH
        probeSucceeded == true -> WifiOptimizationAction.CONFIRM_AND_REFRESH
        probeSucceeded == false -> WifiOptimizationAction.NEGATIVE_EVIDENCE_REFRESH
        else -> WifiOptimizationAction.INCONCLUSIVE_REFRESH
    }

    fun connectivityReport(action: WifiOptimizationAction): Boolean? =
        if (action == WifiOptimizationAction.CONFIRM_AND_REFRESH) true else null
}

class WifiOptimizer(private val context: Context) {
    fun optimize(): WifiOptimizationResult {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork
            ?: return WifiOptimizationResult(false, WifiOptimizationAction.BLOCKED_NON_WIFI, "Aucun réseau actif : aucune action appliquée")
        val caps = cm.getNetworkCapabilities(network)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        if (!isWifi) {
            return WifiOptimizationResult(false, WifiOptimizationAction.BLOCKED_NON_WIFI, "Optimisation bloquée : KINLINK ne touche jamais aux données mobiles")
        }

        val androidValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val captivePortal = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)
        if (captivePortal) {
            return WifiOptimizationResult(
                false,
                WifiOptimizationAction.CAPTIVE_PORTAL_REQUIRED,
                "Portail Wi-Fi détecté : ouvre la page de connexion. KINLINK ne force ni mobile ni fausse panne.",
                androidValidated = false
            )
        }

        val probe = WifiDoctorProbe(context).run()
        val action = WifiOptimizerPolicy.action(true, androidValidated, false, probe.success)
        val reportValue = WifiOptimizerPolicy.connectivityReport(action)
        val hintSent = if (reportValue == null) false else runCatching {
            cm.reportNetworkConnectivity(network, reportValue)
            true
        }.getOrDefault(false)
        val bandwidthRefresh = runCatching { cm.requestBandwidthUpdate(network) }.getOrDefault(false)

        val summary = when (action) {
            WifiOptimizationAction.KEEP_VALIDATED_AND_REFRESH ->
                if (probe.success) {
                    "Internet est validé par Android et confirmé en ${probe.latencyMillis ?: "?"} ms. Les métriques Wi-Fi ont été rafraîchies."
                } else {
                    "Android confirme Internet. Les micro-tests n’ont pas répondu : KINLINK conserve l’état sain et rafraîchit seulement les métriques."
                }
            WifiOptimizationAction.CONFIRM_AND_REFRESH ->
                "Android n’avait pas encore validé Internet, mais le micro-test l’a confirmé. KINLINK transmet uniquement ce signal positif et rafraîchit les métriques."
            WifiOptimizationAction.NEGATIVE_EVIDENCE_REFRESH ->
                "Internet n’est pas confirmé par les micro-tests. KINLINK ne signale pas de panne à Android et ne provoque aucune bascule mobile; seules les métriques sont rafraîchies."
            WifiOptimizationAction.CAPTIVE_PORTAL_REQUIRED -> "Portail Wi-Fi détecté : connexion utilisateur requise."
            WifiOptimizationAction.BLOCKED_NON_WIFI -> "Action bloquée hors Wi-Fi."
            WifiOptimizationAction.INCONCLUSIVE_REFRESH -> "État encore incertain : KINLINK rafraîchit les métriques sans déclarer de panne."
        }

        return WifiOptimizationResult(
            success = androidValidated || probe.success,
            action = action,
            summary = summary,
            latencyMillis = probe.latencyMillis,
            frameworkHintSent = hintSent,
            bandwidthRefreshRequested = bandwidthRefresh,
            androidValidated = androidValidated,
            probeAttempts = probe.attempts
        )
    }
}
