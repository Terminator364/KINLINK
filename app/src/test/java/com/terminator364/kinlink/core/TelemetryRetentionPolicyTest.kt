package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryRetentionPolicyTest {
    @Test fun retentionIsBounded() {
        assertTrue(TelemetryRetentionPolicy.NETWORK_EVENT_MAX_ROWS <= 5_000)
        assertTrue(TelemetryRetentionPolicy.ACTION_RECEIPT_MAX_ROWS <= 500)
    }

    @Test fun cutoffsMatchConfiguredAge() {
        val now = 1_000_000_000L
        assertEquals(
            now - TelemetryRetentionPolicy.NETWORK_EVENT_MAX_AGE_MS,
            TelemetryRetentionPolicy.networkCutoff(now)
        )
        assertEquals(
            now - TelemetryRetentionPolicy.ACTION_RECEIPT_MAX_AGE_MS,
            TelemetryRetentionPolicy.actionCutoff(now)
        )
    }
}
