package com.terminator364.kinlink.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionReceiptIncidentWindowTest {
    @Test fun incidentWindowModelKeepsOrderedEvidenceShape() {
        val actions = listOf(
            ActionReceipt(100L, "USER_INCIDENT_MARKER", true, "marker"),
            ActionReceipt(200L, "PASSIVE_CAUSE_LOW_CAPACITY", true, "cause")
        )
        val summary = DiagnosticSummary(
            generatedAtMillis = 300L,
            totalEvents = 0,
            currentTruth = com.terminator364.kinlink.core.NetworkTruth(),
            stateCounts = emptyMap(),
            latestUserIncidentMarkerMillis = 100L,
            incidentWindowActions = actions
        )
        assertEquals(2, summary.incidentWindowActions.size)
        assertEquals("USER_INCIDENT_MARKER", summary.incidentWindowActions.first().action)
    }
}
