package com.terminator364.kinlink.core

object NotificationPermissionPolicy {
    const val POST_NOTIFICATIONS_SDK = 33

    fun shouldRequest(sdkInt: Int, alreadyGranted: Boolean): Boolean =
        sdkInt >= POST_NOTIFICATIONS_SDK && !alreadyGranted
}
