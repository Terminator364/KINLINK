package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldCandidateQualificationPolicyTest {
    @Test fun allEvidencePasses() {
        val a = FieldCandidateQualificationPolicy.evaluate(
            FieldCandidateQualificationEvidence(1, 1, 1, 1, 1, 0)
        )
        assertEquals(FieldCandidateQualificationVerdict.PASS, a.verdict)
        assertTrue(a.missing.isEmpty())
    }

    @Test fun resourceBlockOverridesOtherPasses() {
        val a = FieldCandidateQualificationPolicy.evaluate(
            FieldCandidateQualificationEvidence(2, 2, 2, 2, 2, 1)
        )
        assertEquals(FieldCandidateQualificationVerdict.BLOCKED, a.verdict)
        assertTrue(a.missing.contains("RESOURCE_BLOCKED"))
    }

    @Test fun newerResourcePassClearsOlderBlock() {
        val a = FieldCandidateQualificationPolicy.evaluate(
            FieldCandidateQualificationEvidence(
                1, 1, 1, 1, 1, 1,
                latestRuntimeResourcePassMillis = 2_000L,
                latestRuntimeResourceBlockMillis = 1_000L
            )
        )
        assertEquals(FieldCandidateQualificationVerdict.PASS, a.verdict)
    }

    @Test fun newerResourceBlockOverridesOlderPass() {
        val a = FieldCandidateQualificationPolicy.evaluate(
            FieldCandidateQualificationEvidence(
                1, 1, 1, 1, 1, 1,
                latestRuntimeResourcePassMillis = 1_000L,
                latestRuntimeResourceBlockMillis = 2_000L
            )
        )
        assertEquals(FieldCandidateQualificationVerdict.BLOCKED, a.verdict)
    }

    @Test fun missingReturnKeepsCandidatePending() {
        val a = FieldCandidateQualificationPolicy.evaluate(
            FieldCandidateQualificationEvidence(1, 1, 1, 0, 1, 0)
        )
        assertEquals(FieldCandidateQualificationVerdict.PENDING, a.verdict)
        assertTrue(a.missing.contains("CELLULAR_TO_WIFI_RETURN"))
    }

    @Test fun inconclusiveResourcesDoNotPretendPass() {
        val a = FieldCandidateQualificationPolicy.evaluate(
            FieldCandidateQualificationEvidence(1, 1, 1, 1, 0, 0)
        )
        assertEquals(FieldCandidateQualificationVerdict.PENDING, a.verdict)
        assertTrue(a.missing.contains("RUNTIME_RESOURCE_PASS"))
    }
}
