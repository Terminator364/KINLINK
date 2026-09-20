package com.terminator364.kinlink.core

import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkLossSettlingPolicyTest {
    @Test fun lossSettleWindowIsShortAndBounded() {
        assertTrue(NetworkLossSettlingPolicy.LOSS_SETTLE_MS in 500L..2_000L)
    }
}
