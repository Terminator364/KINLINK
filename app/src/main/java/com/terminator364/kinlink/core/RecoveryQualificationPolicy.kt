package com.terminator364.kinlink.core

enum class RecoveryGate {
    ENGINE_PROTOTYPE,
    NAME_RESOLUTION_RESILIENCE,
    WATCHDOG_TEARDOWN,
    RESOURCE_BUDGET,
    LATENCY_BUDGET,
    ROLLBACK
}

enum class RecoveryQualificationVerdict {
    BLOCKED,
    QUALIFYING,
    ELIGIBLE_FOR_FIELD_TRIAL
}

data class RecoveryQualificationLimits(
    val maxRamDeltaMiB: Int = 24,
    val maxBatteryPercentPerHour: Double = 1.5,
    val maxAddedLatencyMillis: Int = 10
)

data class RecoveryQualificationEvidence(
    val enginePrototypePass: Boolean = false,
    val nameResolutionPass: Boolean = false,
    val watchdogTeardownPass: Boolean = false,
    val rollbackPass: Boolean = false,
    val ramDeltaMiB: Int? = null,
    val batteryPercentPerHour: Double? = null,
    val addedLatencyMillis: Int? = null
)

data class RecoveryQualificationResult(
    val verdict: RecoveryQualificationVerdict,
    val failedOrMissingGates: Set<RecoveryGate>
)

object RecoveryQualificationPolicy {
    fun evaluate(
        evidence: RecoveryQualificationEvidence,
        limits: RecoveryQualificationLimits = RecoveryQualificationLimits()
    ): RecoveryQualificationResult {
        val missing = linkedSetOf<RecoveryGate>()

        if (!evidence.enginePrototypePass) missing += RecoveryGate.ENGINE_PROTOTYPE
        if (!evidence.nameResolutionPass) missing += RecoveryGate.NAME_RESOLUTION_RESILIENCE
        if (!evidence.watchdogTeardownPass) missing += RecoveryGate.WATCHDOG_TEARDOWN
        if (!evidence.rollbackPass) missing += RecoveryGate.ROLLBACK

        val ram = evidence.ramDeltaMiB
        val battery = evidence.batteryPercentPerHour
        if (ram == null || battery == null ||
            ram > limits.maxRamDeltaMiB ||
            battery > limits.maxBatteryPercentPerHour
        ) {
            missing += RecoveryGate.RESOURCE_BUDGET
        }

        val latency = evidence.addedLatencyMillis
        if (latency == null || latency > limits.maxAddedLatencyMillis) {
            missing += RecoveryGate.LATENCY_BUDGET
        }

        val verdict = when {
            missing.isEmpty() -> RecoveryQualificationVerdict.ELIGIBLE_FOR_FIELD_TRIAL
            evidence.enginePrototypePass ||
                evidence.nameResolutionPass ||
                evidence.watchdogTeardownPass -> RecoveryQualificationVerdict.QUALIFYING
            else -> RecoveryQualificationVerdict.BLOCKED
        }

        return RecoveryQualificationResult(verdict, missing)
    }
}

/** Strong recovery remains disabled until separate field qualification is complete. */
object RecoveryFeatureGate {
    fun productionEnabled(result: RecoveryQualificationResult): Boolean = false
}
