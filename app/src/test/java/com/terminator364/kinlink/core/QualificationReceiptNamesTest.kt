package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class QualificationReceiptNamesTest {
    @Test fun namesAreVersionScoped() {
        val v = 8L
        assertEquals("SELF_TEST_CORE_V8", QualificationReceiptNames.coreSelfTest(v))
        assertEquals("SELF_TEST_OBSERVER_CALLBACK_V8", QualificationReceiptNames.observerSelfTest(v))
        assertEquals(
            "HANDOFF_WIFI_TO_CELLULAR_V8",
            QualificationReceiptNames.handoff(HandoffKind.WIFI_TO_CELLULAR, v)
        )
        assertEquals(
            "HANDOFF_OUTCOME_MOBILE_VALIDATED_V8",
            QualificationReceiptNames.handoffOutcome(HandoffOutcome.MOBILE_VALIDATED, v)
        )
        assertEquals(
            "RUNTIME_RESOURCE_GATE_PASS_V8",
            QualificationReceiptNames.resourceGate(RuntimeResourceVerdict.PASS, v)
        )
        assertEquals("FIELD_CANDIDATE_QUALIFIED_V8", QualificationReceiptNames.fieldQualified(v))
        assertEquals("FIELD_CANDIDATE_BLOCKED_V8", QualificationReceiptNames.fieldBlocked(v))
    }
}
