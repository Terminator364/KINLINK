package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MobileAssistEvidenceTrackerTest {
    private fun mobile(
        at: Long,
        down: Int,
        up: Int,
        internet: InternetState = InternetState.VALIDATED
    ) = NetworkTruth(
        transport = Transport.CELLULAR,
        internetState = internet,
        downstreamKbps = down,
        upstreamKbps = up,
        observedAtMillis = at
    )

    @Test fun unknownBaselineOnlyProvesMetricsBecameAvailable() {
        val t = MobileAssistEvidenceTracker()
        t.start(mobile(1_000L, 0, 0))
        val e = t.observe(mobile(5_000L, 10_000, 2_000))
        assertEquals(MobileAssistEvidenceResult.METRICS_AVAILABLE, e?.result)
    }

    @Test fun improvementMustRemainBetterForTwentySeconds() {
        val t = MobileAssistEvidenceTracker()
        t.start(mobile(1_000L, 600, 200))
        assertNull(t.observe(mobile(10_000L, 6_000, 1_500)))
        assertNull(t.observe(mobile(25_000L, 6_000, 1_500)))
        val e = t.observe(mobile(31_000L, 8_000, 2_000))
        assertEquals(MobileAssistEvidenceResult.SUSTAINED_BETTER, e?.result)
    }

    @Test fun transientBetterThenBaselineIsRelapse() {
        val t = MobileAssistEvidenceTracker()
        t.start(mobile(1_000L, 600, 200))
        assertNull(t.observe(mobile(10_000L, 7_000, 1_500)))
        val e = t.observe(mobile(20_000L, 700, 220))
        assertEquals(MobileAssistEvidenceResult.RELAPSED, e?.result)
    }

    @Test fun noBenefitAfterMinuteIsExplicit() {
        val t = MobileAssistEvidenceTracker()
        t.start(mobile(1_000L, 600, 200))
        val e = t.observe(mobile(61_001L, 700, 220))
        assertEquals(MobileAssistEvidenceResult.NO_BETTER, e?.result)
    }

    @Test fun sustainedBetterThenLaterDegradationIsVisibleAndCanBeReevaluated() {
        val t = MobileAssistEvidenceTracker()
        t.start(mobile(1_000L, 600, 200))
        assertNull(t.observe(mobile(10_000L, 6_000, 1_500)))
        val sustained = t.observe(mobile(31_000L, 8_000, 2_000))
        assertEquals(MobileAssistEvidenceResult.SUSTAINED_BETTER, sustained?.result)

        val relapse = t.observe(mobile(91_000L, 650, 210))
        assertEquals(
            MobileAssistEvidenceResult.RELAPSED_AFTER_SUSTAINED,
            relapse?.result
        )
    }

    @Test fun transportOrValidationChangeIsInconclusive() {
        val t = MobileAssistEvidenceTracker()
        t.start(mobile(1_000L, 600, 200))
        val e = t.observe(
            mobile(2_000L, 0, 0, InternetState.OFFLINE).copy(
                transport = Transport.WIFI
            )
        )
        assertEquals(MobileAssistEvidenceResult.INCONCLUSIVE, e?.result)
    }
    @Test fun explicitManualBaselineCannotBeReplacedByPostActionState() {
        val t = MobileAssistEvidenceTracker()
        t.startBaseline(
            quality = PassiveLinkQuality.CONSTRAINED,
            score = 30,
            observedAtMillis = 1_000L
        )
        assertNull(t.observe(mobile(10_000L, 6_000, 1_500)))
        val e = t.observe(mobile(31_000L, 8_000, 2_000))
        assertEquals(MobileAssistEvidenceResult.SUSTAINED_BETTER, e?.result)
        assertEquals(30, e?.baselineScore)
    }

}
