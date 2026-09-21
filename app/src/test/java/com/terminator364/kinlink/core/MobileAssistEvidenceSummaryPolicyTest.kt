package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileAssistEvidenceSummaryPolicyTest {
    @Test fun emptyEvidenceIsCollectionNotSuccess() {
        val s = MobileAssistEvidenceSummaryPolicy.summarize(emptyMap())
        assertEquals(MobileAssistEvidenceTrend.INSUFFICIENT, s.trend)
        assertEquals(0, s.evaluatedActions)
        assertTrue(s.label.contains("en collecte"))
    }

    @Test fun sustainedBetterNeverBecomesCausalWording() {
        val s = MobileAssistEvidenceSummaryPolicy.summarize(
            mapOf("SUSTAINED_BETTER" to 2)
        )
        assertEquals(MobileAssistEvidenceTrend.SUSTAINED_BETTER, s.trend)
        assertTrue(s.label.contains("corrélé"))
    }

    @Test fun relapseIsVisibleInsteadOfHiddenBehindEarlierSuccess() {
        val s = MobileAssistEvidenceSummaryPolicy.summarize(
            mapOf("RELAPSED_AFTER_SUSTAINED" to 1)
        )
        assertEquals(MobileAssistEvidenceTrend.RELAPSING, s.trend)
        assertTrue(s.label.contains("rechute"))
    }

    @Test fun noBenefitIsExplicit() {
        val s = MobileAssistEvidenceSummaryPolicy.summarize(
            mapOf("NO_BETTER" to 3)
        )
        assertEquals(MobileAssistEvidenceTrend.NO_CONFIRMED_BENEFIT, s.trend)
        assertTrue(s.label.contains("aucun mieux confirmé"))
    }

    @Test fun mixedEvidenceKeepsBothBenefitAndFailureCounts() {
        val s = MobileAssistEvidenceSummaryPolicy.summarize(
            mapOf(
                "SUSTAINED_BETTER" to 2,
                "RELAPSED" to 1,
                "NO_BETTER" to 1
            )
        )
        assertEquals(MobileAssistEvidenceTrend.MIXED, s.trend)
        assertTrue(s.label.contains("mieux=2"))
        assertTrue(s.label.contains("rechute=1"))
        assertTrue(s.label.contains("sans mieux=1"))
    }
    @Test fun lateRelapseSupersedesItsEarlierSustainedEvent() {
        val s = MobileAssistEvidenceSummaryPolicy.summarize(
            mapOf(
                "SUSTAINED_BETTER" to 1,
                "RELAPSED_AFTER_SUSTAINED" to 1
            )
        )
        assertEquals(MobileAssistEvidenceTrend.RELAPSING, s.trend)
        assertEquals(1, s.evaluatedActions)
        assertTrue(s.label.contains("rechute"))
    }

    @Test fun oneHoldingImprovementAndOneLateRelapseRemainMixed() {
        val s = MobileAssistEvidenceSummaryPolicy.summarize(
            mapOf(
                "SUSTAINED_BETTER" to 2,
                "RELAPSED_AFTER_SUSTAINED" to 1
            )
        )
        assertEquals(MobileAssistEvidenceTrend.MIXED, s.trend)
        assertEquals(2, s.evaluatedActions)
        assertTrue(s.label.contains("mieux=1"))
        assertTrue(s.label.contains("rechute=1"))
    }

}
