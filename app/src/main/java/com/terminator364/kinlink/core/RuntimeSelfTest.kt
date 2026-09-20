package com.terminator364.kinlink.core

import android.content.Context

data class RuntimeSelfTestResult(
    val pass: Boolean,
    val summary: String
)

object RuntimeSelfTestPolicy {
    fun core(databaseVersion: Int, recoveryModeReadable: Boolean): RuntimeSelfTestResult =
        if (databaseVersion >= 5 && recoveryModeReadable) {
            RuntimeSelfTestResult(true, "Base/migration et mode de récupération lisibles.")
        } else {
            RuntimeSelfTestResult(
                false,
                "Self-test core incomplet: dbVersion=$databaseVersion; recoveryModeReadable=$recoveryModeReadable"
            )
        }

    fun observerCallback(truthReceived: Boolean): RuntimeSelfTestResult =
        if (truthReceived) {
            RuntimeSelfTestResult(true, "Premier callback réseau reçu.")
        } else {
            RuntimeSelfTestResult(false, "Aucun callback réseau reçu.")
        }
}

class PostUpdateSelfTestStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun needsCoreTest(versionCode: Long): Boolean =
        prefs.getLong(KEY_CORE_VERSION, -1L) != versionCode

    fun markCoreTested(versionCode: Long) {
        prefs.edit().putLong(KEY_CORE_VERSION, versionCode).apply()
    }

    fun needsObserverTest(versionCode: Long): Boolean =
        prefs.getLong(KEY_OBSERVER_VERSION, -1L) != versionCode

    fun markObserverTested(versionCode: Long) {
        prefs.edit().putLong(KEY_OBSERVER_VERSION, versionCode).apply()
    }

    companion object {
        private const val PREFS = "kinlink_post_update_self_test"
        private const val KEY_CORE_VERSION = "core_version"
        private const val KEY_OBSERVER_VERSION = "observer_version"
    }
}
