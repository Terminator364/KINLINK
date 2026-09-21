package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoundedProbeExecutorPolicyTest {
    @Test fun probeWorkerConcurrencyIsStrictlyBounded() {
        assertEquals(2, BoundedProbeExecutorPolicy.MAX_CONCURRENT_ATTEMPTS)
        assertTrue(BoundedProbeExecutorPolicy.MAX_CONCURRENT_ATTEMPTS <= 2)
    }

    @Test fun idleProbeWorkersExpireQuickly() {
        assertTrue(BoundedProbeExecutorPolicy.KEEP_ALIVE_SECONDS <= 30L)
    }
}
