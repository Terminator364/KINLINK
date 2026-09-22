package com.terminator364.kinlink.core

import android.content.Context
import java.security.MessageDigest

object TrustedWifiContextPolicy {
    fun fingerprint(truth: NetworkTruth): String? {
        if (truth.transport != Transport.WIFI) return null
        val iface = truth.interfaceName?.trim().orEmpty()
        val gateway = truth.gateway?.trim().orEmpty()
        if (iface.isEmpty() || gateway.isEmpty()) return null
        val canonical = listOf(
            "v1", iface, gateway,
            truth.dnsServerCount.coerceAtLeast(0).toString(),
            truth.privateDnsActive.toString(),
            truth.hasIpv4Address.toString(),
            truth.hasIpv6Address.toString(),
            truth.hasIpv4DefaultRoute.toString(),
            truth.hasIpv6DefaultRoute.toString()
        ).joinToString("|")
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    fun mayAuthorizeActiveRecovery(): Boolean = false
}

class TrustedWifiContextStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isHome(fingerprint: String): Boolean =
        fingerprint.isNotBlank() && fingerprint == prefs.getString(KEY_HOME, null)

    fun isTrusted(fingerprint: String): Boolean =
        fingerprint.isNotBlank() &&
            (isHome(fingerprint) ||
                prefs.getStringSet(KEY_TRUSTED, emptySet()).orEmpty().contains(fingerprint))

    fun markHome(fingerprint: String): Boolean {
        if (!validDigest(fingerprint)) return false
        return prefs.edit()
            .putString(KEY_HOME, fingerprint)
            .putStringSet(
                KEY_TRUSTED,
                prefs.getStringSet(KEY_TRUSTED, emptySet()).orEmpty()
                    .toMutableSet().apply { add(fingerprint) }
            )
            .commit()
    }

    fun markTrusted(fingerprint: String): Boolean {
        if (!validDigest(fingerprint)) return false
        val updated = prefs.getStringSet(KEY_TRUSTED, emptySet()).orEmpty()
            .toMutableSet().apply { add(fingerprint) }
        return prefs.edit().putStringSet(KEY_TRUSTED, updated).commit()
    }

    fun forget(fingerprint: String): Boolean {
        if (!validDigest(fingerprint)) return false
        val updated = prefs.getStringSet(KEY_TRUSTED, emptySet()).orEmpty()
            .toMutableSet().apply { remove(fingerprint) }
        val editor = prefs.edit().putStringSet(KEY_TRUSTED, updated)
        if (isHome(fingerprint)) editor.remove(KEY_HOME)
        return editor.commit()
    }

    private fun validDigest(value: String): Boolean =
        value.length == 64 && value.all { it in '0'..'9' || it in 'a'..'f' }

    companion object {
        private const val PREFS = "kinlink_trusted_wifi_context_v1"
        private const val KEY_HOME = "home_digest"
        private const val KEY_TRUSTED = "trusted_digests"
    }
}
