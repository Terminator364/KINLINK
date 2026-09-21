package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPermissionPolicyTest {
    @Test fun modernAndroidRequestsOnlyWhenMissing() {
        assertTrue(NotificationPermissionPolicy.shouldRequest(33, false))
        assertTrue(NotificationPermissionPolicy.shouldRequest(37, false))
        assertFalse(NotificationPermissionPolicy.shouldRequest(37, true))
    }

    @Test fun preNotificationPermissionAndroidNeverRequests() {
        assertFalse(NotificationPermissionPolicy.shouldRequest(32, false))
    }
}
