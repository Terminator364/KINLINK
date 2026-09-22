package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SubscriptionIdentityPolicyTest {
    @Test
    fun defaultWithoutNeedOrPermissionIsAggregateOnly() {
        val decision = SubscriptionIdentityPolicy.decide(
            perSubscriptionNeedJustified = false,
            phoneStatePermissionGranted = false
        )
        assertEquals(SubscriptionAttributionMode.AGGREGATE_ONLY, decision.mode)
    }

    @Test
    fun justifiedNeedWithoutPermissionStillFallsBackToAggregate() {
        val decision = SubscriptionIdentityPolicy.decide(
            perSubscriptionNeedJustified = true,
            phoneStatePermissionGranted = false
        )
        assertEquals(SubscriptionAttributionMode.AGGREGATE_ONLY, decision.mode)
    }

    @Test
    fun permissionWithoutJustifiedNeedStillFallsBackToAggregate() {
        val decision = SubscriptionIdentityPolicy.decide(
            perSubscriptionNeedJustified = false,
            phoneStatePermissionGranted = true
        )
        assertEquals(SubscriptionAttributionMode.AGGREGATE_ONLY, decision.mode)
    }

    @Test
    fun localSubscriptionModeNeedsBothIndependentGates() {
        val decision = SubscriptionIdentityPolicy.decide(
            perSubscriptionNeedJustified = true,
            phoneStatePermissionGranted = true
        )
        assertEquals(
            SubscriptionAttributionMode.LOCAL_SUBSCRIPTION_ID,
            decision.mode
        )
    }

    @Test
    fun subscriptionIdentityNeverEntersExportsAndNonResettableIdsStayForbidden() {
        assertFalse(SubscriptionIdentityPolicy.mayExportSubscriptionIdentity())
        assertFalse(SubscriptionIdentityPolicy.mayUseNonResettableIdentifier())
    }
}
