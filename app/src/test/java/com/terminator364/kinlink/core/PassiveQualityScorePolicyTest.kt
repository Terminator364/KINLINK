package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PassiveQualityScorePolicyTest {
    @Test fun offlineIsAlwaysZero() {
        assertEquals(
            0,
            PassiveQualityScorePolicy.score(
                NetworkTruth(
                    transport = Transport.NONE,
                    internetState = InternetState.OFFLINE
                )
            ).score
        )
    }

    @Test fun validatedComfortableMobileScoresAboveConstrainedMobile() {
        val weak = NetworkTruth(
            transport = Transport.CELLULAR,
            internetState = InternetState.VALIDATED,
            downstreamKbps = 600,
            upstreamKbps = 180,
            androidNotSuspended = true,
            androidNotCongested = false
        )
        val strong = weak.copy(
            downstreamKbps = 15_000,
            upstreamKbps = 3_000,
            androidNotCongested = true
        )
        assertTrue(
            PassiveQualityScorePolicy.score(strong).score >
                PassiveQualityScorePolicy.score(weak).score
        )
    }

    @Test fun scoreNeverOverridesValidationSemantics() {
        val partialFast = NetworkTruth(
            transport = Transport.CELLULAR,
            internetState = InternetState.PARTIAL,
            downstreamKbps = 100_000,
            upstreamKbps = 50_000,
            androidNotSuspended = true,
            androidNotCongested = true
        )
        val validatedSlow = NetworkTruth(
            transport = Transport.CELLULAR,
            internetState = InternetState.VALIDATED,
            downstreamKbps = 1_200,
            upstreamKbps = 500,
            androidNotSuspended = true,
            androidNotCongested = true
        )
        assertTrue(
            PassiveQualityScorePolicy.score(validatedSlow).score >
                PassiveQualityScorePolicy.score(partialFast).score
        )
    }

    @Test fun unknownBandwidthStillProducesBoundedPassiveScore() {
        val score = PassiveQualityScorePolicy.score(
            NetworkTruth(
                transport = Transport.CELLULAR,
                internetState = InternetState.VALIDATED,
                downstreamKbps = 0,
                upstreamKbps = 0
            )
        ).score
        assertTrue(score in 0..100)
    }
    @Test fun stalledStateCannotOutscoreValidatedState() {
        val base = NetworkTruth(
            transport = Transport.CELLULAR,
            downstreamKbps = 20_000,
            upstreamKbps = 5_000,
            androidNotSuspended = true,
            androidNotCongested = true
        )
        val stalled = PassiveQualityScorePolicy.score(
            base.copy(internetState = InternetState.STALLED)
        ).score
        val validated = PassiveQualityScorePolicy.score(
            base.copy(internetState = InternetState.VALIDATED)
        ).score
        assertTrue(stalled < validated)
    }

}
