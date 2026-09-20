package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import com.terminator364.kinlink.data.TelemetryLedger
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class AutopilotRecoveryController(
    private val context: Context,
    private val ledger: TelemetryLedger,
    private val onRecoveryActionExecuted: ((NetworkTruth) -> Unit)? = null
) {
    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private val profileStore = AutopilotProfileStore(context)
    private val resourceGuard = DeviceResourceGuard(context)
    private val worker = Executors.newSingleThreadExecutor()
    private val inFlight = AtomicBoolean(false)
    @Volatile private var closed = false
    @Volatile private var lastTransportTransitionElapsedMillis: Long? = null
    private var lowQualityStreak = 0

    fun onTransportTransition() {
        lastTransportTransitionElapsedMillis = SystemClock.elapsedRealtime()
        lowQualityStreak = 0
    }

    fun onTruth(truth: NetworkTruth, instabilityScore: Int) {
        if (closed) return
        if (!TransportSettlingPolicy.recoveryAllowed(
                SystemClock.elapsedRealtime(),
                lastTransportTransitionElapsedMillis
            )
        ) {
            return
        }
        lowQualityStreak = PassiveQualityPersistencePolicy.nextStreak(lowQualityStreak, truth)
        val persistentLowQuality = PassiveQualityPersistencePolicy.persistentLowQuality(lowQualityStreak)
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
            sinceLast,
            persistentLowQuality
        )
        if (decision.action == AutomaticRecoveryAction.NONE) return
        if (!inFlight.compareAndSet(false, true)) return
        worker.execute {
            try { execute(decision, truth) } finally { inFlight.set(false) }
        }
    }

    fun close() {
        closed = true
        worker.shutdownNow()
    }

    private fun execute(decision: AutomaticRecoveryDecision, triggeringTruth: NetworkTruth) {
        val started = SystemClock.elapsedRealtime()
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
                if (!stillSameActiveWifi(network)) {
                    record(false, "WATCHDOG_TRANSPORT_CHANGED", "Transport changé avant refresh; abandon fail-open.")
                    return
                }
                if (watchdogExpired(started)) {
                    record(false, "WATCHDOG_TIMEOUT", "Deadline de récupération dépassée avant refresh; abandon fail-open.")
                    return
                }
                val refreshed = runCatching { cm.requestBandwidthUpdate(network) }.getOrDefault(false)
                record(refreshed, "REFRESH", "Wi-Fi validé mais instable : métriques rafraîchies sans probe.")
                if (refreshed) onRecoveryActionExecuted?.invoke(triggeringTruth)
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
                if (watchdogExpired(started)) {
                    record(false, "WATCHDOG_TIMEOUT", "Micro-probe terminé hors deadline; aucune action réseau supplémentaire.")
                    return
                }
                if (!stillSameActiveWifi(network)) {
                    record(false, "WATCHDOG_TRANSPORT_CHANGED", "Transport changé pendant le micro-probe; aucune action réseau supplémentaire.")
                    return
                }
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

    private fun watchdogExpired(startedElapsedMillis: Long): Boolean =
        RecoveryWatchdogPolicy.action(
            nowElapsedMillis = SystemClock.elapsedRealtime(),
            lastHeartbeatElapsedMillis = startedElapsedMillis,
            hardDeadlineMillis = RECOVERY_DEADLINE_MS
        ) == RecoveryWatchdogAction.FAIL_OPEN

    private fun stillSameActiveWifi(expected: android.net.Network): Boolean {
        val active = cm.activeNetwork ?: return false
        if (active != expected) return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun record(success: Boolean, suffix: String, summary: String) {
        runCatching { ledger.appendAction("${ACTION_PREFIX}_$suffix", success, summary) }
    }

    companion object {
        const val ACTION_PREFIX = "AUTO_RECOVERY"
        const val RECOVERY_DEADLINE_MS = 5_000L
    }
}
