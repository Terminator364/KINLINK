package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class QualityTrendPolicyTest {
    @Test fun detectsImprovingTrend() {
        assertEquals(
            QualityTrend.IMPROVING,
            QualityTrendPolicy.classify(
                listOf("CONSTRAINED", "CONSTRAINED", "LIMITED", "COMFORTABLE")
            )
        )
    }

    @Test fun detectsDegradingTrend() {
        assertEquals(
            QualityTrend.DEGRADING,
            QualityTrendPolicy.classify(
                listOf("COMFORTABLE", "COMFORTABLE", "LIMITED", "CONSTRAINED")
            )
        )
    }

    @Test fun tooFewComparableSamplesIsInsufficient() {
        assertEquals(
            QualityTrend.INSUFFICIENT,
            QualityTrendPolicy.classify(listOf("UNKNOWN", "LIMITED"))
        )
    }
}
