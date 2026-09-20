package com.terminator364.kinlink.core

object NotificationUpdatePolicy {
    fun shouldPublish(previousText: String?, nextText: String): Boolean =
        previousText != nextText
}
