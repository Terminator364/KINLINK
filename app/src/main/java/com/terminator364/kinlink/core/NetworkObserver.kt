package com.terminator364.kinlink.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.LinkProperties
import android.os.Handler
import android.os.Looper

object DefaultNetworkCallbackAcceptancePolicy {
    fun accept(callbackNetworkPresent: Boolean, callbackMatchesActive: Boolean): Boolean =
        !callbackNetworkPresent || callbackMatchesActive

    fun acceptStable(
        callbackNetworkPresent: Boolean,
        callbackMatchedBeforeReduction: Boolean,
        callbackMatchedAfterReduction: Boolean
    ): Boolean =
        !callbackNetworkPresent ||
            (callbackMatchedBeforeReduction && callbackMatchedAfterReduction)
}

/** Observer only: any internal failure leaves Android networking untouched. */
class NetworkObserver(
    context: Context,
    private val onTruth: (NetworkTruth) -> Unit
) {
    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private var registered = false
    private var lastFingerprint: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var lossGeneration = 0L

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            lossGeneration += 1L
            safePublish(network)
        }
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            lossGeneration += 1L
            safePublish(network, caps)
        }
        override fun onLinkPropertiesChanged(network: Network, lp: LinkProperties) =
            safePublish(network, null, lp)

        override fun onLost(network: Network) {
            val generation = ++lossGeneration
            handler.postDelayed({
                if (!registered || generation != lossGeneration) return@postDelayed
                safePublish(cm.activeNetwork)
            }, NetworkLossSettlingPolicy.LOSS_SETTLE_MS)
        }
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
        lossGeneration += 1L
        handler.removeCallbacksAndMessages(null)
        runCatching { cm.unregisterNetworkCallback(callback) }
    }

    private fun safePublish(
        network: Network?,
        providedCaps: NetworkCapabilities? = null,
        providedLp: LinkProperties? = null
    ) {
        runCatching {
            val activeNow = cm.activeNetwork
            if (!DefaultNetworkCallbackAcceptancePolicy.accept(
                    callbackNetworkPresent = network != null,
                    callbackMatchesActive = network != null && network == activeNow
                )
            ) {
                return
            }
            val active = activeNow
            val caps = providedCaps ?: active?.let(cm::getNetworkCapabilities)
            val lp = providedLp ?: active?.let(cm::getLinkProperties)
            val truth = ConnectivityTruthEngine.reduce(caps, lp)
            if (!DefaultNetworkCallbackAcceptancePolicy.acceptStable(
                    callbackNetworkPresent = network != null,
                    callbackMatchedBeforeReduction = network == activeNow,
                    callbackMatchedAfterReduction = network != null && network == cm.activeNetwork
                )
            ) {
                return
            }
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
