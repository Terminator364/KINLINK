package com.terminator364.kinlink.core

import android.content.Context
import android.content.Intent

enum class StartupSource {
    BOOT_COMPLETED,
    PACKAGE_REPLACED,
    OTHER
}

data class PendingStartupReceipt(
    val source: StartupSource,
    val success: Boolean,
    val detail: String
)

object StartupSourcePolicy {
    fun fromAction(action: String?): StartupSource = when (action) {
        Intent.ACTION_BOOT_COMPLETED -> StartupSource.BOOT_COMPLETED
        Intent.ACTION_MY_PACKAGE_REPLACED -> StartupSource.PACKAGE_REPLACED
        else -> StartupSource.OTHER
    }
}

class StartupReceiptStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun note(source: StartupSource, success: Boolean, detail: String) {
        prefs.edit()
            .putString(KEY_SOURCE, source.name)
            .putBoolean(KEY_SUCCESS, success)
            .putString(KEY_DETAIL, detail.take(160))
            .putBoolean(KEY_PENDING, true)
            .apply()
    }

    fun consume(): PendingStartupReceipt? {
        if (!prefs.getBoolean(KEY_PENDING, false)) return null
        val source = runCatching {
            StartupSource.valueOf(prefs.getString(KEY_SOURCE, StartupSource.OTHER.name)!!)
        }.getOrDefault(StartupSource.OTHER)
        val result = PendingStartupReceipt(
            source = source,
            success = prefs.getBoolean(KEY_SUCCESS, false),
            detail = prefs.getString(KEY_DETAIL, "") ?: ""
        )
        prefs.edit().putBoolean(KEY_PENDING, false).apply()
        return result
    }

    companion object {
        private const val PREFS = "kinlink_startup_receipts"
        private const val KEY_SOURCE = "source"
        private const val KEY_SUCCESS = "success"
        private const val KEY_DETAIL = "detail"
        private const val KEY_PENDING = "pending"
    }
}
