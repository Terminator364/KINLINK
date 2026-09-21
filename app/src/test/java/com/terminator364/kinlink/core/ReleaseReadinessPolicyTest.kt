package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessPolicyTest {
    @Test fun devIsBlockedWithoutAllEvidence() {
        val a = ReleaseReadinessPolicy.evaluate(
            ReleaseReadinessEvidence(
                machinePass = true,
                migrationPass = true,
                signerContinuityPass = true,
                fieldHandoffPass = true,
                resourceQualificationPass = false
            )
        )
        assertEquals(ReleaseReadinessVerdict.BLOCKED, a.verdict)
        assertTrue(a.missingGates.contains(ReleaseReadinessGate.RESOURCE_QUALIFICATION))
    }

    @Test fun everyGateIsMandatory() {
        for (gate in ReleaseReadinessGate.entries) {
            val a = ReleaseReadinessPolicy.evaluate(
                ReleaseReadinessEvidence(
                    machinePass = gate != ReleaseReadinessGate.MACHINE,
                    migrationPass = gate != ReleaseReadinessGate.MIGRATION,
                    signerContinuityPass = gate != ReleaseReadinessGate.SIGNER_CONTINUITY,
                    fieldHandoffPass = gate != ReleaseReadinessGate.FIELD_HANDOFF,
                    resourceQualificationPass = gate != ReleaseReadinessGate.RESOURCE_QUALIFICATION
                )
            )
            assertEquals(ReleaseReadinessVerdict.BLOCKED, a.verdict)
            assertTrue(a.missingGates.contains(gate))
        }
    }

    @Test fun onlyCompleteEvidenceAllowsSignedCandidate() {
        val a = ReleaseReadinessPolicy.evaluate(
            ReleaseReadinessEvidence(
                machinePass = true,
                migrationPass = true,
                signerContinuityPass = true,
                fieldHandoffPass = true,
                resourceQualificationPass = true
            )
        )
        assertEquals(ReleaseReadinessVerdict.READY_FOR_SIGNED_CANDIDATE, a.verdict)
        assertTrue(a.missingGates.isEmpty())
    }
}
