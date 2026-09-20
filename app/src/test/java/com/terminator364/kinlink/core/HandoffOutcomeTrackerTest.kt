package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HandoffOutcomeTrackerTest {
    @Test fun ignoresNormalCellularWithoutPriorWifiExit() {
        val tracker = HandoffOutcomeTracker()
        assertNull(
            tracker.observe(
                NetworkTruth(
                    transport = Transport.CELLULAR,
                    internetState = InternetState.VALIDATED
                )
            )
        )
    }

    @Test fun recordsValidatedMobileAfterWifiExit() {
        val tracker = HandoffOutcomeTracker()
        tracker.onTransition(
            HandoffTransition(
                Transport.WIFI,
                Transport.CELLULAR,
                HandoffKind.WIFI_TO_CELLULAR,
                "test"
            )
        )
        val result = tracker.observe(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED
            )
        )
        assertEquals(HandoffOutcome.MOBILE_VALIDATED, result?.outcome)
    }

    @Test fun unvalidatedMobileRemainsPending() {
        val tracker = HandoffOutcomeTracker()
        tracker.onTransition(
            HandoffTransition(
                Transport.WIFI,
                Transport.CELLULAR,
                HandoffKind.WIFI_TO_CELLULAR,
                "test"
            )
        )
        val result = tracker.observe(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.UNKNOWN
            )
        )
        assertEquals(HandoffOutcome.MOBILE_PRESENT_UNVALIDATED, result?.outcome)
    }
}
