package com.terminator364.kinlink.core

enum class NameResolutionPath {
    PRIMARY,
    FRESH_CACHE,
    SECONDARY,
    SYSTEM_FAIL_OPEN
}

data class NameResolutionInput(
    val primarySucceeded: Boolean,
    val freshCacheAvailable: Boolean,
    val secondarySucceeded: Boolean,
    val metered: Boolean,
    val budgetState: BudgetState
)

object NameResolutionPolicy {
    fun choose(input: NameResolutionInput): NameResolutionPath {
        if (input.primarySucceeded) return NameResolutionPath.PRIMARY
        if (input.freshCacheAvailable) return NameResolutionPath.FRESH_CACHE

        val paidDataGuard = input.metered && (
            input.budgetState == BudgetState.BUNDLE_LOW ||
                input.budgetState == BudgetState.BUNDLE_EXHAUSTED ||
                input.budgetState == BudgetState.BUNDLE_EXPIRED
            )

        if (!paidDataGuard && input.secondarySucceeded) return NameResolutionPath.SECONDARY
        return NameResolutionPath.SYSTEM_FAIL_OPEN
    }
}
