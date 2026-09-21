package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecoveryEffectivenessTrackerTest {
    private fun wifi(down: Int, up: Int) = NetworkTruth(
        transport = Transport.WIFI,
        internetState = InternetState.VALIDATED,
        downstreamKbps = down,
        upstreamKbps = up
    )

    @Test fun waitsForTwoPostActionObservations() {
        val tracker = RecoveryEffectivenessTracker()
        tracker.start(wifi(700, 180))
        assertNull(tracker.observe(wifi(2_000, 700)))
        assertEquals(
            RecoveryEffectiveness.IMPROVED,
            tracker.observe(wifi(20_000, 5_000))?.result
        )
    }

    @Test fun unchangedQualityIsRecorded() {
        val tracker = RecoveryEffectivenessTracker()
        tracker.start(wifi(2_000, 700))
        tracker.observe(wifi(2_200, 750))
        assertEquals(
            RecoveryEffectiveness.UNCHANGED,
            tracker.observe(wifi(2_400, 800))?.result
        )
    }

    @Test fun transportChangeMakesOutcomeInconclusive() {
        val tracker = RecoveryEffectivenessTracker()
        tracker.start(wifi(700, 180))
        assertEquals(
            RecoveryEffectiveness.INCONCLUSIVE,
            tracker.observe(
                NetworkTruth(
                    transport = Transport.CELLULAR,
                    internetState = InternetState.VALIDATED
                )
            )?.result
        )
    }
    @Test fun cellularTrackerMeasuresTwoValidatedMobileObservations() {
        fun mobile(down: Int, up: Int) = NetworkTruth(
            transport = Transport.CELLULAR,
            internetState = InternetState.VALIDATED,
            downstreamKbps = down,
            upstreamKbps = up
        )
        val tracker = RecoveryEffectivenessTracker(Transport.CELLULAR)
        tracker.start(mobile(700, 180))
        assertNull(tracker.observe(mobile(2_000, 700)))
        assertEquals(
            RecoveryEffectiveness.IMPROVED,
            tracker.observe(mobile(20_000, 5_000))?.result
        )
    }

    @Test fun cellularTrackerRejectsWifiHandoffAsInconclusive() {
        val tracker = RecoveryEffectivenessTracker(Transport.CELLULAR)
        tracker.start(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 700,
                upstreamKbps = 180
            )
        )
        assertEquals(
            RecoveryEffectiveness.INCONCLUSIVE,
            tracker.observe(wifi(20_000, 5_000))?.result
        )
    }

}
