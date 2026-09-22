package com.terminator364.kinlink.core

enum class NetworkStatsQueryGateStatus {
    STARTED,
    IN_FLIGHT,
    SESSION_DISABLED
}

data class NetworkStatsQueryGateDecision(
    val status: NetworkStatsQueryGateStatus,
    val token: Long? = null
)

/**
 * Process-session gate for the optional NetworkStats query path.
 *
 * At most one query may be active. A deadline breach permanently disables the
 * optional path until process restart so a slow platform call can never cause
 * request stacking or block the KINLINK cockpit/recovery control path.
 */
class NetworkStatsQuerySessionGate {
    private var activeToken: Long? = null
    private var nextToken = 1L
    private var sessionDisabled = false

    @Synchronized
    fun tryStart(): NetworkStatsQueryGateDecision {
        if (sessionDisabled) {
            return NetworkStatsQueryGateDecision(
                NetworkStatsQueryGateStatus.SESSION_DISABLED
            )
        }
        if (activeToken != null) {
            return NetworkStatsQueryGateDecision(
                NetworkStatsQueryGateStatus.IN_FLIGHT
            )
        }

        val token = nextToken++
        activeToken = token
        return NetworkStatsQueryGateDecision(
            status = NetworkStatsQueryGateStatus.STARTED,
            token = token
        )
    }

    @Synchronized
    fun complete(token: Long): Boolean {
        if (activeToken != token) return false
        activeToken = null
        return true
    }

    @Synchronized
    fun timeoutAndDisable(token: Long): Boolean {
        if (activeToken != token) return false
        activeToken = null
        sessionDisabled = true
        return true
    }

    @Synchronized
    fun disableForSession() {
        activeToken = null
        sessionDisabled = true
    }

    @Synchronized
    fun isSessionDisabled(): Boolean = sessionDisabled

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 4_000L
    }
}
