package com.terminator364.kinlink.core

/**
 * One best-effort relapse check after a sustained correlated improvement.
 *
 * Handler delay does not request an exact alarm and does not wake a sleeping
 * device. Natural Android callbacks remain the primary observation source.
 */
object MobileAssistRelapseGuardPolicy {
    const val RECHECK_DELAY_MS = 5L * 60L * 1000L
    const val MAX_RECHECKS_PER_SUSTAINED_EVENT = 1

    fun shouldReevaluate(result: MobileAssistEvidenceResult): Boolean =
        result == MobileAssistEvidenceResult.RELAPSED_AFTER_SUSTAINED
}
