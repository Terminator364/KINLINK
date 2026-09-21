package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MobileAssistScenarioMatrixTest {
    private fun truth(
        internet: InternetState,
        budget: BudgetState = BudgetState.BALANCE_UNKNOWN,
        suspended: Boolean = false,
        congested: Boolean = false,
        down: Int = 600,
        up: Int = 200
    ) = NetworkTruth(
        transport = Transport.CELLULAR,
        internetState = internet,
        budgetState = budget,
        androidNotSuspended = !suspended,
        androidNotCongested = !congested,
        downstreamKbps = down,
        upstreamKbps = up,
        metered = true
    )

    private fun decide(
        truth: NetworkTruth,
        mode: RecoveryMode = RecoveryMode.AUTOMATIC,
        constrained: Boolean = false,
        recent: Int = 0,
        sinceLast: Long = Long.MAX_VALUE,
        ineffective: Int = 0
    ) = MobileAssistPolicy.decide(
        truth = truth,
        recoveryMode = mode,
        resourceConstrained = constrained,
        recentActions = recent,
        millisSinceLastAction = sinceLast,
        recentIneffectiveOutcomes = ineffective
    )

    @Test fun protectedBudgetMatrixNeverRunsAutomaticMobileAssist() {
        for (budget in listOf(
            BudgetState.BUNDLE_LOW,
            BudgetState.BUNDLE_EXHAUSTED,
            BudgetState.BUNDLE_EXPIRED
        )) {
            for (internet in InternetState.values()) {
                assertEquals(
                    "budget=$budget internet=$internet",
                    MobileAssistAction.NONE,
                    decide(truth(internet, budget = budget)).action
                )
            }
        }
    }

    @Test fun safeModeResourceAndSuspensionMatricesNeverRunAutomaticAssist() {
        for (internet in InternetState.values()) {
            val t = truth(internet)
            assertEquals(
                MobileAssistAction.NONE,
                decide(t, mode = RecoveryMode.OBSERVATION_ONLY).action
            )
            assertEquals(
                MobileAssistAction.NONE,
                decide(t, constrained = true).action
            )
            assertEquals(
                MobileAssistAction.NONE,
                decide(truth(internet, suspended = true)).action
            )
        }
    }

    @Test fun automaticPanelRecommendationNeverOccursForValidatedCellular() {
        for (congested in listOf(false, true)) {
            for (down in listOf(0, 600, 2_000, 20_000)) {
                assertNotEquals(
                    MobileAssistAction.OFFER_SYSTEM_CONNECTIVITY_PANEL,
                    decide(
                        truth(
                            InternetState.VALIDATED,
                            congested = congested,
                            down = down,
                            up = if (down == 0) 0 else 500
                        )
                    ).action
                )
            }
        }
    }

    @Test fun unvalidatedMobileNeverBecomesHiddenMetricRefresh() {
        for (internet in InternetState.values().filter {
            it != InternetState.VALIDATED
        }) {
            val action = decide(truth(internet)).action
            assertNotEquals(
                "internet=$internet",
                MobileAssistAction.REFRESH_LINK_METRICS,
                action
            )
        }
    }

    @Test fun cooldownHourlyCapAndAntiRepeatDominateDegradedCellular() {
        val degraded = truth(InternetState.VALIDATED)
        assertEquals(
            MobileAssistAction.NONE,
            decide(
                degraded,
                sinceLast = MobileAssistPolicy.COOLDOWN_MS - 1L
            ).action
        )
        assertEquals(
            MobileAssistAction.NONE,
            decide(
                degraded,
                recent = MobileAssistPolicy.MAX_ACTIONS_PER_HOUR
            ).action
        )
        assertEquals(
            MobileAssistAction.NONE,
            decide(
                degraded,
                ineffective = 2
            ).action
        )
    }
}
