package com.terminator364.kinlink.core

enum class HandoffOutcome {
    NONE,
    MOBILE_VALIDATED,
    MOBILE_PRESENT_UNVALIDATED,
    WIFI_RECOVERED
}

data class HandoffOutcomeEvidence(
    val outcome: HandoffOutcome,
    val summary: String
)

class HandoffOutcomeTracker {
    private var awaitingMobileOutcome = false

    fun onTransition(transition: HandoffTransition) {
        if (transition.kind == HandoffKind.WIFI_TO_CELLULAR ||
            transition.kind == HandoffKind.WIFI_TO_NONE
        ) {
            awaitingMobileOutcome = true
        }
        if (transition.kind == HandoffKind.CELLULAR_TO_WIFI) {
            awaitingMobileOutcome = false
        }
    }

    fun observe(truth: NetworkTruth): HandoffOutcomeEvidence? {
        if (!awaitingMobileOutcome) return null

        return when {
            truth.transport == Transport.CELLULAR &&
                truth.internetState == InternetState.VALIDATED -> {
                awaitingMobileOutcome = false
                HandoffOutcomeEvidence(
                    HandoffOutcome.MOBILE_VALIDATED,
                    "Handoff terminé : Android signale les données mobiles VALIDATED; KINLINK est resté observation-only."
                )
            }
            truth.transport == Transport.CELLULAR -> {
                HandoffOutcomeEvidence(
                    HandoffOutcome.MOBILE_PRESENT_UNVALIDATED,
                    "Transport mobile présent mais Internet pas encore VALIDATED; aucune action KINLINK."
                )
            }
            truth.transport == Transport.WIFI &&
                truth.internetState == InternetState.VALIDATED -> {
                awaitingMobileOutcome = false
                HandoffOutcomeEvidence(
                    HandoffOutcome.WIFI_RECOVERED,
                    "Le Wi-Fi est redevenu validé avant confirmation mobile; KINLINK n’a pris aucun contrôle du routage."
                )
            }
            else -> null
        }
    }
}
