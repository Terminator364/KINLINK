package com.terminator364.kinlink.core

import org.junit.Assert.assertTrue
import org.junit.Test

class AutopilotProfileControlPolicyTest {
    @Test fun maximumStabilityIsMoreResponsiveButStillBounded() {
        val conservative =
            AutopilotProfileControlPolicy.tuning(AutopilotProfile.CONSERVATIVE)
        val balanced =
            AutopilotProfileControlPolicy.tuning(AutopilotProfile.BALANCED)
        val maximum =
            AutopilotProfileControlPolicy.tuning(AutopilotProfile.MAXIMUM_STABILITY)

        assertTrue(maximum.vigilanceFloor > balanced.vigilanceFloor)
        assertTrue(balanced.vigilanceFloor > conservative.vigilanceFloor)
        assertTrue(maximum.mobileAssistCooldownMs < balanced.mobileAssistCooldownMs)
        assertTrue(balanced.mobileAssistCooldownMs < conservative.mobileAssistCooldownMs)
        assertTrue(maximum.mobileAssistMaxActionsPerHour > balanced.mobileAssistMaxActionsPerHour)
        assertTrue(balanced.mobileAssistMaxActionsPerHour > conservative.mobileAssistMaxActionsPerHour)
        assertTrue(maximum.mobileAssistMaxActionsPerHour <= 8)
    }
}
