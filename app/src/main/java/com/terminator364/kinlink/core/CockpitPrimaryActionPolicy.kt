package com.terminator364.kinlink.core

enum class CockpitPrimaryAction {
    NONE,
    WIFI_ASSIST,
    MOBILE_ASSIST
}

object CockpitPrimaryActionPolicy {
    fun select(
        transport: Transport,
        recoveryMode: RecoveryMode
    ): CockpitPrimaryAction {
        if (recoveryMode == RecoveryMode.OBSERVATION_ONLY) {
            return CockpitPrimaryAction.NONE
        }
        return when (transport) {
            Transport.WIFI -> CockpitPrimaryAction.WIFI_ASSIST
            Transport.CELLULAR -> CockpitPrimaryAction.MOBILE_ASSIST
            else -> CockpitPrimaryAction.NONE
        }
    }
}
