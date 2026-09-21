package com.terminator364.kinlink.core

enum class ContinuousControlState {
    SAFE_MODE,
    RESOURCE_PROTECTED,
    HEALTHY_MONITORING,
    VERIFYING,
    MAINTAINING,
    RELAPSED,
    NO_CONFIRMED_BENEFIT,
    COLLECTING
}

data class ContinuousControlPanel(
    val state: ContinuousControlState,
    val title: String,
    val beforeScore: Int?,
    val nowScore: Int,
    val delta: Int?,
    val maintenance: String,
    val evidence: String
)

object ContinuousControlPanelPolicy {
    private val baselineRegex = Regex("""baseline=[A-Z_]+/(\d{1,3})""")
    private val currentRegex = Regex("""current=[A-Z_]+/(\d{1,3})""")

    fun build(
        currentScore: Int,
        observationOnly: Boolean,
        resourceConstrained: Boolean,
        latestEvidenceAction: String?,
        latestEvidenceSummary: String?,
        latestEvidenceAgeMillis: Long?
    ): ContinuousControlPanel {
        val safeNow = currentScore.coerceIn(0, 100)
        val before = latestEvidenceSummary
            ?.let { baselineRegex.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() }
            ?.coerceIn(0, 100)
        val delta = before?.let { safeNow - it }

        if (observationOnly) {
            return ContinuousControlPanel(
                ContinuousControlState.SAFE_MODE,
                "Mode sûr · surveillance seulement",
                before,
                safeNow,
                delta,
                "Maintien · action active suspendue",
                evidenceLabel(latestEvidenceAction, latestEvidenceAgeMillis)
            )
        }

        if (resourceConstrained) {
            return ContinuousControlPanel(
                ContinuousControlState.RESOURCE_PROTECTED,
                "Protection téléphone · surveillance",
                before,
                safeNow,
                delta,
                "Maintien · priorité batterie/RAM/température",
                evidenceLabel(latestEvidenceAction, latestEvidenceAgeMillis)
            )
        }

        val action = latestEvidenceAction.orEmpty()
        return when {
            action.endsWith("SUSTAINED_BETTER") &&
                before != null &&
                safeNow > before + MobileAssistEvidenceTracker.RELAPSE_TOLERANCE_POINTS ->
                ContinuousControlPanel(
                    ContinuousControlState.MAINTAINING,
                    "Maintien actif · mieux confirmé",
                    before,
                    safeNow,
                    delta,
                    "Maintien · qualité toujours au-dessus du niveau avant action",
                    "Preuve · amélioration soutenue corrélée, surveillance de rechute active"
                )

            action.endsWith("SUSTAINED_BETTER") && before != null ->
                ContinuousControlPanel(
                    ContinuousControlState.RELAPSED,
                    "Rechute détectée · réévaluation",
                    before,
                    safeNow,
                    delta,
                    "Maintien · retour proche du niveau initial",
                    "Preuve · amélioration antérieure non maintenue"
                )

            action.endsWith("RELAPSED_AFTER_SUSTAINED") ||
                action.endsWith("RELAPSED") ->
                ContinuousControlPanel(
                    ContinuousControlState.RELAPSED,
                    "Rechute détectée · réévaluation",
                    before,
                    safeNow,
                    delta,
                    "Maintien · amélioration non conservée",
                    "Preuve · rechute enregistrée; nouvelle action seulement après garde-fous"
                )

            action.endsWith("NO_BETTER") ->
                ContinuousControlPanel(
                    ContinuousControlState.NO_CONFIRMED_BENEFIT,
                    "Aucun mieux confirmé · pas d’acharnement",
                    before,
                    safeNow,
                    delta,
                    "Maintien · aucune amélioration à maintenir",
                    "Preuve · action sans bénéfice confirmé; anti-répétition actif"
                )

            action.endsWith("METRICS_AVAILABLE") ->
                ContinuousControlPanel(
                    ContinuousControlState.VERIFYING,
                    "Mesure actualisée · bénéfice à vérifier",
                    before,
                    safeNow,
                    delta,
                    "Maintien · observation en cours",
                    "Preuve · métriques Android actualisées; débit non attribué à KINLINK"
                )

            latestEvidenceAction != null &&
                latestEvidenceAgeMillis != null &&
                latestEvidenceAgeMillis <= MobileAssistEvidenceTracker.MAX_EVIDENCE_WINDOW_MS ->
                ContinuousControlPanel(
                    ContinuousControlState.VERIFYING,
                    "Vérification après action",
                    before,
                    safeNow,
                    delta,
                    "Maintien · confirmation bornée en cours",
                    "Preuve · comparaison avant/maintenant sans speedtest"
                )

            safeNow >= 75 ->
                ContinuousControlPanel(
                    ContinuousControlState.HEALTHY_MONITORING,
                    "Connexion dans la zone cible",
                    before,
                    safeNow,
                    delta,
                    "Maintien · surveillance événementielle",
                    evidenceLabel(latestEvidenceAction, latestEvidenceAgeMillis)
                )

            else ->
                ContinuousControlPanel(
                    ContinuousControlState.COLLECTING,
                    "Qualité sous surveillance",
                    before,
                    safeNow,
                    delta,
                    "Maintien · attente d’une preuve suffisante",
                    evidenceLabel(latestEvidenceAction, latestEvidenceAgeMillis)
                )
        }
    }

    private fun evidenceLabel(action: String?, ageMillis: Long?): String {
        if (action == null) return "Preuve · en collecte"
        val age = ageMillis?.coerceAtLeast(0L)
        val ageText = when {
            age == null -> ""
            age < 60_000L -> " · il y a <1 min"
            age < 3_600_000L -> " · il y a " + (age / 60_000L) + " min"
            else -> " · il y a " + (age / 3_600_000L) + " h"
        }
        return "Preuve · " +
            action.removePrefix("MOBILE_ASSIST_EVIDENCE_")
                .lowercase()
                .replace('_', ' ') +
            ageText
    }
}
