package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryQualificationPolicyTest {
    @Test fun blocksWithoutEvidence() {
        val result = RecoveryQualificationPolicy.evaluate(RecoveryQualificationEvidence())
        assertEquals(RecoveryQualificationVerdict.BLOCKED, result.verdict)
        assertTrue(result.failedOrMissingGates.contains(RecoveryGate.WATCHDOG_TEARDOWN))
        assertFalse(RecoveryFeatureGate.productionEnabled(result))
    }

    @Test fun completeEvidenceOnlyQualifiesForFieldTrial() {
        val result = RecoveryQualificationPolicy.evaluate(
            RecoveryQualificationEvidence(
                enginePrototypePass = true,
                nameResolutionPass = true,
                watchdogTeardownPass = true,
                rollbackPass = true,
                ramDeltaMiB = 12,
                batteryPercentPerHour = 0.8,
                addedLatencyMillis = 4
            )
        )
        assertEquals(RecoveryQualificationVerdict.ELIGIBLE_FOR_FIELD_TRIAL, result.verdict)
        assertTrue(result.failedOrMissingGates.isEmpty())
        assertFalse(RecoveryFeatureGate.productionEnabled(result))
    }

    @Test fun resourceRegressionPreventsQualification() {
        val result = RecoveryQualificationPolicy.evaluate(
            RecoveryQualificationEvidence(
                enginePrototypePass = true,
                nameResolutionPass = true,
                watchdogTeardownPass = true,
                rollbackPass = true,
                ramDeltaMiB = 64,
                batteryPercentPerHour = 0.8,
                addedLatencyMillis = 4
            )
        )
        assertTrue(result.failedOrMissingGates.contains(RecoveryGate.RESOURCE_BUDGET))
    }
}
