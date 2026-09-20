package com.terminator364.kinlink.core

enum class AutomaticRecoveryAction {
    NONE,
    REFRESH_METRICS,
    CONFIRM_WIFI
}

data class AutomaticRecoveryDecision(
    val action: AutomaticRecoveryAction,
    val reason: String
)

object AutopilotRecoveryPolicy {
    private const val HOUR_MS = 60L * 60L * 1000L

    fun decide(
        truth: NetworkTruth,
        profile: AutopilotProfile,
        instabilityScore: Int,
        resourceConstrained: Boolean,
        recentAutomaticActions: Int,
        millisSinceLastAutomaticAction: Long,
        persistentLowQuality: Boolean = false,
        recentIneffectiveOutcomes: Int = 0,
        recentSafetyAborts: Int = 0
    ): AutomaticRecoveryDecision {
        if (truth.transport != Transport.WIFI) {
            return AutomaticRecoveryDecision(AutomaticRecoveryAction.NONE, "Récupération automatique limitée au Wi-Fi.")
        }
        if (truth.internetState == InternetState.CAPTIVE_PORTAL) {
            return AutomaticRecoveryDecision(AutomaticRecoveryAction.NONE, "Portail captif : action utilisateur requise.")
        }
        if (resourceConstrained) {
            return AutomaticRecoveryDecision(AutomaticRecoveryAction.NONE, "Batterie ou température : récupération automatique suspendue.")
        }

        val cooldown = when (profile) {
            AutopilotProfile.CONSERVATIVE -> 15L * 60L * 1000L
            AutopilotProfile.BALANCED -> 5L * 60L * 1000L
            AutopilotProfile.MAXIMUM_STABILITY -> 2L * 60L * 1000L
        }
        val hourlyCap = when (profile) {
            AutopilotProfile.CONSERVATIVE -> 1
            AutopilotProfile.BALANCED -> 4
            AutopilotProfile.MAXIMUM_STABILITY -> 8
        }

        if (RecoveryCircuitBreakerPolicy.open(recentSafetyAborts)) {
            return AutomaticRecoveryDecision(
                AutomaticRecoveryAction.NONE,
                "Circuit breaker : plusieurs abandons de sécurité récents, observation passive temporaire."
            )
        }
        if (recentIneffectiveOutcomes >= 2) {
            return AutomaticRecoveryDecision(
                AutomaticRecoveryAction.NONE,
                "Deux récupérations récentes n’ont pas amélioré la qualité : pause anti-répétition."
            )
        }
        if (recentAutomaticActions >= hourlyCap) {
            return AutomaticRecoveryDecision(AutomaticRecoveryAction.NONE, "Plafond horaire de récupération atteint.")
        }
        if (millisSinceLastAutomaticAction < cooldown) {
            return AutomaticRecoveryDecision(AutomaticRecoveryAction.NONE, "Cooldown de récupération actif.")
        }

        if (truth.internetState == InternetState.VALIDATED) {
            val threshold = when (profile) {
                AutopilotProfile.CONSERVATIVE -> 80
                AutopilotProfile.BALANCED -> 65
                AutopilotProfile.MAXIMUM_STABILITY -> 50
            }
            return if (instabilityScore >= threshold || persistentLowQuality) {
                AutomaticRecoveryDecision(
                    AutomaticRecoveryAction.REFRESH_METRICS,
                    if (persistentLowQuality)
                        "Wi-Fi validé mais capacité faible persistante : rafraîchissement métrique sans probe."
                    else
                        "Wi-Fi validé mais instable : rafraîchissement métrique sans probe."
                )
            } else {
                AutomaticRecoveryDecision(AutomaticRecoveryAction.NONE, "Wi-Fi validé : aucune intervention.")
            }
        }

        if (truth.metered) {
            return AutomaticRecoveryDecision(AutomaticRecoveryAction.NONE, "Wi-Fi mesuré : aucun probe automatique.")
        }

        val localLinkPresent =
            truth.lanState == LanState.LINK_PRESENT || truth.lanState == LanState.HEALTHY
        if (!localLinkPresent) {
            return AutomaticRecoveryDecision(AutomaticRecoveryAction.NONE, "Aucune liaison locale fiable à confirmer.")
        }

        return AutomaticRecoveryDecision(
            AutomaticRecoveryAction.CONFIRM_WIFI,
            "Wi-Fi local présent sans Internet validé : confirmation bornée autorisée."
        )
    }

    fun hourlyWindowMillis(): Long = HOUR_MS
}
