package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeResourceQualificationPolicyTest {
    @Test fun shortSessionIsInconclusiveNotPass() {
        val a = RuntimeResourceQualificationPolicy.evaluate(
            RuntimeResourceEvidence(
                durationMillis = 10L * 60L * 1000L,
                pssDeltaMiB = 3,
                batteryPercentPerHour = null,
                backgroundChurnEvents = 10
            )
        )
        assertEquals(RuntimeResourceVerdict.INCONCLUSIVE, a.verdict)
        assertTrue(a.reasons.contains("SESSION_TOO_SHORT"))
    }

    @Test fun boundedLongSessionPasses() {
        val a = RuntimeResourceQualificationPolicy.evaluate(
            RuntimeResourceEvidence(
                durationMillis = 60L * 60L * 1000L,
                pssDeltaMiB = 8,
                batteryPercentPerHour = 1.0,
                backgroundChurnEvents = 30
            )
        )
        assertEquals(RuntimeResourceVerdict.PASS, a.verdict)
    }

    @Test fun excessivePssBlocks() {
        val a = RuntimeResourceQualificationPolicy.evaluate(
            RuntimeResourceEvidence(
                durationMillis = 60L * 60L * 1000L,
                pssDeltaMiB = 25,
                batteryPercentPerHour = 1.0,
                backgroundChurnEvents = 30
            )
        )
        assertEquals(RuntimeResourceVerdict.BLOCKED, a.verdict)
        assertTrue(a.reasons.contains("PSS_GROWTH_OVER_LIMIT"))
    }

    @Test fun churnBudgetScalesWithSessionDuration() {
        val a = RuntimeResourceQualificationPolicy.evaluate(
            RuntimeResourceEvidence(
                durationMillis = 60L * 60L * 1000L,
                pssDeltaMiB = 2,
                batteryPercentPerHour = 0.8,
                backgroundChurnEvents = 121
            )
        )
        assertEquals(RuntimeResourceVerdict.PASS, a.verdict)
    }

    @Test fun excessiveBackgroundChurnRateBlocks() {
        val a = RuntimeResourceQualificationPolicy.evaluate(
            RuntimeResourceEvidence(
                durationMillis = 60L * 60L * 1000L,
                pssDeltaMiB = 2,
                batteryPercentPerHour = 0.8,
                backgroundChurnEvents = 241
            )
        )
        assertEquals(RuntimeResourceVerdict.BLOCKED, a.verdict)
        assertTrue(a.reasons.contains("BACKGROUND_CHURN_OVER_LIMIT"))
    }

    @Test fun chargingOrCoarseBatteryEvidenceCannotProducePass() {
        val a = RuntimeResourceQualificationPolicy.evaluate(
            RuntimeResourceEvidence(
                durationMillis = 60L * 60L * 1000L,
                pssDeltaMiB = 2,
                batteryPercentPerHour = null,
                backgroundChurnEvents = 10
            )
        )
        assertEquals(RuntimeResourceVerdict.INCONCLUSIVE, a.verdict)
        assertTrue(a.reasons.contains("BATTERY_RATE_INCONCLUSIVE"))
    }
}
