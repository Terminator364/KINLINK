package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.LinkProperties

class NetworkObserver(
    context: Context,
    private val onTruth: (NetworkTruth) -> Unit
) {
    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private var registered = false
    private var lastFingerprint: String? = null

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = publish(network)
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = publish(network, caps)
        override fun onLinkPropertiesChanged(network: Network, lp: LinkProperties) = publish(network, null, lp)
        override fun onLost(network: Network) = publish(cm.activeNetwork)
    }

    fun start() {
        if (registered) return
        registered = true
        cm.registerDefaultNetworkCallback(callback)
        publish(cm.activeNetwork)
    }

    fun stop() {
        if (!registered) return
        registered = false
        runCatching { cm.unregisterNetworkCallback(callback) }
    }

    private fun publish(
        network: Network?,
        providedCaps: NetworkCapabilities? = null,
        providedLp: LinkProperties? = null
    ) {
        val active = network ?: cm.activeNetwork
        val caps = providedCaps ?: active?.let(cm::getNetworkCapabilities)
        val lp = providedLp ?: active?.let(cm::getLinkProperties)
        val truth = ConnectivityTruthEngine.reduce(caps, lp)
        val fingerprint = truth.telemetryFingerprint()
        if (fingerprint == lastFingerprint) return
        lastFingerprint = fingerprint
        onTruth(truth)
    }
}
