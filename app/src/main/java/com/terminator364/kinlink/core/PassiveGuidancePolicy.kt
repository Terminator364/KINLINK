package com.terminator364.kinlink.core

data class PassiveGuidance(
    val title: String,
    val message: String
)

object PassiveGuidancePolicy {
    fun guidance(assessment: PassiveProblemAssessment): PassiveGuidance = when (assessment.cause) {
        PassiveProblemCause.NONE ->
            PassiveGuidance("Connexion utilisable", "Aucune action particulière recommandée.")

        PassiveProblemCause.NO_LINK ->
            PassiveGuidance("Aucun lien réseau", "Vérifie simplement que le Wi-Fi ou les données mobiles sont activés. KINLINK ne force aucune bascule.")

        PassiveProblemCause.CAPTIVE_PORTAL ->
            PassiveGuidance("Connexion Wi-Fi requise", "Ouvre la page de connexion du réseau. KINLINK ne contourne pas le portail captif.")

        PassiveProblemCause.ADDRESSING_SUSPECT ->
            PassiveGuidance(
                "Adresse IP à surveiller",
                "Le Wi-Fi est associé mais Android n’expose pas encore d’adresse IP utilisable. Attends quelques secondes ou reconnecte le Wi-Fi si cela persiste."
            )

        PassiveProblemCause.ROUTE_CONFIGURATION_SUSPECT ->
            PassiveGuidance(
                "Route réseau à surveiller",
                "Le Wi-Fi est associé mais aucune route par défaut n’est exposée par Android. KINLINK ne force aucune bascule."
            )

        PassiveProblemCause.DNS_CONFIGURATION_SUSPECT ->
            PassiveGuidance(
                "DNS à surveiller",
                "Android n’expose aucun serveur DNS sur ce Wi-Fi alors qu’un lien local existe. Attends la stabilisation ou reconnecte le Wi-Fi si cela persiste."
            )

        PassiveProblemCause.NETWORK_SUSPENDED ->
            PassiveGuidance(
                "Réseau suspendu",
                "Android signale momentanément le Wi-Fi comme suspendu. KINLINK attend la reprise et n’envoie aucun trafic de récupération."
            )

        PassiveProblemCause.WEAK_WIFI_SIGNAL ->
            PassiveGuidance(
                "Signal Wi-Fi faible",
                "Rapproche-toi du point d’accès si possible ou réduis les obstacles. KINLINK n’augmente pas la puissance radio et n’effectue aucune bascule forcée."
            )

        PassiveProblemCause.CONGESTION_SUSPECT ->
            PassiveGuidance(
                "Congestion possible",
                "Android signale une capacité limitée et ne confirme pas l’état non congestionné. KINLINK observe plusieurs mesures avant toute action légère."
            )

        PassiveProblemCause.LOW_CAPACITY ->
            PassiveGuidance(
                "Wi-Fi lent mais connecté",
                "Internet est validé mais la capacité estimée est faible. Évite les gros transferts; KINLINK attend plusieurs observations avant toute récupération."
            )

        PassiveProblemCause.FLAPPING ->
            PassiveGuidance(
                "Connexion instable",
                "Plusieurs transitions rapprochées ont été observées. KINLINK privilégie la stabilité et évite les actions répétées."
            )

        PassiveProblemCause.WAN_UNVALIDATED ->
            PassiveGuidance(
                "Internet non confirmé",
                "Le réseau local est présent mais Android ne valide pas encore Internet. Les fonctions LAN restent préservées."
            )

        PassiveProblemCause.MOBILE_NETWORK_SUSPENDED ->
            PassiveGuidance(
                "Réseau mobile suspendu",
                "Android signale momentanément les données mobiles comme suspendues. KINLINK n’envoie aucun probe payant et attend la reprise."
            )

        PassiveProblemCause.MOBILE_CONGESTION_SUSPECT ->
            PassiveGuidance(
                "Congestion mobile possible",
                "Internet mobile est validé mais la capacité passive est limitée. Mobile Assist peut rafraîchir les métriques Android sans speedtest ni trafic de test."
            )

        PassiveProblemCause.MOBILE_LOW_CAPACITY ->
            PassiveGuidance(
                "Données mobiles lentes",
                "Internet mobile est disponible mais la capacité estimée est faible. Mobile Assist privilégie les actions zéro-probe et protège le forfait."
            )

        PassiveProblemCause.MOBILE_UNVALIDATED ->
            PassiveGuidance(
                "Données mobiles non confirmées",
                "Android voit le transport mobile mais ne valide pas Internet. KINLINK peut proposer le panneau système de connectivité sans lancer de probe mobile."
            )

        PassiveProblemCause.UNKNOWN ->
            PassiveGuidance("Analyse en cours", "Les éléments disponibles ne suffisent pas encore pour isoler la cause.")
    }
}

class PassiveProblemTransitionTracker {
    private var previous: PassiveProblemCause? = null

    fun observe(current: PassiveProblemCause): PassiveProblemCause? {
        val before = previous
        previous = current
        if (before == null || before == current) return null
        return current
    }
}
