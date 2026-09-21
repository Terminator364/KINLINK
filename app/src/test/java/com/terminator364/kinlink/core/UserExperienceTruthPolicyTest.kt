package com.terminator364.kinlink.core

import com.terminator364.kinlink.data.RecentReliabilityWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserExperienceTruthPolicyTest {
    private fun mobile() = NetworkTruth(
        transport = Transport.CELLULAR,
        internetState = InternetState.VALIDATED,
        downstreamKbps = 20_000,
        upstreamKbps = 5_000,
        androidNotCongested = true,
        androidNotSuspended = true,
        metered = true
    )

    @Test fun highAndroidEstimatesDoNotBecomePerfectUserQuality() {
        val result = UserExperienceTruthPolicy.assess(mobile(), null, false)
        assertEquals(UserExperienceState.ACCESS_AVAILABLE_QUALITY_UNVERIFIED, result.state)
        assertFalse(result.statusLabel.contains("100"))
        assertFalse(result.statusLabel.contains("confort", ignoreCase = true))
    }

    @Test fun unstableHistoryOverridesComfortableFrameworkEstimate() {
        val reliability = RecentReliabilityWindow(
            interruptionCount = 1,
            cumulativeMillis = 5_000,
            longestMillis = 5_000,
            dominantCause = null,
            mobileLowQualityEpisodeCount = 30,
            mobileLowQualityCumulativeMillis = 525_106
        )
        val result = UserExperienceTruthPolicy.assess(mobile(), reliability, false)
        assertEquals(UserExperienceState.UNSTABLE_HISTORY, result.state)
        assertTrue(result.degraded)
    }

    @Test fun explicitUserProblemReportOverridesFrameworkEstimate() {
        val result = UserExperienceTruthPolicy.assess(mobile(), null, true)
        assertEquals(UserExperienceState.USER_REPORTED_BAD, result.state)
        assertTrue(result.degraded)
    }
}
