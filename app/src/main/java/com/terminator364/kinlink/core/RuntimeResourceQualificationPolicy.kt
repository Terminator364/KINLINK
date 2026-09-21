package com.terminator364.kinlink.core

enum class RuntimeResourceVerdict {
    INCONCLUSIVE,
    PASS,
    BLOCKED
}

data class RuntimeResourceEvidence(
    val durationMillis: Long,
    val pssDeltaMiB: Int,
    val batteryPercentPerHour: Double?,
    val backgroundChurnEvents: Int,
    val batteryDeltaPercent: Int? = null
)

data class RuntimeResourceLimits(
    val minimumQualifiedDurationMillis: Long = 30L * 60L * 1000L,
    val maxPositivePssDeltaMiB: Int = 24,
    val maxBatteryPercentPerHour: Double = 1.5,
    val batteryQuantizationTolerancePercent: Int = 1,
    val maxBackgroundChurnEventsPer30Min: Int = 120
)

data class RuntimeResourceAssessment(
    val verdict: RuntimeResourceVerdict,
    val reasons: Set<String>
)

object RuntimeResourceQualificationPolicy {
    fun evaluate(
        evidence: RuntimeResourceEvidence,
        limits: RuntimeResourceLimits = RuntimeResourceLimits()
    ): RuntimeResourceAssessment {
        val reasons = linkedSetOf<String>()

        if (evidence.durationMillis < limits.minimumQualifiedDurationMillis) {
            reasons += "SESSION_TOO_SHORT"
        }
        if (evidence.pssDeltaMiB > limits.maxPositivePssDeltaMiB) {
            reasons += "PSS_GROWTH_OVER_LIMIT"
        }
        val battery = evidence.batteryPercentPerHour
        val batteryDelta = evidence.batteryDeltaPercent
        val batteryBeyondQuantization =
            batteryDelta == null || batteryDelta > limits.batteryQuantizationTolerancePercent
        if (
            battery != null &&
            battery > limits.maxBatteryPercentPerHour &&
            batteryBeyondQuantization
        ) {
            reasons += "BATTERY_RATE_OVER_LIMIT"
        }
        val churnWindowMillis = 30L * 60L * 1000L
        val allowedChurnEvents = kotlin.math.ceil(
            limits.maxBackgroundChurnEventsPer30Min.toDouble() *
                evidence.durationMillis.coerceAtLeast(1L).toDouble() /
                churnWindowMillis.toDouble()
        ).toInt().coerceAtLeast(1)
        if (evidence.backgroundChurnEvents > allowedChurnEvents) {
            reasons += "BACKGROUND_CHURN_OVER_LIMIT"
        }

        val hardFailure = reasons.any { it != "SESSION_TOO_SHORT" }
        val verdict = when {
            hardFailure -> RuntimeResourceVerdict.BLOCKED
            evidence.durationMillis < limits.minimumQualifiedDurationMillis -> RuntimeResourceVerdict.INCONCLUSIVE
            battery == null -> RuntimeResourceVerdict.INCONCLUSIVE
            else -> RuntimeResourceVerdict.PASS
        }
        if (battery == null) reasons += "BATTERY_RATE_INCONCLUSIVE"

        return RuntimeResourceAssessment(verdict, reasons)
    }
}
