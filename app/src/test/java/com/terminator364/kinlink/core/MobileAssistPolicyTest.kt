package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MobileAssistPolicyTest {
    private fun cellular(
        internet: InternetState = InternetState.VALIDATED,
        down: Int = 10_000,
        up: Int = 2_000,
        congested: Boolean = false,
        suspended: Boolean = false,
        budget: BudgetState = BudgetState.BALANCE_UNKNOWN
    ) = NetworkTruth(
        transport = Transport.CELLULAR,
        internetState = internet,
        downstreamKbps = down,
        upstreamKbps = up,
        androidNotCongested = !congested,
        androidNotSuspended = !suspended,
        budgetState = budget,
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
        truth,
        mode,
        constrained,
        recent,
        sinceLast,
        ineffective
    )

    @Test fun healthyValidatedCellularIsLeftAlone() {
        assertEquals(MobileAssistAction.NONE, decide(cellular()).action)
    }

    @Test fun constrainedValidatedCellularCanRefreshMetricsWithoutProbe() {
        assertEquals(
            MobileAssistAction.REFRESH_LINK_METRICS,
            decide(cellular(down = 600, up = 200)).action
        )
    }

    @Test fun congestedValidatedCellularCanRefreshMetrics() {
        assertEquals(
            MobileAssistAction.REFRESH_LINK_METRICS,
            decide(cellular(congested = true)).action
        )
    }

    @Test fun unvalidatedCellularOffersSystemPanelInsteadOfHiddenProbe() {
        assertEquals(
            MobileAssistAction.OFFER_SYSTEM_CONNECTIVITY_PANEL,
            decide(cellular(internet = InternetState.UNKNOWN)).action
        )
    }

    @Test fun observationOnlyBlocksMobileAssist() {
        assertEquals(
            MobileAssistBlockReason.OBSERVATION_ONLY,
            decide(cellular(down = 600), mode = RecoveryMode.OBSERVATION_ONLY).blockReason
        )
    }

    @Test fun resourcePressureBlocksMobileAssist() {
        assertEquals(
            MobileAssistBlockReason.RESOURCE_CONSTRAINED,
            decide(cellular(down = 600), constrained = true).blockReason
        )
    }

    @Test fun suspendedCellularNeverTriggersAction() {
        assertEquals(
            MobileAssistBlockReason.NETWORK_SUSPENDED,
            decide(cellular(down = 600, suspended = true)).blockReason
        )
    }

    @Test fun lowOrExhaustedBudgetBlocksMobileAssist() {
        for (budget in listOf(
            BudgetState.BUNDLE_LOW,
            BudgetState.BUNDLE_EXHAUSTED,
            BudgetState.BUNDLE_EXPIRED
        )) {
            assertEquals(
                MobileAssistBlockReason.BUDGET_PROTECTED,
                decide(cellular(down = 600, budget = budget)).blockReason
            )
        }
    }

    @Test fun cooldownAndHourlyCapAreHardBounds() {
        assertEquals(
            MobileAssistBlockReason.COOLDOWN,
            decide(
                cellular(down = 600),
                sinceLast = MobileAssistPolicy.COOLDOWN_MS - 1
            ).blockReason
        )
        assertEquals(
            MobileAssistBlockReason.HOURLY_CAP,
            decide(
                cellular(down = 600),
                recent = MobileAssistPolicy.MAX_ACTIONS_PER_HOUR
            ).blockReason
        )
    }

    @Test fun repeatedIneffectiveAssistTriggersAntiRepeatPause() {
        assertEquals(
            MobileAssistBlockReason.INEFFECTIVE_RECENTLY,
            decide(
                cellular(down = 600, up = 200),
                ineffective = 2
            ).blockReason
        )
    }

    @Test fun manualAssistHasShortAntiSpamCooldownAndHourlyBound() {
        fun decision(
            validated: Boolean = true,
            notSuspended: Boolean = true,
            observationOnly: Boolean = false,
            budgetProtected: Boolean = false,
            resourceConstrained: Boolean = false,
            recentActions: Int = 0,
            sinceLast: Long = Long.MAX_VALUE
        ) = MobileAssistManualPolicy.decide(
            isCellular = true,
            validated = validated,
            notSuspended = notSuspended,
            observationOnly = observationOnly,
            budgetProtected = budgetProtected,
            resourceConstrained = resourceConstrained,
            recentActions = recentActions,
            millisSinceLastAction = sinceLast
        )

        assertEquals(
            MobileAssistManualBlockReason.COOLDOWN,
            decision(sinceLast = 29_999L).blockReason
        )
        assertEquals(
            MobileAssistManualAction.REFRESH_LINK_METRICS,
            decision(sinceLast = 30_000L).action
        )
        assertEquals(
            MobileAssistManualBlockReason.HOURLY_CAP,
            decision(
                recentActions = MobileAssistPolicy.MAX_ACTIONS_PER_HOUR,
                sinceLast = 30_000L
            ).blockReason
        )
        assertEquals(
            MobileAssistManualBlockReason.RESOURCE_CONSTRAINED,
            decision(
                resourceConstrained = true,
                sinceLast = 30_000L
            ).blockReason
        )
    }

    @Test fun explicitPanelNavigationRemainsAvailableWithoutHiddenProbe() {
        val d = MobileAssistManualPolicy.decide(
            isCellular = true,
            validated = false,
            notSuspended = true,
            observationOnly = true,
            budgetProtected = true,
            resourceConstrained = true,
            recentActions = 0,
            millisSinceLastAction = Long.MAX_VALUE
        )
        assertEquals(
            MobileAssistManualAction.OPEN_SYSTEM_CONNECTIVITY_PANEL,
            d.action
        )
        assertEquals(MobileAssistManualBlockReason.NONE, d.blockReason)
    }

    @Test fun everyNonCellularTransportIsBlocked() {
        for (transport in Transport.values().filter { it != Transport.CELLULAR }) {
            val truth = cellular(down = 600).copy(transport = transport)
            assertEquals(
                "transport=$transport",
                MobileAssistBlockReason.NOT_CELLULAR,
                decide(truth).blockReason
            )
        }
    }
}
