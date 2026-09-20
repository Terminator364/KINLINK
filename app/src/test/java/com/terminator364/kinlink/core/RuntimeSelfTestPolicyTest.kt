package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeSelfTestPolicyTest {
    @Test fun currentSchemaAndReadableModePassCore() {
        assertTrue(RuntimeSelfTestPolicy.core(4, true).pass)
    }

    @Test fun oldSchemaFailsCore() {
        assertFalse(RuntimeSelfTestPolicy.core(3, true).pass)
    }

    @Test fun unreadableModeFailsCore() {
        assertFalse(RuntimeSelfTestPolicy.core(4, false).pass)
    }

    @Test fun callbackProofIsExplicit() {
        assertTrue(RuntimeSelfTestPolicy.observerCallback(true).pass)
        assertFalse(RuntimeSelfTestPolicy.observerCallback(false).pass)
    }
}
