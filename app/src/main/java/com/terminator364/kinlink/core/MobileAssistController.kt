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
        profile: AutopilotProfile = AutopilotProfile.BALANCED,
        nowWallMs: Long = System.currentTimeMillis()
    ): MobileAssistDecision {
        if (truth.transport != Transport.CELLULAR) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.NOT_CELLULAR,
                "Mobile Assist inactif hors données mobiles."
            )
        }
        if (mode == RecoveryMode.OBSERVATION_ONLY) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.OBSERVATION_ONLY,
                "Mode sûr : aucune action mobile active."
            )
        }
        if (!truth.androidNotSuspended) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.NETWORK_SUSPENDED,
                "Android signale le réseau mobile suspendu."
            )
        }
        if (
            MobileRadioQualityPolicy.assess(truth).quality ==
                MobileRadioQuality.WEAK
        ) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.WEAK_SIGNAL,
                "Signal radio faible : aucun refresh inutile."
            )
        }
        if (
            truth.budgetState == BudgetState.BUNDLE_LOW ||
            truth.budgetState == BudgetState.BUNDLE_EXHAUSTED ||
            truth.budgetState == BudgetState.BUNDLE_EXPIRED
        ) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.BUDGET_PROTECTED,
                "Protection du forfait active."
            )
        }

        val passiveQuality = PassiveLinkQualityPolicy.assess(truth).quality
        val healthyValidated =
            truth.internetState == InternetState.VALIDATED &&
                truth.androidNotCongested &&
                passiveQuality == PassiveLinkQuality.COMFORTABLE
        if (healthyValidated) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.HEALTHY_OR_UNKNOWN,
                "Liaison mobile utilisable : aucune dépense CPU/DB inutile."
            )
        }

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
                ledger.countExactActionSince(EVIDENCE_NO_BETTER, since) +
                    ledger.countExactActionSince(EVIDENCE_RELAPSED, since)
            }.getOrDefault(2)

        val decision = MobileAssistPolicy.decide(
            truth = truth,
            recoveryMode = mode,
            resourceConstrained = runCatching {
                resourceGuard.snapshot().constrained
            }.getOrDefault(true),
            recentActions = recent,
            millisSinceLastAction = sinceLast,
            recentIneffectiveOutcomes = recentIneffective,
            profile = profile
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
        const val EVIDENCE_NO_BETTER = "MOBILE_ASSIST_EVIDENCE_NO_BETTER"
        const val EVIDENCE_RELAPSED = "MOBILE_ASSIST_EVIDENCE_RELAPSED"
    }
}
