package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PassiveQualityPersistencePolicyTest {
    private val limitedWifi = NetworkTruth(
        transport = Transport.WIFI,
        internetState = InternetState.VALIDATED,
        downstreamKbps = 2_000,
        upstreamKbps = 700
    )

    @Test fun requiresThreeConsecutiveLowQualityObservations() {
        var streak = 0
        streak = PassiveQualityPersistencePolicy.nextStreak(streak, limitedWifi)
        assertFalse(PassiveQualityPersistencePolicy.persistentLowQuality(streak))
        streak = PassiveQualityPersistencePolicy.nextStreak(streak, limitedWifi)
        assertFalse(PassiveQualityPersistencePolicy.persistentLowQuality(streak))
        streak = PassiveQualityPersistencePolicy.nextStreak(streak, limitedWifi)
        assertTrue(PassiveQualityPersistencePolicy.persistentLowQuality(streak))
    }

    @Test fun goodWifiResetsStreak() {
        val good = limitedWifi.copy(downstreamKbps = 20_000, upstreamKbps = 5_000)
        assertEquals(0, PassiveQualityPersistencePolicy.nextStreak(2, good))
    }

    @Test fun cellularResetsStreak() {
        assertEquals(
            0,
            PassiveQualityPersistencePolicy.nextStreak(
                2,
                limitedWifi.copy(transport = Transport.CELLULAR)
            )
        )
    }
}
