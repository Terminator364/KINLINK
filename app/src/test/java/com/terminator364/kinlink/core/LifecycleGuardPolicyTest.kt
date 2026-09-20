package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LifecycleGuardPolicyTest {
    @Test fun normalStartsKeepRecoveryEnabled() {
        assertTrue(LifecycleGuardPolicy.decide(1).recoveryAllowed)
        assertTrue(LifecycleGuardPolicy.decide(3).recoveryAllowed)
    }

    @Test fun restartStormSuspendsActiveRecovery() {
        assertFalse(LifecycleGuardPolicy.decide(4).recoveryAllowed)
        assertFalse(LifecycleGuardPolicy.decide(10).recoveryAllowed)
    }
}
