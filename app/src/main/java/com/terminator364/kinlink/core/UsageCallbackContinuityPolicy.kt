package com.terminator364.kinlink.core

enum class UsageCallbackContinuityStatus {
    FRESH_RECONCILIATION_REQUIRED,
    RECONCILED_THIS_PROCESS
}

data class UsageCallbackProcessState(
    val processGeneration: Long,
    val callbackObservedThisProcess: Boolean,
    val freshReconciliationObservedAtEpochMillis: Long?
)

/**
 * B96: usage callbacks are advisory hints only while the process is alive.
 *
 * Callback continuity is never durable truth. A new process generation always
 * requires a fresh reconciliation before usage may influence spending.
 */
object UsageCallbackContinuityPolicy {
    fun startProcess(previousProcessGeneration: Long): UsageCallbackProcessState =
        UsageCallbackProcessState(
            processGeneration = previousProcessGeneration.coerceAtLeast(0L) + 1L,
            callbackObservedThisProcess = false,
            freshReconciliationObservedAtEpochMillis = null
        )

    fun observeCallback(
        state: UsageCallbackProcessState
    ): UsageCallbackProcessState =
        state.copy(callbackObservedThisProcess = true)

    fun recordFreshReconciliation(
        state: UsageCallbackProcessState,
        observedAtEpochMillis: Long
    ): UsageCallbackProcessState =
        if (observedAtEpochMillis > 0L) {
            state.copy(
                freshReconciliationObservedAtEpochMillis =
                    observedAtEpochMillis
            )
        } else {
            state
        }

    fun status(
        state: UsageCallbackProcessState
    ): UsageCallbackContinuityStatus =
        if (state.freshReconciliationObservedAtEpochMillis != null) {
            UsageCallbackContinuityStatus.RECONCILED_THIS_PROCESS
        } else {
            UsageCallbackContinuityStatus.FRESH_RECONCILIATION_REQUIRED
        }

    fun mayUseUsageForSpending(
        state: UsageCallbackProcessState
    ): Boolean =
        status(state) ==
            UsageCallbackContinuityStatus.RECONCILED_THIS_PROCESS

    fun usageImpliedByMissingCallbackBytes(): Long? = null
}
