package com.terminator364.kinlink.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileAssistEvidenceSamplingPolicyTest {
    @Test fun evidenceSamplingIsStrictlyBounded() {
        assertEquals(
            3,
            MobileAssistEvidenceSamplingPolicy.MAX_SAMPLES_PER_ACTION
        )
        assertArrayEquals(
            longArrayOf(21_000L, 42_000L, 65_000L),
            MobileAssistEvidenceSamplingPolicy.sampleDelaysMs
        )
        assertTrue(MobileAssistEvidenceSamplingPolicy.valid())
    }
}
