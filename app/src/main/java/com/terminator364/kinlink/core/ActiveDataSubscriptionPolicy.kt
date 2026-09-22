package com.terminator364.kinlink.core

enum class DefaultActiveDataRelation {
    SAME,
    DIFFERENT,
    UNKNOWN
}

enum class SubscriptionEvidenceFreshness {
    CURRENT_ACTIVE_DATA_GENERATION,
    STALE_ACTIVE_DATA_GENERATION,
    ACTIVE_DATA_UNKNOWN
}

data class ActiveDataSubscriptionState(
    val defaultDataGeneration: Long = 0L,
    val activeDataGeneration: Long = 0L,
    val defaultDataAvailable: Boolean = false,
    val activeDataAvailable: Boolean = false,
    val relation: DefaultActiveDataRelation = DefaultActiveDataRelation.UNKNOWN
)

/**
 * B95 privacy-preserving routing semantics.
 *
 * This policy intentionally consumes only availability/change facts. It never
 * receives or persists Android subscription identifiers. A future platform
 * adapter may compare identities locally, but the core only receives whether
 * the default or active selection changed and their coarse relation.
 */
object ActiveDataSubscriptionPolicy {
    fun advance(
        previous: ActiveDataSubscriptionState,
        defaultDataAvailable: Boolean,
        defaultDataChanged: Boolean,
        activeDataAvailable: Boolean,
        activeDataChanged: Boolean,
        relation: DefaultActiveDataRelation
    ): ActiveDataSubscriptionState {
        val defaultGeneration = when {
            !defaultDataAvailable -> previous.defaultDataGeneration
            !previous.defaultDataAvailable && defaultDataAvailable ->
                previous.defaultDataGeneration + 1L
            previous.defaultDataAvailable && defaultDataChanged ->
                previous.defaultDataGeneration + 1L
            else -> previous.defaultDataGeneration
        }

        val activeGeneration = when {
            !activeDataAvailable -> previous.activeDataGeneration
            !previous.activeDataAvailable && activeDataAvailable ->
                previous.activeDataGeneration + 1L
            previous.activeDataAvailable && activeDataChanged ->
                previous.activeDataGeneration + 1L
            else -> previous.activeDataGeneration
        }

        val safeRelation =
            if (defaultDataAvailable && activeDataAvailable) {
                relation
            } else {
                DefaultActiveDataRelation.UNKNOWN
            }

        return ActiveDataSubscriptionState(
            defaultDataGeneration = defaultGeneration,
            activeDataGeneration = activeGeneration,
            defaultDataAvailable = defaultDataAvailable,
            activeDataAvailable = activeDataAvailable,
            relation = safeRelation
        )
    }

    fun evidenceFreshness(
        current: ActiveDataSubscriptionState,
        evidenceActiveDataGeneration: Long
    ): SubscriptionEvidenceFreshness {
        if (!current.activeDataAvailable || current.activeDataGeneration <= 0L) {
            return SubscriptionEvidenceFreshness.ACTIVE_DATA_UNKNOWN
        }
        return if (evidenceActiveDataGeneration == current.activeDataGeneration) {
            SubscriptionEvidenceFreshness.CURRENT_ACTIVE_DATA_GENERATION
        } else {
            SubscriptionEvidenceFreshness.STALE_ACTIVE_DATA_GENERATION
        }
    }
}
