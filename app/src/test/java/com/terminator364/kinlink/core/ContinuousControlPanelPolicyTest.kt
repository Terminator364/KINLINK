package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinuousControlPanelPolicyTest {
    @Test fun sustainedImprovementShowsBeforeNowAndPositiveDelta() {
        val panel = ContinuousControlPanelPolicy.build(
            currentScore = 82,
            observationOnly = false,
            resourceConstrained = false,
            latestEvidenceAction = "MOBILE_ASSIST_EVIDENCE_SUSTAINED_BETTER",
            latestEvidenceSummary = "baseline=LIMITED/55; current=COMFORTABLE/80; elapsedMs=31000; transport=CELLULAR",
            latestEvidenceAgeMillis = 5_000L
        )
        assertEquals(ContinuousControlState.MAINTAINING, panel.state)
        assertEquals(55, panel.beforeScore)
        assertEquals(27, panel.delta)
        assertTrue(panel.maintenance.contains("au-dessus"))
    }

    @Test fun laterReturnNearBaselineShowsRelapseEvenBeforeAnotherReceipt() {
        val panel = ContinuousControlPanelPolicy.build(
            currentScore = 58,
            observationOnly = false,
            resourceConstrained = false,
            latestEvidenceAction = "MOBILE_ASSIST_EVIDENCE_SUSTAINED_BETTER",
            latestEvidenceSummary = "baseline=LIMITED/55; current=COMFORTABLE/80; elapsedMs=31000; transport=CELLULAR",
            latestEvidenceAgeMillis = 60_000L
        )
        assertEquals(ContinuousControlState.RELAPSED, panel.state)
    }

    @Test fun noBenefitNeverLooksLikeSuccess() {
        val panel = ContinuousControlPanelPolicy.build(
            currentScore = 52,
            observationOnly = false,
            resourceConstrained = false,
            latestEvidenceAction = "MOBILE_ASSIST_EVIDENCE_NO_BETTER",
            latestEvidenceSummary = "baseline=LIMITED/52; current=LIMITED/52; elapsedMs=61000; transport=CELLULAR",
            latestEvidenceAgeMillis = 1_000L
        )
        assertEquals(ContinuousControlState.NO_CONFIRMED_BENEFIT, panel.state)
        assertEquals(0, panel.delta)
    }

    @Test fun resourceProtectionDominatesEvidence() {
        val panel = ContinuousControlPanelPolicy.build(
            currentScore = 90,
            observationOnly = false,
            resourceConstrained = true,
            latestEvidenceAction = "MOBILE_ASSIST_EVIDENCE_SUSTAINED_BETTER",
            latestEvidenceSummary = "baseline=LIMITED/60; current=COMFORTABLE/90; elapsedMs=31000",
            latestEvidenceAgeMillis = 1_000L
        )
        assertEquals(ContinuousControlState.RESOURCE_PROTECTED, panel.state)
    }

    @Test fun safeModeDominatesEverything() {
        val panel = ContinuousControlPanelPolicy.build(
            currentScore = 90,
            observationOnly = true,
            resourceConstrained = false,
            latestEvidenceAction = null,
            latestEvidenceSummary = null,
            latestEvidenceAgeMillis = null
        )
        assertEquals(ContinuousControlState.SAFE_MODE, panel.state)
    }
}
