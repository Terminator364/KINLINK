package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

enum class WifiOptimizationAction {
    BLOCKED_NON_WIFI,
    HANDOFF_ABORTED,
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
    val probeAttempts: Int = 0,
    val dnsProbeSucceeded: Boolean? = null,
    val dnsLatencyMillis: Long? = null,
    val manualDiagnosisCause: ManualWifiDiagnosisCause = ManualWifiDiagnosisCause.INCONCLUSIVE
)

object WifiOptimizationContinuationPolicy {
    fun mayRefresh(sameActiveNetwork: Boolean, activeIsWifi: Boolean): Boolean =
        sameActiveNetwork && activeIsWifi
}

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
            val activeAfter = cm.activeNetwork
            val activeCaps = activeAfter?.let(cm::getNetworkCapabilities)
            val mayRefresh = WifiOptimizationContinuationPolicy.mayRefresh(
                sameActiveNetwork = activeAfter == network,
                activeIsWifi = activeCaps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            )
            if (!mayRefresh) {
                return WifiOptimizationResult(
                    success = false,
                    action = WifiOptimizationAction.HANDOFF_ABORTED,
                    summary = "Handoff détecté : le Wi-Fi initial n’est plus actif; aucune action restante.",
                    androidValidated = androidValidated,
                    probeAttempts = 0
                )
            }
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

        val dnsProbe = WifiDnsProbe(context, network).run()
        val probe = WifiDoctorProbe(context).run(network)
        val action = WifiOptimizerPolicy.action(true, androidValidated, false, probe.success, meteredWifi = false)
        val diagnosisCause = ManualWifiDiagnosisPolicy.classify(
            androidValidated = androidValidated,
            dnsSucceeded = dnsProbe.success,
            httpSucceeded = probe.success
        )
        // P0 handoff invariant: stale Wi-Fi work must stop after Android changes
        // the active default network. requestBandwidthUpdate is only allowed for the
        // originally captured Wi-Fi while it is still active.
        val activeAfterDiagnostics = cm.activeNetwork
        val activeCapsAfterDiagnostics =
            activeAfterDiagnostics?.let(cm::getNetworkCapabilities)
        val mayRefresh = WifiOptimizationContinuationPolicy.mayRefresh(
            sameActiveNetwork = activeAfterDiagnostics == network,
            activeIsWifi =
                activeCapsAfterDiagnostics?.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                ) == true
        )
        if (!mayRefresh) {
            return WifiOptimizationResult(
                success = false,
                action = WifiOptimizationAction.HANDOFF_ABORTED,
                summary = "Handoff détecté après diagnostic : aucune action appliquée au Wi-Fi devenu inactif.",
                latencyMillis = probe.latencyMillis,
                frameworkHintSent = false,
                bandwidthRefreshRequested = false,
                androidValidated = androidValidated,
                probeAttempts = probe.attempts,
                dnsProbeSucceeded = dnsProbe.success,
                dnsLatencyMillis = dnsProbe.latencyMillis,
                manualDiagnosisCause = diagnosisCause
            )
        }

        val hintSent = false
        val bandwidthRefresh =
            runCatching { cm.requestBandwidthUpdate(network) }.getOrDefault(false)

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
            WifiOptimizationAction.HANDOFF_ABORTED -> "Handoff détecté : aucune action restante sur le Wi-Fi devenu inactif."
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
            probeAttempts = probe.attempts,
            dnsProbeSucceeded = dnsProbe.success,
            dnsLatencyMillis = dnsProbe.latencyMillis,
            manualDiagnosisCause = diagnosisCause
        )
    }
}
