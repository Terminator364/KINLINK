package com.terminator364.kinlink.core

enum class AutopilotProfile {
    CONSERVATIVE,
    BALANCED,
    MAXIMUM_STABILITY
}

enum class AutopilotIntent {
    HOLD_STEADY,
    PROTECT_MOBILE,
    OBSERVE_WIFI,
    MOBILE_ASSIST,
    CAPTIVE_PORTAL_ACTION,
    RECOVERY_CANDIDATE,
    WAIT_FOR_EVIDENCE
}

data class AdaptiveDecision(
    val intent: AutopilotIntent,
    val reason: String,
    val allowAutomaticProbe: Boolean,
    val allowMobileAssist: Boolean
)

/**
 * Pure policy engine. It never touches Android networking directly.
 * Decisions are intentionally conservative until a stronger data plane passes its own gates.
 */
object AdaptivePolicyEngine {
    fun evaluate(
        truth: NetworkTruth,
        instabilityScore: Int,
        profile: AutopilotProfile = AutopilotProfile.BALANCED
    ): AdaptiveDecision {
        if (
            truth.budgetState == BudgetState.BUNDLE_EXHAUSTED ||
            truth.budgetState == BudgetState.BUNDLE_EXPIRED ||
            truth.budgetState == BudgetState.BUNDLE_LOW
        ) {
            return AdaptiveDecision(
                AutopilotIntent.PROTECT_MOBILE,
                "Le budget mobile impose la retenue.",
                allowAutomaticProbe = false,
                allowMobileAssist = false
            )
        }

        if (truth.internetState == InternetState.CAPTIVE_PORTAL) {
            return AdaptiveDecision(
                AutopilotIntent.CAPTIVE_PORTAL_ACTION,
                "Un portail de connexion exige une action utilisateur.",
                allowAutomaticProbe = false,
                allowMobileAssist = false
            )
        }

        if (truth.transport == Transport.WIFI && truth.internetState == InternetState.VALIDATED) {
            val passiveQuality = PassiveLinkQualityPolicy.assess(truth)
            val threshold = when (profile) {
                AutopilotProfile.CONSERVATIVE -> 70
                AutopilotProfile.BALANCED -> 55
                AutopilotProfile.MAXIMUM_STABILITY -> 40
            }

            return if (instabilityScore >= threshold) {
                AdaptiveDecision(
                    AutopilotIntent.OBSERVE_WIFI,
                    "Le Wi-Fi fonctionne mais présente des oscillations récentes.",
                    allowAutomaticProbe = false,
                    allowMobileAssist = false
                )
            } else if (
                passiveQuality.quality == PassiveLinkQuality.CONSTRAINED ||
                passiveQuality.quality == PassiveLinkQuality.LIMITED
            ) {
                AdaptiveDecision(
                    AutopilotIntent.OBSERVE_WIFI,
                    "Internet est validé, mais la capacité passive annoncée par Android est limitée.",
                    allowAutomaticProbe = false,
                    allowMobileAssist = false
                )
            } else {
                AdaptiveDecision(
                    AutopilotIntent.HOLD_STEADY,
                    "Le Wi-Fi est validé : ne pas perturber une connexion qui fonctionne.",
                    allowAutomaticProbe = false,
                    allowMobileAssist = false
                )
            }
        }

        if (truth.transport == Transport.CELLULAR) {
            if (!truth.androidNotSuspended) {
                return AdaptiveDecision(
                    AutopilotIntent.WAIT_FOR_EVIDENCE,
                    "Android signale le réseau mobile suspendu : aucune action active.",
                    allowAutomaticProbe = false,
                    allowMobileAssist = false
                )
            }
            if (truth.internetState == InternetState.VALIDATED) {
                val quality = PassiveLinkQualityPolicy.assess(truth).quality
                val degraded =
                    quality == PassiveLinkQuality.CONSTRAINED ||
                        quality == PassiveLinkQuality.LIMITED ||
                        !truth.androidNotCongested
                return if (degraded) {
                    AdaptiveDecision(
                        AutopilotIntent.MOBILE_ASSIST,
                        "Données mobiles validées mais dégradées : Mobile Assist zéro-probe peut rafraîchir les métriques Android.",
                        allowAutomaticProbe = false,
                        allowMobileAssist = true
                    )
                } else {
                    AdaptiveDecision(
                        AutopilotIntent.HOLD_STEADY,
                        "Données mobiles validées et utilisables : ne pas perturber la liaison.",
                        allowAutomaticProbe = false,
                        allowMobileAssist = false
                    )
                }
            }
            return AdaptiveDecision(
                AutopilotIntent.WAIT_FOR_EVIDENCE,
                "Transport mobile présent mais Internet non validé : pas de probe payant; assistance système seulement sur action utilisateur.",
                allowAutomaticProbe = false,
                allowMobileAssist = false
            )
        }

        if (truth.transport == Transport.NONE || truth.internetState == InternetState.OFFLINE) {
            return AdaptiveDecision(
                AutopilotIntent.RECOVERY_CANDIDATE,
                "Aucun accès Internet confirmé. Attendre une preuve avant action forte.",
                allowAutomaticProbe = false,
                allowMobileAssist = false
            )
        }

        return AdaptiveDecision(
            AutopilotIntent.WAIT_FOR_EVIDENCE,
            "État incomplet : observation passive prioritaire.",
            allowAutomaticProbe = false,
            allowMobileAssist = false
        )
    }
}
