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

    @Test fun irreplaceableQualificationReceiptsArePinned() {
        assertTrue(TelemetryRetentionPolicy.qualificationReceiptPinned("SELF_TEST_CORE_V9"))
        assertTrue(TelemetryRetentionPolicy.qualificationReceiptPinned("SELF_TEST_OBSERVER_CALLBACK_V9"))
        assertTrue(TelemetryRetentionPolicy.qualificationReceiptPinned("FIELD_CANDIDATE_QUALIFIED_V9"))
    }

    @Test fun noisyOperationalReceiptsRemainPrunable() {
        assertFalse(TelemetryRetentionPolicy.qualificationReceiptPinned("PASSIVE_CAUSE_FLAPPING"))
        assertFalse(TelemetryRetentionPolicy.qualificationReceiptPinned("HANDOFF_CELLULAR_TO_WIFI_V9"))
        assertFalse(TelemetryRetentionPolicy.qualificationReceiptPinned("RUNTIME_RESOURCE_GATE_PASS_V9"))
    }

}
