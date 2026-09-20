package com.terminator364.kinlink.core

object NetworkLossSettlingPolicy {
    /**
     * Android can report onLost(oldDefault) immediately before onAvailable(newDefault)
     * during Wi-Fi/cellular handoff. Delay only the loss publication, never networking itself.
     */
    const val LOSS_SETTLE_MS = 1_500L
}
