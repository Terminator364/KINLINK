package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileRecommendationPolicyTest {
    @Test fun quietHistoryRecommendsConservative() {
        assertEquals(
            AutopilotProfile.CONSERVATIVE,
            ProfileRecommendationPolicy.recommend(RecentReliabilityBurden.QUIET).profile
        )
    }

    @Test fun severeHistoryRecommendsMaximumStability() {
        assertEquals(
            AutopilotProfile.MAXIMUM_STABILITY,
            ProfileRecommendationPolicy.recommend(RecentReliabilityBurden.SEVERE).profile
        )
    }
}
