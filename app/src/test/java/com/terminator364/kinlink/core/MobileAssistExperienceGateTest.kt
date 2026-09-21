package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MobileAssistExperienceGateTest {
    @Test fun degradedRecentExperienceTriggersBoundedReevaluationEvenWhenFrameworkEstimateLooksGood() {
        val truth = NetworkTruth(
            transport = Transport.CELLULAR,
            internetState = InternetState.VALIDATED,
            downstreamKbps = 20_000,
            upstreamKbps = 5_000,
            androidNotCongested = true,
            androidNotSuspended = true,
            metered = true
        )
        val decision = MobileAssistPolicy.decide(
            truth = truth,
            recoveryMode = RecoveryMode.AUTOMATIC,
            resourceConstrained = false,
            recentActions = 0,
            millisSinceLastAction = Long.MAX_VALUE,
            recentIneffectiveOutcomes = 0,
            recentExperienceDegraded = true
        )
        assertEquals(MobileAssistAction.REFRESH_LINK_METRICS, decision.action)
    }
}
