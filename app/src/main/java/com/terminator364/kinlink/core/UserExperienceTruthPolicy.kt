package com.terminator364.kinlink.core

import com.terminator364.kinlink.data.RecentReliabilityWindow

enum class UserExperienceState {
    OFFLINE,
    LOGIN_REQUIRED,
    DEGRADED_NOW,
    UNSTABLE_HISTORY,
    USER_REPORTED_BAD,
    ACCESS_AVAILABLE_QUALITY_UNVERIFIED
}

data class UserExperienceAssessment(
    val state: UserExperienceState,
    val headline: String,
    val statusLabel: String,
    val detail: String,
    val degraded: Boolean
)

/**
 * User-facing truth policy.
 *
 * Android VALIDATED and bandwidth estimates are valuable framework evidence, but
 * they are not equivalent to the user's perceived latency, throughput or stability.
 * This policy therefore never labels a connection "100/100" or "comfortable" from
 * framework estimates alone.
 */
object UserExperienceTruthPolicy {
    fun assess(
        truth: NetworkTruth,
        reliability: RecentReliabilityWindow?,
        recentUserIssue: Boolean
    ): UserExperienceAssessment {
        if (truth.transport == Transport.NONE || truth.internetState == InternetState.OFFLINE) {
            return UserExperienceAssessment(
                UserExperienceState.OFFLINE,
                "Hors ligne",
                "indisponible",
                "Android ne confirme aucun accès Internet actif.",
                true
            )
        }

        if (truth.internetState == InternetState.CAPTIVE_PORTAL) {
            return UserExperienceAssessment(
                UserExperienceState.LOGIN_REQUIRED,
                "Connexion requise",
                "connexion requise",
                "Un portail captif demande une action avant de pouvoir juger la qualité.",
                true
            )
        }

        if (recentUserIssue) {
            return UserExperienceAssessment(
                UserExperienceState.USER_REPORTED_BAD,
                "Connexion signalée mauvaise",
                "problème signalé",
                "Ton signalement utilisateur prime sur un score passif : KINLINK garde l’état en surveillance renforcée.",
                true
            )
        }

        val burden = reliability?.let {
            RecentReliabilityPolicy.classify(
                it.interruptionCount,
                it.cumulativeMillis,
                it.longestMillis,
                it.lowQualityEpisodeCount + it.mobileLowQualityEpisodeCount,
                it.lowQualityCumulativeMillis + it.mobileLowQualityCumulativeMillis,
                maxOf(it.lowQualityLongestMillis, it.mobileLowQualityLongestMillis)
            )
        }

        if (
            (reliability?.platformDataStallCount ?: 0) >= 2 ||
            burden == RecentReliabilityBurden.SEVERE ||
            burden == RecentReliabilityBurden.UNSTABLE
        ) {
            val platformStalls = reliability?.platformDataStallCount ?: 0
            val label = when {
                platformStalls >= 2 -> "stalls Android détectés"
                burden == RecentReliabilityBurden.SEVERE -> "très instable sur 24 h"
                else -> "instable sur 24 h"
            }
            return UserExperienceAssessment(
                UserExperienceState.UNSTABLE_HISTORY,
                "Internet disponible · historique instable",
                label,
                "Android confirme l’accès maintenant, mais l’historique récent ou ses diagnostics natifs montrent des coupures, lenteurs ou stalls.",
                true
            )
        }

        val passive = PassiveLinkQualityPolicy.assess(truth).quality
        if (
            truth.internetState != InternetState.VALIDATED ||
            passive == PassiveLinkQuality.CONSTRAINED ||
            passive == PassiveLinkQuality.LIMITED ||
            !truth.androidNotCongested ||
            !truth.androidNotSuspended
        ) {
            return UserExperienceAssessment(
                UserExperienceState.DEGRADED_NOW,
                "Connexion à surveiller",
                "signaux de dégradation",
                "Les indicateurs Android montrent un risque de lenteur ou d’instabilité. KINLINK évite de conclure à une bonne qualité sans preuve.",
                true
            )
        }

        return UserExperienceAssessment(
            UserExperienceState.ACCESS_AVAILABLE_QUALITY_UNVERIFIED,
            if (truth.transport == Transport.CELLULAR) "Données mobiles connectées" else "Wi-Fi connecté",
            "accès disponible · qualité non mesurée",
            "Android confirme l’accès Internet. Le débit et la réactivité réellement ressentis ne sont pas mesurés par un speedtest.",
            false
        )
    }
}
