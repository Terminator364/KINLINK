package com.terminator364.kinlink.core

enum class ReleaseReadinessGate {
    MACHINE,
    MIGRATION,
    SIGNER_CONTINUITY,
    FIELD_HANDOFF,
    RESOURCE_QUALIFICATION
}

enum class ReleaseReadinessVerdict {
    BLOCKED,
    READY_FOR_SIGNED_FIELD_CANDIDATE,
    READY_FOR_CANONICAL_PROMOTION
}

data class ReleaseReadinessEvidence(
    val machinePass: Boolean = false,
    val migrationPass: Boolean = false,
    val signerContinuityPass: Boolean = false,
    val fieldHandoffPass: Boolean = false,
    val resourceQualificationPass: Boolean = false
)

data class ReleaseReadinessAssessment(
    val verdict: ReleaseReadinessVerdict,
    val missingGates: Set<ReleaseReadinessGate>
)

object ReleaseReadinessPolicy {
    fun evaluate(evidence: ReleaseReadinessEvidence): ReleaseReadinessAssessment {
        val missing = linkedSetOf<ReleaseReadinessGate>()
        if (!evidence.machinePass) missing += ReleaseReadinessGate.MACHINE
        if (!evidence.migrationPass) missing += ReleaseReadinessGate.MIGRATION
        if (!evidence.signerContinuityPass) missing += ReleaseReadinessGate.SIGNER_CONTINUITY
        if (!evidence.fieldHandoffPass) missing += ReleaseReadinessGate.FIELD_HANDOFF
        if (!evidence.resourceQualificationPass) missing += ReleaseReadinessGate.RESOURCE_QUALIFICATION

        val preCandidateMissing = missing.intersect(
            setOf(
                ReleaseReadinessGate.MACHINE,
                ReleaseReadinessGate.MIGRATION,
                ReleaseReadinessGate.SIGNER_CONTINUITY
            )
        )

        val verdict = when {
            preCandidateMissing.isNotEmpty() -> ReleaseReadinessVerdict.BLOCKED
            missing.isEmpty() -> ReleaseReadinessVerdict.READY_FOR_CANONICAL_PROMOTION
            else -> ReleaseReadinessVerdict.READY_FOR_SIGNED_FIELD_CANDIDATE
        }

        return ReleaseReadinessAssessment(verdict, missing)
    }
}
