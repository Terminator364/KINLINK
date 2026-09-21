package com.terminator364.kinlink.core

object TelemetryRetentionPolicy {
    const val NETWORK_EVENT_MAX_ROWS = 5_000
    const val ACTION_RECEIPT_MAX_ROWS = 500
    const val NETWORK_EVENT_MAX_AGE_MS = 7L * 24L * 60L * 60L * 1000L
    const val ACTION_RECEIPT_MAX_AGE_MS = 14L * 24L * 60L * 60L * 1000L

    val PINNED_QUALIFICATION_ACTION_PATTERNS = listOf(
        "SELF_TEST_CORE_V%",
        "SELF_TEST_OBSERVER_CALLBACK_V%",
        "FIELD_CANDIDATE_QUALIFIED_V%"
    )

    fun qualificationReceiptPinned(action: String): Boolean =
        action.startsWith("SELF_TEST_CORE_V") ||
            action.startsWith("SELF_TEST_OBSERVER_CALLBACK_V") ||
            action.startsWith("FIELD_CANDIDATE_QUALIFIED_V")

    fun networkCutoff(nowWallMs: Long): Long =
        nowWallMs - NETWORK_EVENT_MAX_AGE_MS

    fun actionCutoff(nowWallMs: Long): Long =
        nowWallMs - ACTION_RECEIPT_MAX_AGE_MS
}
