package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ActiveDataSubscriptionPolicyTest {
    @Test
    fun firstAvailableSelectionsCreateIndependentGenerations() {
        val state = ActiveDataSubscriptionPolicy.advance(
            previous = ActiveDataSubscriptionState(),
            defaultDataAvailable = true,
            defaultDataChanged = false,
            activeDataAvailable = true,
            activeDataChanged = false,
            relation = DefaultActiveDataRelation.SAME
        )

        assertEquals(1L, state.defaultDataGeneration)
        assertEquals(1L, state.activeDataGeneration)
        assertEquals(DefaultActiveDataRelation.SAME, state.relation)
    }

    @Test
    fun defaultSelectionChangeDoesNotInvalidateActiveEvidenceWhenActiveIsUnchanged() {
        val first = ActiveDataSubscriptionPolicy.advance(
            previous = ActiveDataSubscriptionState(),
            defaultDataAvailable = true,
            defaultDataChanged = false,
            activeDataAvailable = true,
            activeDataChanged = false,
            relation = DefaultActiveDataRelation.SAME
        )
        val second = ActiveDataSubscriptionPolicy.advance(
            previous = first,
            defaultDataAvailable = true,
            defaultDataChanged = true,
            activeDataAvailable = true,
            activeDataChanged = false,
            relation = DefaultActiveDataRelation.DIFFERENT
        )

        assertEquals(2L, second.defaultDataGeneration)
        assertEquals(1L, second.activeDataGeneration)
        assertEquals(
            SubscriptionEvidenceFreshness.CURRENT_ACTIVE_DATA_GENERATION,
            ActiveDataSubscriptionPolicy.evidenceFreshness(
                second,
                evidenceActiveDataGeneration = 1L
            )
        )
    }

    @Test
    fun activeSelectionChangeInvalidatesEvidenceEvenWhenDefaultIsUnchanged() {
        val first = ActiveDataSubscriptionPolicy.advance(
            previous = ActiveDataSubscriptionState(),
            defaultDataAvailable = true,
            defaultDataChanged = false,
            activeDataAvailable = true,
            activeDataChanged = false,
            relation = DefaultActiveDataRelation.SAME
        )
        val second = ActiveDataSubscriptionPolicy.advance(
            previous = first,
            defaultDataAvailable = true,
            defaultDataChanged = false,
            activeDataAvailable = true,
            activeDataChanged = true,
            relation = DefaultActiveDataRelation.DIFFERENT
        )

        assertEquals(1L, second.defaultDataGeneration)
        assertEquals(2L, second.activeDataGeneration)
        assertEquals(
            SubscriptionEvidenceFreshness.STALE_ACTIVE_DATA_GENERATION,
            ActiveDataSubscriptionPolicy.evidenceFreshness(
                second,
                evidenceActiveDataGeneration = first.activeDataGeneration
            )
        )
    }

    @Test
    fun lossOfActiveObservationMakesPerSubscriptionEvidenceUnknown() {
        val first = ActiveDataSubscriptionPolicy.advance(
            previous = ActiveDataSubscriptionState(),
            defaultDataAvailable = true,
            defaultDataChanged = false,
            activeDataAvailable = true,
            activeDataChanged = false,
            relation = DefaultActiveDataRelation.SAME
        )
        val unavailable = ActiveDataSubscriptionPolicy.advance(
            previous = first,
            defaultDataAvailable = true,
            defaultDataChanged = false,
            activeDataAvailable = false,
            activeDataChanged = false,
            relation = DefaultActiveDataRelation.SAME
        )

        assertEquals(DefaultActiveDataRelation.UNKNOWN, unavailable.relation)
        assertEquals(
            SubscriptionEvidenceFreshness.ACTIVE_DATA_UNKNOWN,
            ActiveDataSubscriptionPolicy.evidenceFreshness(
                unavailable,
                evidenceActiveDataGeneration = first.activeDataGeneration
            )
        )
    }

    @Test
    fun reappearanceAfterUnknownCreatesNewGenerationSoOldEvidenceStaysStale() {
        val first = ActiveDataSubscriptionPolicy.advance(
            previous = ActiveDataSubscriptionState(),
            defaultDataAvailable = true,
            defaultDataChanged = false,
            activeDataAvailable = true,
            activeDataChanged = false,
            relation = DefaultActiveDataRelation.SAME
        )
        val unavailable = ActiveDataSubscriptionPolicy.advance(
            previous = first,
            defaultDataAvailable = true,
            defaultDataChanged = false,
            activeDataAvailable = false,
            activeDataChanged = false,
            relation = DefaultActiveDataRelation.UNKNOWN
        )
        val returned = ActiveDataSubscriptionPolicy.advance(
            previous = unavailable,
            defaultDataAvailable = true,
            defaultDataChanged = false,
            activeDataAvailable = true,
            activeDataChanged = false,
            relation = DefaultActiveDataRelation.SAME
        )

        assertEquals(2L, returned.activeDataGeneration)
        assertEquals(
            SubscriptionEvidenceFreshness.STALE_ACTIVE_DATA_GENERATION,
            ActiveDataSubscriptionPolicy.evidenceFreshness(
                returned,
                evidenceActiveDataGeneration = first.activeDataGeneration
            )
        )
    }

    @Test
    fun relationCannotClaimSameOrDifferentWhenEitherSideIsUnavailable() {
        val state = ActiveDataSubscriptionPolicy.advance(
            previous = ActiveDataSubscriptionState(),
            defaultDataAvailable = false,
            defaultDataChanged = false,
            activeDataAvailable = true,
            activeDataChanged = false,
            relation = DefaultActiveDataRelation.DIFFERENT
        )

        assertEquals(DefaultActiveDataRelation.UNKNOWN, state.relation)
    }
}
