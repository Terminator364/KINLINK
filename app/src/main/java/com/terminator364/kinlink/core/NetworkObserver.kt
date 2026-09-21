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

    fun snapshotReady(
        hasCapabilities: Boolean,
        hasLinkProperties: Boolean,
        callbackMatchedBeforeReduction: Boolean,
        callbackMatchedAfterReduction: Boolean
    ): Boolean =
        hasCapabilities &&
            hasLinkProperties &&
            callbackMatchedBeforeReduction &&
            callbackMatchedAfterReduction

    fun acceptStable(
        callbackNetworkPresent: Boolean,
        callbackMatchedBeforeReduction: Boolean,
        callbackMatchedAfterReduction: Boolean
    ): Boolean =
        !callbackNetworkPresent ||
            (callbackMatchedBeforeReduction && callbackMatchedAfterReduction)

    fun shouldCancelPendingLoss(callbackMatchesActive: Boolean): Boolean =
        callbackMatchesActive
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
    private var callbackNetwork: Network? = null
    private var callbackCapabilities: NetworkCapabilities? = null
    private var callbackLinkProperties: LinkProperties? = null

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (network != cm.activeNetwork) return
            lossGeneration += 1L
            callbackNetwork = network
            callbackCapabilities = null
            callbackLinkProperties = null
            // Android delivers capabilities/link properties after onAvailable.
            // Do not synchronously query them here: that creates a documented race.
        }

        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            if (network != cm.activeNetwork) return
            lossGeneration += 1L
            if (callbackNetwork != network) {
                callbackNetwork = network
                callbackLinkProperties = null
            }
            callbackCapabilities = caps
            publishCallbackSnapshotIfReady(network)
        }

        override fun onLinkPropertiesChanged(network: Network, lp: LinkProperties) {
            if (network != cm.activeNetwork) return
            if (callbackNetwork != network) {
                callbackNetwork = network
                callbackCapabilities = null
            }
            callbackLinkProperties = lp
            publishCallbackSnapshotIfReady(network)
        }

        override fun onLost(network: Network) {
            if (callbackNetwork == network) {
                callbackNetwork = null
                callbackCapabilities = null
                callbackLinkProperties = null
            }
            val generation = ++lossGeneration
            handler.postDelayed({
                if (!registered || generation != lossGeneration) return@postDelayed
                if (cm.activeNetwork == null) {
                    publishTruth(NetworkTruth())
                }
                // If another default network exists, its callback sequence will publish
                // once both capabilities and link properties are available.
            }, NetworkLossSettlingPolicy.LOSS_SETTLE_MS)
        }
    }

    fun start() {
        if (registered) return
        runCatching {
            cm.registerDefaultNetworkCallback(callback)
            registered = true
        }.onFailure {
            // Fail-open: no retry loop and no Android network change.
            publishTruth(NetworkTruth())
        }
    }

    fun stop() {
        if (!registered) return
        registered = false
        lossGeneration += 1L
        handler.removeCallbacksAndMessages(null)
        callbackNetwork = null
        callbackCapabilities = null
        callbackLinkProperties = null
        runCatching { cm.unregisterNetworkCallback(callback) }
    }

    private fun publishCallbackSnapshotIfReady(network: Network) {
        runCatching {
            val activeBefore = cm.activeNetwork
            val caps = callbackCapabilities
            val lp = callbackLinkProperties
            val activeAfter = cm.activeNetwork
            if (!DefaultNetworkCallbackAcceptancePolicy.snapshotReady(
                    hasCapabilities = caps != null,
                    hasLinkProperties = lp != null,
                    callbackMatchedBeforeReduction = network == activeBefore,
                    callbackMatchedAfterReduction = network == activeAfter
                )
            ) {
                return
            }
            publishTruth(ConnectivityTruthEngine.reduce(caps, lp))
        }.onFailure {
            // Fail open: a malformed callback snapshot cannot seize routing or loop.
        }
    }

    private fun publishTruth(truth: NetworkTruth) {
        val fingerprint = truth.telemetryFingerprint()
        if (fingerprint == lastFingerprint) return
        lastFingerprint = fingerprint
        runCatching { onTruth(truth) }
    }
}
