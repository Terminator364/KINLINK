package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultNetworkCallbackAcceptancePolicyTest {
    @Test fun snapshotRequiresCapabilitiesAndLinkPropertiesOnSameActiveNetwork() {
        assertFalse(
            DefaultNetworkCallbackAcceptancePolicy.snapshotReady(
                hasCapabilities = true,
                hasLinkProperties = false,
                callbackMatchedBeforeReduction = true,
                callbackMatchedAfterReduction = true
            )
        )
        assertFalse(
            DefaultNetworkCallbackAcceptancePolicy.snapshotReady(
                hasCapabilities = true,
                hasLinkProperties = true,
                callbackMatchedBeforeReduction = true,
                callbackMatchedAfterReduction = false
            )
        )
        assertTrue(
            DefaultNetworkCallbackAcceptancePolicy.snapshotReady(
                hasCapabilities = true,
                hasLinkProperties = true,
                callbackMatchedBeforeReduction = true,
                callbackMatchedAfterReduction = true
            )
        )
    }

    @Test fun explicitCallbackMustStillBeActiveDefault() {
        assertTrue(DefaultNetworkCallbackAcceptancePolicy.accept(true, true))
        assertFalse(DefaultNetworkCallbackAcceptancePolicy.accept(true, false))
    }

    @Test fun explicitRefreshWithoutCallbackNetworkMayReadCurrentDefault() {
        assertTrue(DefaultNetworkCallbackAcceptancePolicy.accept(false, false))
    }
    @Test fun callbackMustRemainDefaultAcrossReductionWindow() {
        assertTrue(
            DefaultNetworkCallbackAcceptancePolicy.acceptStable(
                callbackNetworkPresent = true,
                callbackMatchedBeforeReduction = true,
                callbackMatchedAfterReduction = true
            )
        )
        assertFalse(
            DefaultNetworkCallbackAcceptancePolicy.acceptStable(
                callbackNetworkPresent = true,
                callbackMatchedBeforeReduction = true,
                callbackMatchedAfterReduction = false
            )
        )
        assertFalse(
            DefaultNetworkCallbackAcceptancePolicy.acceptStable(
                callbackNetworkPresent = true,
                callbackMatchedBeforeReduction = false,
                callbackMatchedAfterReduction = true
            )
        )
    }

    @Test fun currentDefaultRefreshWithoutCallbackNetworkRemainsAllowed() {
        assertTrue(
            DefaultNetworkCallbackAcceptancePolicy.acceptStable(
                callbackNetworkPresent = false,
                callbackMatchedBeforeReduction = false,
                callbackMatchedAfterReduction = false
            )
        )
    }

    @Test fun onlyCurrentDefaultCallbackCancelsPendingLossSettle() {
        assertTrue(
            DefaultNetworkCallbackAcceptancePolicy.shouldCancelPendingLoss(
                callbackMatchesActive = true
            )
        )
        assertFalse(
            DefaultNetworkCallbackAcceptancePolicy.shouldCancelPendingLoss(
                callbackMatchesActive = false
            )
        )
    }

}
