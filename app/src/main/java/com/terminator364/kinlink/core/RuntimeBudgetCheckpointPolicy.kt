package com.terminator364.kinlink.core

object RuntimeBudgetCheckpointPolicy {
    const val MIN_CHECKPOINT_AGE_MS = 30L * 60L * 1000L

    fun shouldCheckpoint(
        startElapsedMillis: Long?,
        nowElapsedMillis: Long,
        alreadyWritten: Boolean
    ): Boolean {
        if (alreadyWritten || startElapsedMillis == null) return false
        val age = (nowElapsedMillis - startElapsedMillis).coerceAtLeast(0L)
        return age >= MIN_CHECKPOINT_AGE_MS
    }
}
