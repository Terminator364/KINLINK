package com.terminator364.kinlink.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class KinlinkBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val allowed = intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        if (!allowed) return
        runCatching {
            ContextCompat.startForegroundService(
                context,
                Intent(context, KinlinkObserverService::class.java)
            )
        }
    }
}
