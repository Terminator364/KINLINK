package com.terminator364.kinlink.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.terminator364.kinlink.data.TelemetryLedger

class KinlinkObserverService : Service() {
    private lateinit var observer: NetworkObserver
    private lateinit var ledger: TelemetryLedger
    private lateinit var mobileBudget: MobileBudgetTracker
    private lateinit var recovery: AutopilotRecoveryController

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("KINLINK Autopilot")
            .setContentText("Résilience Wi-Fi active · données mobiles protégées")
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        ledger = TelemetryLedger(this)
        mobileBudget = MobileBudgetTracker(this)
        recovery = AutopilotRecoveryController(this, ledger)

        observer = NetworkObserver(this) { rawTruth ->
            val budget = mobileBudget.sample()
            val truth = rawTruth.copy(budgetState = budget.state)
            runCatching {
                ledger.append(truth)
                recovery.onTruth(truth, ledger.stabilityWindow().assessment.score)
            }
        }
        observer.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        if (::observer.isInitialized) observer.stop()
        if (::recovery.isInitialized) recovery.close()
        if (::ledger.isInitialized) ledger.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "KINLINK Autopilot", NotificationManager.IMPORTANCE_LOW)
        )
    }

    companion object {
        private const val CHANNEL_ID = "kinlink_observer"
        private const val NOTIFICATION_ID = 114
    }
}
