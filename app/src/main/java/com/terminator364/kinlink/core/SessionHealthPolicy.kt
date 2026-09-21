package com.terminator364.kinlink.core

enum class SessionHealth {
    HEALTHY,
    WATCH,
    DEGRADED,
    CRITICAL
}

data class SessionHealthAssessment(
    val health: SessionHealth,
    val summary: String
)

object SessionHealthPolicy {
    fun assess(
        truth: NetworkTruth,
        instabilityScore: Int,
        flapping: Boolean,
        passiveProblem: PassiveProblemAssessment = PassiveProblemClassifier.classify(
            truth,
            instabilityScore,
            flapping
        )
    ): SessionHealthAssessment {
        if (truth.transport == Transport.NONE || truth.internetState == InternetState.OFFLINE) {
            return SessionHealthAssessment(SessionHealth.CRITICAL, "Aucune connexion Internet active.")
        }

        if (truth.internetState == InternetState.CAPTIVE_PORTAL) {
            return SessionHealthAssessment(SessionHealth.DEGRADED, "Portail captif : connexion utilisateur requise.")
        }

        if (flapping || instabilityScore >= 70) {
            return SessionHealthAssessment(SessionHealth.DEGRADED, "Connexion instable avec transitions répétées.")
        }

        return when (passiveProblem.cause) {
            PassiveProblemCause.NONE ->
                SessionHealthAssessment(SessionHealth.HEALTHY, "Connexion récente stable et utilisable.")

            PassiveProblemCause.NETWORK_SUSPENDED,
            PassiveProblemCause.WEAK_WIFI_SIGNAL,
            PassiveProblemCause.CONGESTION_SUSPECT,
            PassiveProblemCause.LOW_CAPACITY,
            PassiveProblemCause.WAN_UNVALIDATED,
            PassiveProblemCause.ADDRESSING_SUSPECT,
            PassiveProblemCause.ROUTE_CONFIGURATION_SUSPECT,
            PassiveProblemCause.DNS_CONFIGURATION_SUSPECT ->
                SessionHealthAssessment(SessionHealth.WATCH, "Connexion utilisable ou locale, mais un signal passif mérite surveillance.")

            PassiveProblemCause.MOBILE_NETWORK_SUSPENDED,
            PassiveProblemCause.MOBILE_WEAK_SIGNAL,
            PassiveProblemCause.MOBILE_CONGESTION_SUSPECT,
            PassiveProblemCause.MOBILE_LOW_CAPACITY,
            PassiveProblemCause.MOBILE_UNVALIDATED ->
                SessionHealthAssessment(SessionHealth.WATCH, "Connexion mobile active mais un signal passif mérite surveillance.")

            PassiveProblemCause.FLAPPING ->
                SessionHealthAssessment(SessionHealth.DEGRADED, "Instabilité récente détectée.")

            PassiveProblemCause.CAPTIVE_PORTAL ->
                SessionHealthAssessment(SessionHealth.DEGRADED, "Portail captif détecté.")

            PassiveProblemCause.NO_LINK ->
                SessionHealthAssessment(SessionHealth.CRITICAL, "Aucun lien réseau actif.")

            PassiveProblemCause.UNKNOWN ->
                SessionHealthAssessment(SessionHealth.WATCH, "État encore insuffisamment caractérisé.")
        }
    }
}
