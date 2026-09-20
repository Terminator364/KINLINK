package com.terminator364.kinlink.core

enum class ProbeResponsiveness {
    UNKNOWN,
    RESPONSIVE,
    SLOW,
    VERY_SLOW
}

object WifiProbeLatencyPolicy {
    /**
     * Heuristic over the tiny 204 micro-probe only. It is not throughput,
     * does not estimate Mbps, and never runs on cellular.
     */
    fun classify(latencyMillis: Long?): ProbeResponsiveness = when {
        latencyMillis == null || latencyMillis < 0L -> ProbeResponsiveness.UNKNOWN
        latencyMillis <= 250L -> ProbeResponsiveness.RESPONSIVE
        latencyMillis <= 800L -> ProbeResponsiveness.SLOW
        else -> ProbeResponsiveness.VERY_SLOW
    }

    fun label(value: ProbeResponsiveness): String = when (value) {
        ProbeResponsiveness.UNKNOWN -> "réactivité non mesurée"
        ProbeResponsiveness.RESPONSIVE -> "réactif"
        ProbeResponsiveness.SLOW -> "lent"
        ProbeResponsiveness.VERY_SLOW -> "très lent"
    }
}
