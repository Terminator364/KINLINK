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
    private var recovery: AutopilotRecoveryController? = null
    private val handoffAudit = NetworkHandoffAudit()

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
        val lifecycleDecision = LifecycleSafetyGuard(this).noteStart()
        runCatching {
            ledger.appendAction(
                "SERVICE_START",
                lifecycleDecision.recoveryAllowed,
                "starts10m=${lifecycleDecision.startsInWindow}; ${lifecycleDecision.reason}"
            )
        }
        if (lifecycleDecision.recoveryAllowed) {
            recovery = AutopilotRecoveryController(this, ledger)
        } else {
            runCatching {
                ledger.appendAction(
                    "RECOVERY_SUSPENDED_RESTART_STORM",
                    false,
                    lifecycleDecision.reason
                )
            }
        }

        observer = NetworkObserver(this) { rawTruth ->
            val budget = mobileBudget.sample()
            val truth = rawTruth.copy(budgetState = budget.state)
            runCatching {
                ledger.append(truth)
                handoffAudit.observe(truth.transport)?.let { transition ->
                    ledger.appendAction(
                        "HANDOFF_${transition.kind.name}",
                        true,
                        transition.summary
                    )
                }
                recovery?.onTruth(truth, ledger.stabilityWindow().assessment.score)
            }
        }
        observer.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        if (::observer.isInitialized) observer.stop()
        recovery?.close()
        if (::ledger.isInitialized) {
            runCatching { ledger.appendAction("SERVICE_STOP", true, "Service arrêté proprement.") }
            ledger.close()
        }
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
