package com.terminator364.kinlink.core

import android.net.ConnectivityDiagnosticsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformDiagnosticPolicyTest {
    @Test fun healthyValidatedReportIsNotPersistedAsNoise() {
        assertFalse(
            PlatformDiagnosticPolicy.shouldPersistConnectivityReport(
                validated = true,
                captivePortal = false,
                notSuspended = true
            )
        )
    }

    @Test fun unvalidatedOrSuspendedReportIsPersisted() {
        assertTrue(
            PlatformDiagnosticPolicy.shouldPersistConnectivityReport(
                validated = false,
                captivePortal = false,
                notSuspended = true
            )
        )
        assertTrue(
            PlatformDiagnosticPolicy.shouldPersistConnectivityReport(
                validated = true,
                captivePortal = false,
                notSuspended = false
            )
        )
    }

    @Test fun dataStallMethodIsHumanReadableWithoutPayloadData() {
        val both =
            ConnectivityDiagnosticsManager.DataStallReport.DETECTION_METHOD_DNS_EVENTS or
                ConnectivityDiagnosticsManager.DataStallReport.DETECTION_METHOD_TCP_METRICS
        assertEquals("DNS+TCP", PlatformDiagnosticPolicy.stallMethodLabel(both))
    }
}
