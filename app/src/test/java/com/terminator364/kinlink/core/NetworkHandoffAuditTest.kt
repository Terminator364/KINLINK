package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkHandoffAuditTest {
    @Test fun firstObservationCreatesNoTransition() {
        val audit = NetworkHandoffAudit()
        assertNull(audit.observe(Transport.WIFI))
    }

    @Test fun repeatedTransportCreatesNoNoise() {
        val audit = NetworkHandoffAudit()
        audit.observe(Transport.WIFI)
        assertNull(audit.observe(Transport.WIFI))
    }

    @Test fun wifiToCellularIsExplicitlyRecorded() {
        val audit = NetworkHandoffAudit()
        audit.observe(Transport.WIFI)
        val transition = audit.observe(Transport.CELLULAR)
        assertEquals(HandoffKind.WIFI_TO_CELLULAR, transition?.kind)
        assertEquals(Transport.WIFI, transition?.from)
        assertEquals(Transport.CELLULAR, transition?.to)
    }

    @Test fun noneToCellularIsExplicitlyRecorded() {
        val audit = NetworkHandoffAudit()
        audit.observe(Transport.NONE)
        assertEquals(HandoffKind.NONE_TO_CELLULAR, audit.observe(Transport.CELLULAR)?.kind)
    }
}
