package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultNetworkCallbackAcceptancePolicyTest {
    @Test fun explicitCallbackMustStillBeActiveDefault() {
        assertTrue(DefaultNetworkCallbackAcceptancePolicy.accept(true, true))
        assertFalse(DefaultNetworkCallbackAcceptancePolicy.accept(true, false))
    }

    @Test fun explicitRefreshWithoutCallbackNetworkMayReadCurrentDefault() {
        assertTrue(DefaultNetworkCallbackAcceptancePolicy.accept(false, false))
    }
}
