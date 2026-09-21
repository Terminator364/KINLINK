package com.terminator364.kinlink.core

object UserExperiencePresentationPolicy {
    fun controlTitle(
        state: UserExperienceState,
        fallback: String
    ): String = when (state) {
        UserExperienceState.OFFLINE ->
            "Hors ligne · surveillance passive"
        UserExperienceState.LOGIN_REQUIRED ->
            "Connexion requise"
        UserExperienceState.USER_REPORTED_BAD ->
            "Problème signalé · diagnostic prioritaire"
        UserExperienceState.UNSTABLE_HISTORY ->
            "Historique instable · surveillance renforcée"
        UserExperienceState.DEGRADED_NOW ->
            "Connexion à surveiller"
        UserExperienceState.ACCESS_AVAILABLE_QUALITY_UNVERIFIED ->
            fallback
    }

    fun platformStallLabel(count: Int): String =
        "$count blocage(s) réseau détecté(s)"
}
