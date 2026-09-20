package com.terminator364.kinlink.core

enum class InterruptionSeverity {
    MICRO,
    SHORT,
    LONG
}

data class InterruptionEvidence(
    val severity: InterruptionSeverity,
    val durationMillis: Long,
    val fromTransport: Transport,
    val toTransport: Transport,
    val summary: String
)

class ConnectivityInterruptionTracker {
    private var lastValidated = false
    private var interruptionStartedAt: Long? = null
    private var interruptionFromTransport: Transport = Transport.UNKNOWN

    fun observe(truth: NetworkTruth): InterruptionEvidence? {
        val validated = truth.internetState == InternetState.VALIDATED

        if (lastValidated && !validated && interruptionStartedAt == null) {
            interruptionStartedAt = truth.observedAtMillis
            interruptionFromTransport = truth.transport
        }

        if (validated && interruptionStartedAt != null) {
            val start = interruptionStartedAt!!
            val duration = (truth.observedAtMillis - start).coerceAtLeast(0L)
            val severity = when {
                duration < 2_000L -> InterruptionSeverity.MICRO
                duration < 30_000L -> InterruptionSeverity.SHORT
                else -> InterruptionSeverity.LONG
            }
            interruptionStartedAt = null
            lastValidated = true
            return InterruptionEvidence(
                severity = severity,
                durationMillis = duration,
                fromTransport = interruptionFromTransport,
                toTransport = truth.transport,
                summary = "Internet rétabli après ${duration} ms (${severity.name.lowercase()})."
            )
        }

        lastValidated = validated
        return null
    }
}
