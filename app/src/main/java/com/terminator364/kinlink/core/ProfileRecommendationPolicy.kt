package com.terminator364.kinlink.core

data class ProfileRecommendation(
    val profile: AutopilotProfile,
    val reason: String
)

object ProfileRecommendationPolicy {
    fun recommend(burden: RecentReliabilityBurden): ProfileRecommendation = when (burden) {
        RecentReliabilityBurden.QUIET -> ProfileRecommendation(
            AutopilotProfile.CONSERVATIVE,
            "Historique récent calme : le profil Conservateur limite davantage les actions."
        )
        RecentReliabilityBurden.NOTICEABLE -> ProfileRecommendation(
            AutopilotProfile.BALANCED,
            "Quelques incidents récents : le profil Équilibré reste adapté."
        )
        RecentReliabilityBurden.UNSTABLE,
        RecentReliabilityBurden.SEVERE -> ProfileRecommendation(
            AutopilotProfile.MAXIMUM_STABILITY,
            "Instabilité récente élevée : Stabilité max privilégie la résilience, toujours dans les limites fail-open."
        )
    }
}
