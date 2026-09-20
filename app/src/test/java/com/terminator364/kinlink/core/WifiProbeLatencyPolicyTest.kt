package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class WifiProbeLatencyPolicyTest {
    @Test fun classifiesBoundaries() {
        assertEquals(ProbeResponsiveness.UNKNOWN, WifiProbeLatencyPolicy.classify(null))
        assertEquals(ProbeResponsiveness.RESPONSIVE, WifiProbeLatencyPolicy.classify(250))
        assertEquals(ProbeResponsiveness.SLOW, WifiProbeLatencyPolicy.classify(251))
        assertEquals(ProbeResponsiveness.SLOW, WifiProbeLatencyPolicy.classify(800))
        assertEquals(ProbeResponsiveness.VERY_SLOW, WifiProbeLatencyPolicy.classify(801))
    }
}
