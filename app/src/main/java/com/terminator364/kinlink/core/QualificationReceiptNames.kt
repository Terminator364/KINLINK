package com.terminator364.kinlink.core

object QualificationReceiptNames {
    fun coreSelfTest(versionCode: Long) = "SELF_TEST_CORE_V$versionCode"
    fun observerSelfTest(versionCode: Long) = "SELF_TEST_OBSERVER_CALLBACK_V$versionCode"
    fun handoff(kind: HandoffKind, versionCode: Long) = "HANDOFF_${kind.name}_V$versionCode"
    fun handoffOutcome(outcome: HandoffOutcome, versionCode: Long) =
        "HANDOFF_OUTCOME_${outcome.name}_V$versionCode"
    fun resourceGate(verdict: RuntimeResourceVerdict, versionCode: Long) =
        "RUNTIME_RESOURCE_GATE_${verdict.name}_V$versionCode"
    fun fieldQualified(versionCode: Long) = "FIELD_CANDIDATE_QUALIFIED_V$versionCode"
    fun fieldBlocked(versionCode: Long) = "FIELD_CANDIDATE_BLOCKED_V$versionCode"
}
