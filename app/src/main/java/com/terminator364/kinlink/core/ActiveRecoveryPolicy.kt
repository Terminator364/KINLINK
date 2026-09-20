package com.terminator364.kinlink.core

object ActiveRecoveryPolicy {
    fun allowed(mode: RecoveryMode, transport: Transport): Boolean =
        mode == RecoveryMode.AUTOMATIC && transport == Transport.WIFI
}
