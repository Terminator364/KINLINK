package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.terminator364.kinlink.data.TelemetryLedger
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class AutopilotRecoveryController(
    private val context: Context,
    private val ledger: TelemetryLedger
) {
    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private val profileStore = AutopilotProfileStore(context)
    private val resourceGuard = DeviceResourceGuard(context)
    private val worker = Executors.newSingleThreadExecutor()
    private val inFlight = AtomicBoolean(false)
    @Volatile private var closed = false

    fun onTruth(truth: NetworkTruth, instabilityScore: Int) {
        if (closed) return
        val now = System.currentTimeMillis()
        val since = now - AutopilotRecoveryPolicy.hourlyWindowMillis()
        val recent = runCatching { ledger.countActionsSince(ACTION_PREFIX, since) }.getOrDefault(0)
        val last = runCatching { ledger.latestActionTimestamp(ACTION_PREFIX) }.getOrNull()
        val sinceLast = last?.let { (now - it).coerceAtLeast(0L) } ?: Long.MAX_VALUE
        val decision = AutopilotRecoveryPolicy.decide(
            truth,
            profileStore.current(),
            instabilityScore,
            resourceGuard.snapshot().constrained,
            recent,
            sinceLast
        )
        if (decision.action == AutomaticRecoveryAction.NONE) return
        if (!inFlight.compareAndSet(false, true)) return
        worker.execute {
            try { execute(decision) } finally { inFlight.set(false) }
        }
    }

    fun close() {
        closed = true
        worker.shutdownNow()
    }

    private fun execute(decision: AutomaticRecoveryDecision) {
        val network = cm.activeNetwork ?: run {
            record(false, "NO_NETWORK", "Réseau disparu avant l’action.")
            return
        }
        val caps = cm.getNetworkCapabilities(network) ?: run {
            record(false, "NO_CAPS", "Capacités réseau indisponibles.")
            return
        }
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            record(false, "ABORT_NON_WIFI", "Transport changé : aucune action hors Wi-Fi.")
            return
        }
        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)) {
            record(false, "ABORT_PORTAL", "Portail captif : intervention utilisateur requise.")
            return
        }

        when (decision.action) {
            AutomaticRecoveryAction.REFRESH_METRICS -> {
                val refreshed = runCatching { cm.requestBandwidthUpdate(network) }.getOrDefault(false)
                record(refreshed, "REFRESH", "Wi-Fi validé mais instable : métriques rafraîchies sans probe.")
            }
            AutomaticRecoveryAction.CONFIRM_WIFI -> {
                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    val refreshed = runCatching { cm.requestBandwidthUpdate(network) }.getOrDefault(false)
                    record(true, "ALREADY_VALIDATED", "Android a validé Internet avant le probe; aucune sonde envoyée. Refresh=$refreshed")
                    return
                }
                val metered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                if (metered) {
                    record(false, "ABORT_METERED_WIFI", "Wi-Fi mesuré détecté : aucun micro-probe automatique.")
                    return
                }

                val probe = WifiDoctorProbe(context).run()
                val refreshed = runCatching { cm.requestBandwidthUpdate(network) }.getOrDefault(false)
                if (probe.success) {
                    record(
                        true,
                        "WIFI_CONFIRMED",
                        "Internet confirmé localement en ${probe.latencyMillis ?: -1} ms; aucun signal de validation envoyé à Android; refresh=$refreshed"
                    )
                } else {
                    record(
                        false,
                        "WIFI_INCONCLUSIVE",
                        "Micro-tests non concluants; aucun signal négatif envoyé à Android; refresh=$refreshed"
                    )
                }
            }
            AutomaticRecoveryAction.NONE -> Unit
        }
    }

    private fun record(success: Boolean, suffix: String, summary: String) {
        runCatching { ledger.appendAction("${ACTION_PREFIX}_$suffix", success, summary) }
    }

    companion object {
        const val ACTION_PREFIX = "AUTO_RECOVERY"
    }
}
