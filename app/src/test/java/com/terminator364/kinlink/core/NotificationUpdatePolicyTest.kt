package com.terminator364.kinlink.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationUpdatePolicyTest {
    @Test fun identicalTextIsSuppressed() {
        assertFalse(NotificationUpdatePolicy.shouldPublish("same", "same"))
    }

    @Test fun changedTextIsPublished() {
        assertTrue(NotificationUpdatePolicy.shouldPublish("old", "new"))
    }

    @Test fun firstTextIsPublished() {
        assertTrue(NotificationUpdatePolicy.shouldPublish(null, "first"))
    }
}
