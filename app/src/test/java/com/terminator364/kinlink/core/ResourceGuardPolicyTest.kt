package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceGuardPolicyTest {
    @Test fun normalDeviceAllowsRecovery() {
        assertFalse(ResourceGuardPolicy.constrained(false, false, false))
    }

    @Test fun batterySaverConstrainsRecovery() {
        assertTrue(ResourceGuardPolicy.constrained(true, false, false))
    }

    @Test fun severeThermalStateConstrainsRecovery() {
        assertTrue(ResourceGuardPolicy.constrained(false, true, false))
    }

    @Test fun lowMemoryConstrainsRecovery() {
        assertTrue(ResourceGuardPolicy.constrained(false, false, true))
    }
}
