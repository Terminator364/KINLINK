package com.terminator364.kinlink.core

enum class VaultAction {
    NO_ACTION,
    HOLD_MOBILE_RECOVERY,
    KEEP_WIFI_PREFERRED,
    PRESERVE_LAN
}

data class SafeDecision(
    val why: String,
    val what: VaultAction,
    val result: String
)

/** Local policy only. This does not enable/disable mobile data or change Android routing. */
object MobileVault {
    fun decide(truth: NetworkTruth, assessment: ConnectivityAssessment): SafeDecision = when {
        truth.budgetState == BudgetState.BUNDLE_EXHAUSTED ->
            SafeDecision(
                "Plafond mobile KINLINK atteint",
                VaultAction.HOLD_MOBILE_RECOVERY,
                "Aucun retry ni probe mobile"
            )
        truth.budgetState == BudgetState.BUNDLE_EXPIRED ->
            SafeDecision(
                "Forfait signalé expiré",
                VaultAction.HOLD_MOBILE_RECOVERY,
                "Attente d’une action explicite"
            )
        truth.budgetState == BudgetState.BUNDLE_LOW ->
            SafeDecision(
                "Budget mobile proche du plafond",
                VaultAction.HOLD_MOBILE_RECOVERY,
                "Probes et retries mobiles KINLINK suspendus"
            )
        truth.transport == Transport.WIFI && assessment.preserveLan ->
            SafeDecision(
                "Wi-Fi local présent",
                VaultAction.KEEP_WIFI_PREFERRED,
                "Données mobiles préservées"
            )
        assessment.preserveLan ->
            SafeDecision(
                "LAN disponible sans WAN confirmé",
                VaultAction.PRESERVE_LAN,
                "Aucun changement réseau"
            )
        else ->
            SafeDecision(
                "État insuffisant ou mobile actif",
                VaultAction.NO_ACTION,
                "Mode sûr : Android conserve le contrôle"
            )
    }
}

data class WifiDoctorAdvice(val title: String, val message: String)

object WifiDoctor {
    fun advise(assessment: ConnectivityAssessment): WifiDoctorAdvice = when (assessment.state) {
        OperationalState.WIFI_HEALTHY ->
            WifiDoctorAdvice("Wi-Fi utilisable", "Aucune réparation nécessaire.")
        OperationalState.LAN_OK_WAN_DOWN ->
            WifiDoctorAdvice(
                "Wi-Fi local conservé",
                "Internet n’est pas confirmé. Les fonctions locales restent disponibles."
            )
        OperationalState.WIFI_BROWNOUT ->
            WifiDoctorAdvice(
                "Connexion Wi-Fi requise",
                "Termine la connexion au Wi-Fi. KINLINK ne force pas le mobile."
            )
        OperationalState.WIFI_DEGRADED ->
            WifiDoctorAdvice(
                "Wi-Fi à surveiller",
                "KINLINK attend les observations réelles au lieu de lancer un test coûteux."
            )
        else -> WifiDoctorAdvice("Autopilot prudent", assessment.explanation)
    }
}
