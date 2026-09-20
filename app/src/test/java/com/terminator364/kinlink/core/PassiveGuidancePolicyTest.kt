package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PassiveGuidancePolicyTest {
    @Test fun dnsSuspicionProducesCautiousGuidance() {
        val g = PassiveGuidancePolicy.guidance(
            PassiveProblemAssessment(
                PassiveProblemCause.DNS_CONFIGURATION_SUSPECT,
                "test",
                70
            )
        )
        assertEquals("DNS à surveiller", g.title)
    }

    @Test fun trackerEmitsOnlyCauseTransitions() {
        val t = PassiveProblemTransitionTracker()
        assertNull(t.observe(PassiveProblemCause.NONE))
        assertNull(t.observe(PassiveProblemCause.NONE))
        assertEquals(
            PassiveProblemCause.LOW_CAPACITY,
            t.observe(PassiveProblemCause.LOW_CAPACITY)
        )
        assertNull(t.observe(PassiveProblemCause.LOW_CAPACITY))
    }
}
