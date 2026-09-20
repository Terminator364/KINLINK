package com.terminator364.kinlink.core

/**
 * Product-facing state derived only from Android observations and explicit local policy.
 * It never triggers a probe, enables mobile data or changes Android routing.
 */
enum class OperationalState {
    WIFI_HEALTHY, WIFI_DEGRADED, WIFI_BROWNOUT,
    MOBILE_HEALTHY, MOBILE_DEGRADED,
    DATA_LOW, DATA_EXHAUSTED, DATA_EXPIRED, BALANCE_UNKNOWN,
    LAN_OK_WAN_DOWN, OFFLINE, RECOVERING
}

data class ConnectivityAssessment(
    val state: OperationalState,
    val headline: String,
    val explanation: String,
    val preserveLan: Boolean,
    val avoidAutomaticMobileUse: Boolean
)

object ConnectivityStateClassifier {
    fun classify(truth: NetworkTruth): ConnectivityAssessment = when {
        truth.transport == Transport.NONE || truth.internetState == InternetState.OFFLINE ->
            ConnectivityAssessment(
                OperationalState.OFFLINE,
                "Vous êtes hors ligne",
                "KINLINK n’essaie pas de réveiller les données mobiles automatiquement.",
                preserveLan = false,
                avoidAutomaticMobileUse = true
            )

        truth.transport == Transport.WIFI && truth.internetState == InternetState.VALIDATED ->
            ConnectivityAssessment(
                OperationalState.WIFI_HEALTHY,
                "Wi-Fi prêt",
                "Internet est disponible par Wi-Fi. Les données mobiles restent préservées.",
                preserveLan = true,
                avoidAutomaticMobileUse = true
            )

        truth.transport == Transport.WIFI && truth.internetState == InternetState.CAPTIVE_PORTAL ->
            ConnectivityAssessment(
                OperationalState.WIFI_BROWNOUT,
                "Connexion Wi-Fi requise",
                "Le Wi-Fi demande une connexion. KINLINK ne bascule pas silencieusement sur le mobile.",
                preserveLan = true,
                avoidAutomaticMobileUse = true
            )

        truth.transport == Transport.WIFI && truth.lanState == LanState.LINK_PRESENT ->
            ConnectivityAssessment(
                OperationalState.LAN_OK_WAN_DOWN,
                "Réseau local disponible",
                "Le Wi-Fi local est présent mais Internet n’est pas confirmé. Les fonctions LAN restent possibles.",
                preserveLan = true,
                avoidAutomaticMobileUse = true
            )

        truth.transport == Transport.WIFI ->
            ConnectivityAssessment(
                OperationalState.WIFI_DEGRADED,
                "Wi-Fi à vérifier",
                "KINLINK observe sans lancer de test de données.",
                preserveLan = true,
                avoidAutomaticMobileUse = true
            )

        truth.transport == Transport.CELLULAR && truth.budgetState == BudgetState.BUNDLE_EXHAUSTED ->
            ConnectivityAssessment(OperationalState.DATA_EXHAUSTED, "Forfait épuisé", "KINLINK n’effectue aucun retry coûteux.", false, true)

        truth.transport == Transport.CELLULAR && truth.budgetState == BudgetState.BUNDLE_EXPIRED ->
            ConnectivityAssessment(OperationalState.DATA_EXPIRED, "Forfait expiré", "KINLINK attend une action explicite.", false, true)

        truth.transport == Transport.CELLULAR && truth.budgetState == BudgetState.BUNDLE_LOW ->
            ConnectivityAssessment(OperationalState.DATA_LOW, "Données mobiles limitées", "KINLINK évite toute vérification qui consommerait des données.", false, true)

        truth.transport == Transport.CELLULAR && truth.internetState == InternetState.VALIDATED ->
            ConnectivityAssessment(OperationalState.MOBILE_HEALTHY, "Données mobiles disponibles", "Connexion mobile active, sans test automatique.", false, true)

        truth.transport == Transport.CELLULAR ->
            ConnectivityAssessment(OperationalState.MOBILE_DEGRADED, "Données mobiles à vérifier", "KINLINK n’insiste pas par retries ni gros probes.", false, true)

        else -> ConnectivityAssessment(OperationalState.RECOVERING, "Vérification en cours", "KINLINK attend une observation fiable avant toute décision.", false, true)
    }
}
