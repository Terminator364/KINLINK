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

        PassiveProblemCause.MOBILE_UNVALIDATED ->
            PassiveGuidance(
                "Données mobiles non confirmées",
                "Android voit le transport mobile mais ne valide pas Internet. KINLINK observe seulement et ne relance pas les données mobiles."
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
