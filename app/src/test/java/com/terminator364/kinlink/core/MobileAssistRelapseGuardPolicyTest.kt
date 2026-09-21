package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileAssistRelapseGuardPolicyTest {
    @Test fun onlyLateRelapseCanTriggerContinuousReevaluation() {
        for (result in MobileAssistEvidenceResult.values()) {
            val expected =
                result == MobileAssistEvidenceResult.RELAPSED_AFTER_SUSTAINED
            assertEquals(
                "result=$result",
                expected,
                MobileAssistRelapseGuardPolicy.shouldReevaluate(result)
            )
        }
    }

    @Test fun relapseRecheckIsSingleAndFiveMinutesLater() {
        assertEquals(
            1,
            MobileAssistRelapseGuardPolicy.MAX_RECHECKS_PER_SUSTAINED_EVENT
        )
        assertEquals(
            5L * 60L * 1000L,
            MobileAssistRelapseGuardPolicy.RECHECK_DELAY_MS
        )
    }
}
