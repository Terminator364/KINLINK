package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryCircuitBreakerPolicyTest {
    @Test fun opensOnlyAfterRepeatedSafetyAborts() {
        assertFalse(RecoveryCircuitBreakerPolicy.open(0))
        assertFalse(RecoveryCircuitBreakerPolicy.open(1))
        assertTrue(RecoveryCircuitBreakerPolicy.open(2))
        assertTrue(RecoveryCircuitBreakerPolicy.open(3))
    }
}
