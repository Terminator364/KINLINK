package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.terminator364.kinlink.data.TelemetryLedger

class MobileAssistController(
    context: Context,
    private val ledger: TelemetryLedger,
    private val onRefreshAccepted: (NetworkTruth) -> Unit = {}
) {
    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private val resourceGuard = DeviceResourceGuard(context)

    fun onTruth(
        truth: NetworkTruth,
        mode: RecoveryMode,
        nowWallMs: Long = System.currentTimeMillis()
    ): MobileAssistDecision {
        val since = nowWallMs - MobileAssistPolicy.HOURLY_WINDOW_MS
        val recent = runCatching {
            ledger.countActionsSince(ACTION_PREFIX, since)
        }.getOrDefault(MobileAssistPolicy.MAX_ACTIONS_PER_HOUR)
        val last = runCatching {
            ledger.latestActionTimestamp(ACTION_PREFIX)
        }.getOrNull()
        val sinceLast = last?.let { (nowWallMs - it).coerceAtLeast(0L) } ?: Long.MAX_VALUE
        val recentIneffective =
            runCatching {
                ledger.countActionsSince(OUTCOME_UNCHANGED, since) +
                    ledger.countActionsSince(OUTCOME_DEGRADED, since)
            }.getOrDefault(2)

        val decision = MobileAssistPolicy.decide(
            truth = truth,
            recoveryMode = mode,
            resourceConstrained = runCatching {
                resourceGuard.snapshot().constrained
            }.getOrDefault(true),
            recentActions = recent,
            millisSinceLastAction = sinceLast,
            recentIneffectiveOutcomes = recentIneffective
        )

        if (decision.action != MobileAssistAction.REFRESH_LINK_METRICS) {
            return decision
        }

        val network = cm.activeNetwork
            ?: return decision.copy(
                action = MobileAssistAction.NONE,
                summary = "Mobile Assist annulé : aucun réseau actif."
            )
        val caps = cm.getNetworkCapabilities(network)
        val stillCellular =
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val stillValidated =
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val stillNotSuspended =
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED) == true

        if (!stillCellular || !stillValidated || !stillNotSuspended) {
            runCatching {
                ledger.appendAction(
                    "${ACTION_PREFIX}AUTO_ABORT_STALE",
                    false,
                    "Réseau mobile changé/suspendu avant le rafraîchissement; aucune action appliquée."
                )
            }
            return decision.copy(
                action = MobileAssistAction.NONE,
                summary = "Mobile Assist annulé : l’état mobile a changé."
            )
        }

        val refreshed = runCatching {
            cm.requestBandwidthUpdate(network)
        }.getOrDefault(false)

        runCatching {
            ledger.appendAction(
                "${ACTION_PREFIX}AUTO_REFRESH_METRICS",
                refreshed,
                if (refreshed)
                    "Rafraîchissement passif des métriques Android demandé; aucun speedtest/probe mobile."
                else
                    "Android n’a pas accepté le rafraîchissement des métriques; aucune autre action."
            )
        }

        if (refreshed) {
            runCatching { onRefreshAccepted(truth) }
        }

        return decision.copy(
            summary = if (refreshed)
                "Mobile Assist a demandé un rafraîchissement léger des métriques Android, sans probe mobile."
            else
                "Mobile Assist n’a rien forcé : Android n’a pas accepté le rafraîchissement."
        )
    }

    companion object {
        const val ACTION_PREFIX = "MOBILE_ASSIST_ACTION_"
        const val OUTCOME_UNCHANGED = "MOBILE_ASSIST_OUTCOME_UNCHANGED"
        const val OUTCOME_DEGRADED = "MOBILE_ASSIST_OUTCOME_DEGRADED"
    }
}
