package com.terminator364.kinlink.core

enum class FieldCandidateQualificationVerdict {
    PENDING,
    BLOCKED,
    PASS
}

data class FieldCandidateQualificationEvidence(
    val coreSelfTestPasses: Int,
    val observerSelfTestPasses: Int,
    val mobileValidatedHandoffs: Int,
    val cellularToWifiReturns: Int,
    val runtimeResourcePasses: Int,
    val runtimeResourceBlocks: Int,
    val latestRuntimeResourcePassMillis: Long? = null,
    val latestRuntimeResourceBlockMillis: Long? = null
)

data class FieldCandidateQualificationAssessment(
    val verdict: FieldCandidateQualificationVerdict,
    val missing: Set<String>
)

object FieldCandidateQualificationPolicy {
    fun evaluate(
        evidence: FieldCandidateQualificationEvidence
    ): FieldCandidateQualificationAssessment {
        val hasTimestampedResourceEvidence =
            evidence.latestRuntimeResourcePassMillis != null ||
                evidence.latestRuntimeResourceBlockMillis != null
        val latestResourceIsBlocked = if (hasTimestampedResourceEvidence) {
            val blockedAt = evidence.latestRuntimeResourceBlockMillis
            val passAt = evidence.latestRuntimeResourcePassMillis
            blockedAt != null && (passAt == null || blockedAt >= passAt)
        } else {
            evidence.runtimeResourceBlocks > 0
        }
        val latestResourceIsPass = if (hasTimestampedResourceEvidence) {
            val passAt = evidence.latestRuntimeResourcePassMillis
            val blockedAt = evidence.latestRuntimeResourceBlockMillis
            passAt != null && (blockedAt == null || passAt > blockedAt)
        } else {
            evidence.runtimeResourcePasses > 0 && evidence.runtimeResourceBlocks == 0
        }

        if (latestResourceIsBlocked) {
            return FieldCandidateQualificationAssessment(
                FieldCandidateQualificationVerdict.BLOCKED,
                setOf("RESOURCE_BLOCKED")
            )
        }

        val missing = linkedSetOf<String>()
        if (evidence.coreSelfTestPasses < 1) missing += "CORE_SELF_TEST"
        if (evidence.observerSelfTestPasses < 1) missing += "OBSERVER_SELF_TEST"
        if (evidence.mobileValidatedHandoffs < 1) missing += "WIFI_TO_VALIDATED_CELLULAR"
        if (evidence.cellularToWifiReturns < 1) missing += "CELLULAR_TO_WIFI_RETURN"
        if (!latestResourceIsPass) missing += "RUNTIME_RESOURCE_PASS"

        return FieldCandidateQualificationAssessment(
            if (missing.isEmpty()) FieldCandidateQualificationVerdict.PASS
            else FieldCandidateQualificationVerdict.PENDING,
            missing
        )
    }
}
