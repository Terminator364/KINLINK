package com.terminator364.kinlink.core

enum class MobileAssistAction {
    NONE,
    REFRESH_LINK_METRICS,
    OFFER_SYSTEM_CONNECTIVITY_PANEL
}

enum class MobileAssistBlockReason {
    NONE,
    NOT_CELLULAR,
    OBSERVATION_ONLY,
    RESOURCE_CONSTRAINED,
    NETWORK_SUSPENDED,
    WEAK_SIGNAL,
    BUDGET_PROTECTED,
    HEALTHY_OR_UNKNOWN,
    COOLDOWN,
    HOURLY_CAP,
    INEFFECTIVE_RECENTLY
}

data class MobileAssistDecision(
    val action: MobileAssistAction,
    val blockReason: MobileAssistBlockReason,
    val summary: String
)

object MobileAssistPolicy {
    const val COOLDOWN_MS = 5L * 60L * 1000L
    const val HOURLY_WINDOW_MS = 60L * 60L * 1000L
    const val MAX_ACTIONS_PER_HOUR = 6

    fun decide(
        truth: NetworkTruth,
        recoveryMode: RecoveryMode,
        resourceConstrained: Boolean,
        recentActions: Int,
        millisSinceLastAction: Long,
        recentIneffectiveOutcomes: Int = 0,
        profile: AutopilotProfile = AutopilotProfile.BALANCED,
        recentExperienceDegraded: Boolean = false
    ): MobileAssistDecision {
        val tuning = AutopilotProfileControlPolicy.tuning(profile)
        if (truth.transport != Transport.CELLULAR) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.NOT_CELLULAR,
                "Mobile Assist inactif hors données mobiles."
            )
        }
        if (recoveryMode == RecoveryMode.OBSERVATION_ONLY) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.OBSERVATION_ONLY,
                "Mode sûr : aucune action mobile active."
            )
        }
        if (resourceConstrained) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.RESOURCE_CONSTRAINED,
                "Batterie, mémoire ou température : Mobile Assist reste passif."
            )
        }
        if (!truth.androidNotSuspended) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.NETWORK_SUSPENDED,
                "Android signale le réseau mobile suspendu; aucune action automatique."
            )
        }
        if (
            MobileRadioQualityPolicy.assess(truth).quality ==
                MobileRadioQuality.WEAK
        ) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.WEAK_SIGNAL,
                "Signal mobile faible : un refresh de métriques ne peut pas renforcer la couverture."
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
                "Budget mobile protégé : aucun travail radio supplémentaire."
            )
        }

        if (recentIneffectiveOutcomes >= 2) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.INEFFECTIVE_RECENTLY,
                "Deux assistances mobiles récentes n’ont pas amélioré la qualité : pause anti-répétition."
            )
        }

        if (recentActions >= tuning.mobileAssistMaxActionsPerHour) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.HOURLY_CAP,
                "Plafond Mobile Assist atteint pour cette heure."
            )
        }
        if (millisSinceLastAction < tuning.mobileAssistCooldownMs) {
            return MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.COOLDOWN,
                "Mobile Assist est en cooldown."
            )
        }

        if (truth.internetState != InternetState.VALIDATED) {
            return MobileAssistDecision(
                MobileAssistAction.OFFER_SYSTEM_CONNECTIVITY_PANEL,
                MobileAssistBlockReason.NONE,
                "Données mobiles actives mais Internet non validé : une intervention Android peut être utile."
            )
        }

        val quality = PassiveLinkQualityPolicy.assess(truth).quality
        val score = PassiveQualityScorePolicy.score(truth).score
        val degraded =
            quality == PassiveLinkQuality.CONSTRAINED ||
                quality == PassiveLinkQuality.LIMITED ||
                !truth.androidNotCongested ||
                score < tuning.vigilanceFloor ||
                recentExperienceDegraded

        return if (degraded) {
            MobileAssistDecision(
                MobileAssistAction.REFRESH_LINK_METRICS,
                MobileAssistBlockReason.NONE,
                "Données mobiles validées mais dégradées : rafraîchissement léger des métriques Android."
            )
        } else {
            MobileAssistDecision(
                MobileAssistAction.NONE,
                MobileAssistBlockReason.HEALTHY_OR_UNKNOWN,
                "Connexion mobile utilisable : ne pas perturber une liaison saine."
            )
        }
    }
}


enum class MobileAssistManualAction {
    NONE,
    REFRESH_LINK_METRICS,
    OPEN_SYSTEM_CONNECTIVITY_PANEL
}

enum class MobileAssistManualBlockReason {
    NONE,
    NOT_CELLULAR,
    COOLDOWN,
    HOURLY_CAP,
    OBSERVATION_ONLY,
    RESOURCE_CONSTRAINED,
    BUDGET_PROTECTED
}

data class MobileAssistManualDecision(
    val action: MobileAssistManualAction,
    val blockReason: MobileAssistManualBlockReason
)

object MobileAssistManualPolicy {
    const val COOLDOWN_MS = 30_000L

    fun decide(
        isCellular: Boolean,
        validated: Boolean,
        notSuspended: Boolean,
        observationOnly: Boolean,
        budgetProtected: Boolean,
        resourceConstrained: Boolean,
        recentActions: Int,
        millisSinceLastAction: Long,
        profile: AutopilotProfile = AutopilotProfile.BALANCED,
        preferSystemPanel: Boolean = false
    ): MobileAssistManualDecision {
        val tuning = AutopilotProfileControlPolicy.tuning(profile)
        if (!isCellular) {
            return MobileAssistManualDecision(
                MobileAssistManualAction.NONE,
                MobileAssistManualBlockReason.NOT_CELLULAR
            )
        }
        if (preferSystemPanel) {
            return MobileAssistManualDecision(
                MobileAssistManualAction.OPEN_SYSTEM_CONNECTIVITY_PANEL,
                MobileAssistManualBlockReason.NONE
            )
        }
        if (recentActions >= tuning.mobileAssistMaxActionsPerHour) {
            return MobileAssistManualDecision(
                MobileAssistManualAction.NONE,
                MobileAssistManualBlockReason.HOURLY_CAP
            )
        }
        if (millisSinceLastAction < COOLDOWN_MS) {
            return MobileAssistManualDecision(
                MobileAssistManualAction.NONE,
                MobileAssistManualBlockReason.COOLDOWN
            )
        }

        // Opening Android's own connectivity panel is navigation, not a KINLINK
        // network mutation or data probe. Keep this useful even when Mobile Vault
        // or safe mode forbids active metric refresh.
        if (!validated || !notSuspended) {
            return MobileAssistManualDecision(
                MobileAssistManualAction.OPEN_SYSTEM_CONNECTIVITY_PANEL,
                MobileAssistManualBlockReason.NONE
            )
        }

        if (observationOnly) {
            return MobileAssistManualDecision(
                MobileAssistManualAction.NONE,
                MobileAssistManualBlockReason.OBSERVATION_ONLY
            )
        }
        if (resourceConstrained) {
            return MobileAssistManualDecision(
                MobileAssistManualAction.NONE,
                MobileAssistManualBlockReason.RESOURCE_CONSTRAINED
            )
        }
        if (budgetProtected) {
            return MobileAssistManualDecision(
                MobileAssistManualAction.NONE,
                MobileAssistManualBlockReason.BUDGET_PROTECTED
            )
        }

        return MobileAssistManualDecision(
            MobileAssistManualAction.REFRESH_LINK_METRICS,
            MobileAssistManualBlockReason.NONE
        )
    }
}
