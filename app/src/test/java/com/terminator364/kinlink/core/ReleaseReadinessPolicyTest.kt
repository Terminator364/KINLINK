package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessPolicyTest {
    @Test fun missingAnyPreCandidateGateBlocksCandidate() {
        val preCandidateGates = listOf(
            ReleaseReadinessGate.MACHINE,
            ReleaseReadinessGate.MIGRATION,
            ReleaseReadinessGate.SIGNER_CONTINUITY
        )
        for (gate in preCandidateGates) {
            val a = ReleaseReadinessPolicy.evaluate(
                ReleaseReadinessEvidence(
                    machinePass = gate != ReleaseReadinessGate.MACHINE,
                    migrationPass = gate != ReleaseReadinessGate.MIGRATION,
                    signerContinuityPass = gate != ReleaseReadinessGate.SIGNER_CONTINUITY,
                    fieldHandoffPass = false,
                    resourceQualificationPass = false
                )
            )
            assertEquals(ReleaseReadinessVerdict.BLOCKED, a.verdict)
            assertTrue(a.missingGates.contains(gate))
        }
    }

    @Test fun machineMigrationAndSignerAllowOneSignedFieldCandidate() {
        val a = ReleaseReadinessPolicy.evaluate(
            ReleaseReadinessEvidence(
                machinePass = true,
                migrationPass = true,
                signerContinuityPass = true,
                fieldHandoffPass = false,
                resourceQualificationPass = false
            )
        )
        assertEquals(ReleaseReadinessVerdict.READY_FOR_SIGNED_FIELD_CANDIDATE, a.verdict)
        assertTrue(a.missingGates.contains(ReleaseReadinessGate.FIELD_HANDOFF))
        assertTrue(a.missingGates.contains(ReleaseReadinessGate.RESOURCE_QUALIFICATION))
    }

    @Test fun fieldEvidenceIsRequiredForCanonicalPromotion() {
        val a = ReleaseReadinessPolicy.evaluate(
            ReleaseReadinessEvidence(
                machinePass = true,
                migrationPass = true,
                signerContinuityPass = true,
                fieldHandoffPass = true,
                resourceQualificationPass = false
            )
        )
        assertEquals(ReleaseReadinessVerdict.READY_FOR_SIGNED_FIELD_CANDIDATE, a.verdict)
        assertTrue(a.missingGates.contains(ReleaseReadinessGate.RESOURCE_QUALIFICATION))
    }

    @Test fun completeEvidenceAllowsCanonicalPromotion() {
        val a = ReleaseReadinessPolicy.evaluate(
            ReleaseReadinessEvidence(
                machinePass = true,
                migrationPass = true,
                signerContinuityPass = true,
                fieldHandoffPass = true,
                resourceQualificationPass = true
            )
        )
        assertEquals(ReleaseReadinessVerdict.READY_FOR_CANONICAL_PROMOTION, a.verdict)
        assertTrue(a.missingGates.isEmpty())
    }
}
