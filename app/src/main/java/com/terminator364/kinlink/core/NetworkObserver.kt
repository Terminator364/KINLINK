package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.LinkProperties

/** Observer only: any internal failure leaves Android networking untouched. */
class NetworkObserver(
    context: Context,
    private val onTruth: (NetworkTruth) -> Unit
) {
    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private var registered = false
    private var lastFingerprint: String? = null

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = safePublish(network)
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = safePublish(network, caps)
        override fun onLinkPropertiesChanged(network: Network, lp: LinkProperties) = safePublish(network, null, lp)
        override fun onLost(network: Network) = safePublish(cm.activeNetwork)
    }

    fun start() {
        if (registered) return
        runCatching {
            cm.registerDefaultNetworkCallback(callback)
            registered = true
            safePublish(cm.activeNetwork)
        }.onFailure {
            // Fail-open: no retry loop and no Android network change.
            deliver(NetworkTruth())
        }
    }

    fun stop() {
        if (!registered) return
        registered = false
        runCatching { cm.unregisterNetworkCallback(callback) }
    }

    private fun safePublish(
        network: Network?,
        providedCaps: NetworkCapabilities? = null,
        providedLp: LinkProperties? = null
    ) {
        runCatching {
            val active = network ?: cm.activeNetwork
            val caps = providedCaps ?: active?.let(cm::getNetworkCapabilities)
            val lp = providedLp ?: active?.let(cm::getLinkProperties)
            val truth = ConnectivityTruthEngine.reduce(caps, lp)
            val fingerprint = truth.telemetryFingerprint()
            if (fingerprint == lastFingerprint) return
            lastFingerprint = fingerprint
            deliver(truth)
        }.onFailure {
            // The observer may degrade, but it may never take ownership of routing.
            deliver(NetworkTruth())
        }
    }

    private fun deliver(truth: NetworkTruth) {
        runCatching { onTruth(truth) }
    }
}
