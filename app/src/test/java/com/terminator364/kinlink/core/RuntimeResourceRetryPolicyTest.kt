package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeResourceRetryPolicyTest {
    @Test fun passNeverRetries() {
        assertFalse(
            RuntimeResourceRetryPolicy.shouldRetry(
                RuntimeResourceAssessment(RuntimeResourceVerdict.PASS, emptySet()),
                completedAttempts = 1
            )
        )
    }

    @Test fun firstInconclusiveWindowRetries() {
        assertTrue(
            RuntimeResourceRetryPolicy.shouldRetry(
                RuntimeResourceAssessment(
                    RuntimeResourceVerdict.INCONCLUSIVE,
                    setOf("BATTERY_RATE_INCONCLUSIVE")
                ),
                completedAttempts = 1
            )
        )
    }

    @Test fun firstBatteryOnlyBlockRetries() {
        assertTrue(
            RuntimeResourceRetryPolicy.shouldRetry(
                RuntimeResourceAssessment(
                    RuntimeResourceVerdict.BLOCKED,
                    setOf("BATTERY_RATE_OVER_LIMIT")
                ),
                completedAttempts = 1
            )
        )
    }

    @Test fun hardMemoryOrChurnBlockDoesNotRetry() {
        assertFalse(
            RuntimeResourceRetryPolicy.shouldRetry(
                RuntimeResourceAssessment(
                    RuntimeResourceVerdict.BLOCKED,
                    setOf("PSS_GROWTH_OVER_LIMIT")
                ),
                completedAttempts = 1
            )
        )
        assertFalse(
            RuntimeResourceRetryPolicy.shouldRetry(
                RuntimeResourceAssessment(
                    RuntimeResourceVerdict.BLOCKED,
                    setOf("BACKGROUND_CHURN_OVER_LIMIT")
                ),
                completedAttempts = 1
            )
        )
    }

    @Test fun secondCompletedAttemptIsTerminal() {
        assertFalse(
            RuntimeResourceRetryPolicy.shouldRetry(
                RuntimeResourceAssessment(
                    RuntimeResourceVerdict.INCONCLUSIVE,
                    setOf("BATTERY_RATE_INCONCLUSIVE")
                ),
                completedAttempts = RuntimeResourceRetryPolicy.MAX_ATTEMPTS
            )
        )
    }
}
