package com.terminator364.kinlink.core

enum class MobileVaultOfflineFallbackStatus {
    NOT_CONFIGURED,
    CONFIGURED_USAGE_UNKNOWN,
    READY_WITH_USER_USAGE,
    INVALID_CONFIGURATION
}

data class MobileVaultOfflineFallbackDecision(
    val status: MobileVaultOfflineFallbackStatus,
    val assessment: MobileVaultAssessment?
)

/**
 * B100: the user-entered plan is first-class and local.
 *
 * Usage Access, telephony permission, carrier login and cloud availability are
 * intentionally absent from this API: none is required to configure/use the
 * local fallback. Missing exact usage stays UNKNOWN rather than becoming zero.
 */
object MobileVaultOfflineFallbackPolicy {
    fun evaluate(
        config: MobilePlanConfig?,
        userReconciledUsedBytes: Long?,
        observedAtEpochMillis: Long?,
        nowEpochMillis: Long
    ): MobileVaultOfflineFallbackDecision {
        if (config == null) {
            return MobileVaultOfflineFallbackDecision(
                MobileVaultOfflineFallbackStatus.NOT_CONFIGURED,
                null
            )
        }
        if (!MobilePlanVaultPolicy.valid(config)) {
            return MobileVaultOfflineFallbackDecision(
                MobileVaultOfflineFallbackStatus.INVALID_CONFIGURATION,
                MobilePlanVaultPolicy.evaluate(config, null, nowEpochMillis)
            )
        }

        val usage =
            if (
                userReconciledUsedBytes != null &&
                userReconciledUsedBytes >= 0L &&
                observedAtEpochMillis != null &&
                observedAtEpochMillis > 0L
            ) {
                MobilePlanUsage(
                    usedBytes = userReconciledUsedBytes,
                    source = MobilePlanUsageSource.USER_RECONCILED,
                    observedAtEpochMillis = observedAtEpochMillis
                )
            } else {
                null
            }

        val assessment =
            MobilePlanVaultPolicy.evaluate(config, usage, nowEpochMillis)
        return MobileVaultOfflineFallbackDecision(
            status =
                if (usage == null) {
                    MobileVaultOfflineFallbackStatus.CONFIGURED_USAGE_UNKNOWN
                } else {
                    MobileVaultOfflineFallbackStatus.READY_WITH_USER_USAGE
                },
            assessment = assessment
        )
    }
}
