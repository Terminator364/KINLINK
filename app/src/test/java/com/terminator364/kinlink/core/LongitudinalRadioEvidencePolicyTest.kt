package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class LongitudinalRadioEvidencePolicyTest {
    @Test fun insufficientBelowThreeRelevantTransitions() {
        val a = LongitudinalRadioEvidencePolicy.assess(
            mapOf(
                PassiveProblemCause.WEAK_WIFI_SIGNAL.name to 1,
                PassiveProblemCause.CONGESTION_SUSPECT.name to 1
            )
        )
        assertEquals(LongitudinalRadioPattern.INSUFFICIENT, a.pattern)
        assertEquals(2, a.sampleCount)
    }

    @Test fun weakSignalCanBecomeLongitudinallyDominant() {
        val a = LongitudinalRadioEvidencePolicy.assess(
            mapOf(
                PassiveProblemCause.WEAK_WIFI_SIGNAL.name to 6,
                PassiveProblemCause.CONGESTION_SUSPECT.name to 2,
                PassiveProblemCause.NETWORK_SUSPENDED.name to 1
            )
        )
        assertEquals(LongitudinalRadioPattern.RADIO_DOMINANT, a.pattern)
        assertEquals(66, a.dominancePercent)
    }

    @Test fun mixedEvidenceAvoidsFalseSingleCauseClaim() {
        val a = LongitudinalRadioEvidencePolicy.assess(
            mapOf(
                PassiveProblemCause.WEAK_WIFI_SIGNAL.name to 2,
                PassiveProblemCause.CONGESTION_SUSPECT.name to 2,
                PassiveProblemCause.NETWORK_SUSPENDED.name to 1
            )
        )
        assertEquals(LongitudinalRadioPattern.MIXED, a.pattern)
    }

    @Test fun unrelatedCausesDoNotInflateRadioEvidence() {
        val a = LongitudinalRadioEvidencePolicy.assess(
            mapOf(
                PassiveProblemCause.DNS_CONFIGURATION_SUSPECT.name to 20,
                PassiveProblemCause.WEAK_WIFI_SIGNAL.name to 1
            )
        )
        assertEquals(LongitudinalRadioPattern.INSUFFICIENT, a.pattern)
        assertEquals(1, a.sampleCount)
    }
}
