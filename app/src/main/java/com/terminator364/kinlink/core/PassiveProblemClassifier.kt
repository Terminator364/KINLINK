package com.terminator364.kinlink.core

enum class PassiveProblemCause {
    NONE,
    NO_LINK,
    CAPTIVE_PORTAL,
    ADDRESSING_SUSPECT,
    ROUTE_CONFIGURATION_SUSPECT,
    DNS_CONFIGURATION_SUSPECT,
    NETWORK_SUSPENDED,
    WEAK_WIFI_SIGNAL,
    CONGESTION_SUSPECT,
    LOW_CAPACITY,
    FLAPPING,
    WAN_UNVALIDATED,
    MOBILE_UNVALIDATED,
    UNKNOWN
}

data class PassiveProblemAssessment(
    val cause: PassiveProblemCause,
    val summary: String,
    val confidence: Int
)

/**
 * Passive-only diagnosis. No packet is emitted here.
 * "DNS_CONFIGURATION_SUSPECT" means Android exposes no DNS server on a linked,
 * unvalidated Wi-Fi; it is not proof of a DNS outage.
 */
object PassiveProblemClassifier {
    fun classify(
        truth: NetworkTruth,
        instabilityScore: Int = 0,
        flapping: Boolean = false
    ): PassiveProblemAssessment = when {
        truth.transport == Transport.NONE || truth.internetState == InternetState.OFFLINE ->
            PassiveProblemAssessment(PassiveProblemCause.NO_LINK, "Aucun réseau actif observé.", 95)

        truth.internetState == InternetState.CAPTIVE_PORTAL ->
            PassiveProblemAssessment(PassiveProblemCause.CAPTIVE_PORTAL, "Portail captif signalé par Android.", 95)

        truth.transport == Transport.WIFI &&
            truth.lanState == LanState.LINK_PRESENT &&
            truth.internetState != InternetState.VALIDATED &&
            !truth.hasIpv4Address &&
            !truth.hasIpv6Address ->
            PassiveProblemAssessment(
                PassiveProblemCause.ADDRESSING_SUSPECT,
                "Wi-Fi local présent mais Android n’expose aucune adresse IP utilisable.",
                80
            )

        truth.transport == Transport.WIFI &&
            truth.lanState == LanState.LINK_PRESENT &&
            truth.internetState != InternetState.VALIDATED &&
            !truth.hasIpv4DefaultRoute &&
            !truth.hasIpv6DefaultRoute ->
            PassiveProblemAssessment(
                PassiveProblemCause.ROUTE_CONFIGURATION_SUSPECT,
                "Wi-Fi local présent mais Android n’expose aucune route par défaut.",
                78
            )

        truth.transport == Transport.WIFI &&
            truth.lanState == LanState.LINK_PRESENT &&
            truth.internetState != InternetState.VALIDATED &&
            truth.dnsServerCount == 0 ->
            PassiveProblemAssessment(
                PassiveProblemCause.DNS_CONFIGURATION_SUSPECT,
                "Wi-Fi local présent, Internet non validé et aucun serveur DNS exposé par Android.",
                70
            )

        truth.transport == Transport.WIFI &&
            !truth.androidNotSuspended ->
            PassiveProblemAssessment(
                PassiveProblemCause.NETWORK_SUSPENDED,
                "Android signale le réseau Wi-Fi comme suspendu ou non disponible pour le trafic courant.",
                88
            )

        truth.transport == Transport.WIFI &&
            WifiRadioQualityPolicy.assess(truth).quality == WifiRadioQuality.WEAK ->
            PassiveProblemAssessment(
                PassiveProblemCause.WEAK_WIFI_SIGNAL,
                "Le Wi-Fi est actif mais le signal radio exposé par Android est faible.",
                80
            )

        truth.transport == Transport.WIFI &&
            truth.internetState == InternetState.VALIDATED &&
            !truth.androidNotCongested &&
            (
                PassiveLinkQualityPolicy.assess(truth).quality == PassiveLinkQuality.CONSTRAINED ||
                PassiveLinkQualityPolicy.assess(truth).quality == PassiveLinkQuality.LIMITED
            ) ->
            PassiveProblemAssessment(
                PassiveProblemCause.CONGESTION_SUSPECT,
                "Internet est validé mais Android ne signale pas le réseau comme non congestionné et la capacité passive est limitée.",
                68
            )

        truth.transport == Transport.WIFI &&
            truth.internetState == InternetState.VALIDATED &&
            PassiveLinkQualityPolicy.assess(truth).quality == PassiveLinkQuality.CONSTRAINED ->
            PassiveProblemAssessment(
                PassiveProblemCause.LOW_CAPACITY,
                "Internet validé, mais capacité passive Android très faible.",
                75
            )

        flapping || instabilityScore >= 60 ->
            PassiveProblemAssessment(
                PassiveProblemCause.FLAPPING,
                "Transitions réseau répétées dans la fenêtre récente.",
                80
            )

        truth.transport == Transport.WIFI &&
            truth.lanState == LanState.LINK_PRESENT &&
            truth.internetState != InternetState.VALIDATED ->
            PassiveProblemAssessment(
                PassiveProblemCause.WAN_UNVALIDATED,
                "Lien Wi-Fi local présent mais Internet non validé.",
                65
            )

        truth.transport == Transport.CELLULAR &&
            truth.internetState != InternetState.VALIDATED ->
            PassiveProblemAssessment(
                PassiveProblemCause.MOBILE_UNVALIDATED,
                "Transport mobile présent mais Internet non validé; KINLINK n’agit pas dessus.",
                70
            )

        truth.internetState == InternetState.VALIDATED ->
            PassiveProblemAssessment(PassiveProblemCause.NONE, "Aucune panne passive certaine détectée.", 90)

        else ->
            PassiveProblemAssessment(PassiveProblemCause.UNKNOWN, "Éléments insuffisants pour isoler la cause.", 40)
    }
}
