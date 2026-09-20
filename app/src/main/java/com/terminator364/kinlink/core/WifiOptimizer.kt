package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

enum class WifiOptimizationAction {
    BLOCKED_NON_WIFI,
    CAPTIVE_PORTAL_REQUIRED,
    METERED_WIFI_REFRESH_ONLY,
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
        probeSucceeded: Boolean?,
        meteredWifi: Boolean = false
    ): WifiOptimizationAction = when {
        !isWifi -> WifiOptimizationAction.BLOCKED_NON_WIFI
        captivePortal -> WifiOptimizationAction.CAPTIVE_PORTAL_REQUIRED
        meteredWifi -> WifiOptimizationAction.METERED_WIFI_REFRESH_ONLY
        androidValidated -> WifiOptimizationAction.KEEP_VALIDATED_AND_REFRESH
        probeSucceeded == true -> WifiOptimizationAction.CONFIRM_AND_REFRESH
        probeSucceeded == false -> WifiOptimizationAction.NEGATIVE_EVIDENCE_REFRESH
        else -> WifiOptimizationAction.INCONCLUSIVE_REFRESH
    }

    /** Hard fail-open rule: KINLINK never reports connectivity truth back to Android. */
    fun connectivityReport(action: WifiOptimizationAction): Boolean? = null
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
        val meteredWifi = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        if (captivePortal) {
            return WifiOptimizationResult(
                false,
                WifiOptimizationAction.CAPTIVE_PORTAL_REQUIRED,
                "Portail Wi-Fi détecté : ouvre la page de connexion. KINLINK ne force ni mobile ni fausse panne.",
                androidValidated = false
            )
        }

        if (meteredWifi) {
            val refreshed = runCatching { cm.requestBandwidthUpdate(network) }.getOrDefault(false)
            return WifiOptimizationResult(
                success = androidValidated,
                action = WifiOptimizationAction.METERED_WIFI_REFRESH_ONLY,
                summary = "Wi-Fi mesuré détecté : aucun micro-test HTTP. KINLINK rafraîchit seulement les métriques Android.",
                frameworkHintSent = false,
                bandwidthRefreshRequested = refreshed,
                androidValidated = androidValidated,
                probeAttempts = 0
            )
        }

        val probe = WifiDoctorProbe(context).run()
        val action = WifiOptimizerPolicy.action(true, androidValidated, false, probe.success, meteredWifi = false)
        // P0 handoff invariant: never influence Android's network validation state.
        // requestBandwidthUpdate only refreshes metrics for the currently observed Wi-Fi.
        val hintSent = false
        val bandwidthRefresh = runCatching { cm.requestBandwidthUpdate(network) }.getOrDefault(false)

        val responsiveness = WifiProbeLatencyPolicy.classify(probe.latencyMillis)
        val responsivenessLabel = WifiProbeLatencyPolicy.label(responsiveness)
        val summary = when (action) {
            WifiOptimizationAction.KEEP_VALIDATED_AND_REFRESH ->
                if (probe.success) {
                    "Internet est validé par Android et confirmé en ${probe.latencyMillis ?: "?"} ms ($responsivenessLabel). Les métriques Wi-Fi ont été rafraîchies."
                } else {
                    "Android confirme Internet. Les micro-tests n’ont pas répondu : KINLINK conserve l’état sain et rafraîchit seulement les métriques."
                }
            WifiOptimizationAction.CONFIRM_AND_REFRESH ->
                "Android n’avait pas encore validé Internet, mais le micro-test l’a confirmé localement ($responsivenessLabel). KINLINK ne modifie pas l’état réseau Android et rafraîchit seulement les métriques."
            WifiOptimizationAction.NEGATIVE_EVIDENCE_REFRESH ->
                "Internet n’est pas confirmé par les micro-tests. KINLINK ne signale pas de panne à Android et ne provoque aucune bascule mobile; seules les métriques sont rafraîchies."
            WifiOptimizationAction.CAPTIVE_PORTAL_REQUIRED -> "Portail Wi-Fi détecté : connexion utilisateur requise."
            WifiOptimizationAction.METERED_WIFI_REFRESH_ONLY -> "Wi-Fi mesuré : métriques seulement, sans micro-test."
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
