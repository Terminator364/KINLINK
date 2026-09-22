package com.terminator364.kinlink.core

enum class SubscriptionAttributionMode {
    AGGREGATE_ONLY,
    LOCAL_SUBSCRIPTION_ID
}

data class SubscriptionIdentityDecision(
    val mode: SubscriptionAttributionMode,
    val reason: String
)

/**
 * B94 privacy boundary.
 *
 * KINLINK stays aggregate-only unless a separately justified product need and
 * an explicit phone-state permission are both proven. Even then, subscription
 * identity is local-only: it is not diagnostic/export data and non-resettable
 * identifiers remain forbidden.
 */
object SubscriptionIdentityPolicy {
    fun decide(
        perSubscriptionNeedJustified: Boolean,
        phoneStatePermissionGranted: Boolean
    ): SubscriptionIdentityDecision {
        val localSubscriptionAllowed =
            perSubscriptionNeedJustified && phoneStatePermissionGranted

        return if (localSubscriptionAllowed) {
            SubscriptionIdentityDecision(
                mode = SubscriptionAttributionMode.LOCAL_SUBSCRIPTION_ID,
                reason =
                    "Per-subscription need and explicit permission are both proven; local subscription association may be considered."
            )
        } else {
            SubscriptionIdentityDecision(
                mode = SubscriptionAttributionMode.AGGREGATE_ONLY,
                reason =
                    "Per-subscription identity is not authorized; preserve aggregate/UNKNOWN semantics."
            )
        }
    }

    fun mayExportSubscriptionIdentity(): Boolean = false

    fun mayUseNonResettableIdentifier(): Boolean = false
}
