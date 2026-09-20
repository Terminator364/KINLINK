package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryModePolicyTest {
    @Test fun modesRemainExplicitAndExhaustive() {
        assertEquals(
            setOf("AUTOMATIC", "OBSERVATION_ONLY"),
            RecoveryMode.entries.map { it.name }.toSet()
        )
    }
}
