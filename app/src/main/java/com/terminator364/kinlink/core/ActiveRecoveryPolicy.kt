package com.terminator364.kinlink.core

enum class RecoveryBlockReason {
    NONE,
    OBSERVATION_ONLY,
    NON_WIFI
}

object ActiveRecoveryPolicy {
    fun allowed(mode: RecoveryMode, transport: Transport): Boolean =
        blockReason(mode, transport) == RecoveryBlockReason.NONE

    fun blockReason(mode: RecoveryMode, transport: Transport): RecoveryBlockReason = when {
        mode == RecoveryMode.OBSERVATION_ONLY -> RecoveryBlockReason.OBSERVATION_ONLY
        transport != Transport.WIFI -> RecoveryBlockReason.NON_WIFI
        else -> RecoveryBlockReason.NONE
    }
}
