package com.terminator364.kinlink.core

import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MobileVaultFormPolicyTest {
    @Test fun parsesHumanMbAndGb() {
        assertEquals(500_000_000L, MobileVaultFormPolicy.parseDecimalBytes("500 MB"))
        assertEquals(1_500_000_000L, MobileVaultFormPolicy.parseDecimalBytes("1.5 GB"))
        assertEquals(1_500_000_000L, MobileVaultFormPolicy.parseDecimalBytes("1,5 gb"))
        assertEquals(250_000_000L, MobileVaultFormPolicy.parseDecimalBytes("250"))
    }

    @Test fun rejectsInvalidSizes() {
        assertNull(MobileVaultFormPolicy.parseDecimalBytes("-1 GB"))
        assertNull(MobileVaultFormPolicy.parseDecimalBytes("abc"))
    }

    @Test fun expiryDateIsInclusiveForUser() {
        val zone = ZoneId.of("Africa/Kinshasa")
        val epoch = MobileVaultFormPolicy.parseInclusiveExpiryDate("2026-09-30", zone)
        assertEquals(
            "2026-09-30",
            MobileVaultFormPolicy.formatInclusiveExpiryDate(epoch, zone)
        )
    }
}
