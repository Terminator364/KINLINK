package com.terminator364.kinlink.core

import kotlin.math.min

data class StabilityAssessment(
    val score: Int,
    val label: String,
    val flapping: Boolean
)

object NetworkStabilityPolicy {
    /**
     * Converts a bounded event window into a 0..100 instability score.
     * The score is descriptive only; it does not trigger traffic by itself.
     */
    fun assess(
        events: Int,
        transitions: Int,
        validatedEvents: Int
    ): StabilityAssessment {
        if (events <= 0) return StabilityAssessment(0, "Aucune donnée récente", false)

        val safeTransitions = transitions.coerceIn(0, events)
        val safeValidated = validatedEvents.coerceIn(0, events)
        val negativeEvents = events - safeValidated

        val transitionWeight = safeTransitions * 16
        val negativeWeight = (negativeEvents * 40) / events
        val score = min(100, transitionWeight + negativeWeight)

        val flapping = events >= 4 && safeTransitions >= 3 && score >= 50
        val label = when {
            flapping -> "Connexion instable"
            score >= 50 -> "Connexion à surveiller"
            score >= 20 -> "Variations légères"
            else -> "Connexion stable"
        }
        return StabilityAssessment(score, label, flapping)
    }
}
