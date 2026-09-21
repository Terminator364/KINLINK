package com.terminator364.kinlink.core

object RuntimeResourceRetryPolicy {
    const val MAX_ATTEMPTS = 2

    fun shouldRetry(
        assessment: RuntimeResourceAssessment,
        completedAttempts: Int
    ): Boolean {
        if (completedAttempts >= MAX_ATTEMPTS) return false
        return when (assessment.verdict) {
            RuntimeResourceVerdict.PASS -> false
            RuntimeResourceVerdict.INCONCLUSIVE -> true
            RuntimeResourceVerdict.BLOCKED -> {
                assessment.reasons.isNotEmpty() &&
                    assessment.reasons.all {
                        it == "BATTERY_RATE_OVER_LIMIT" ||
                            it == "BATTERY_RATE_INCONCLUSIVE"
                    }
            }
        }
    }
}
