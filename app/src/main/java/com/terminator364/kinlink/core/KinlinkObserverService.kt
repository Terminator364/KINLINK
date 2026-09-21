package com.terminator364.kinlink.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.terminator364.kinlink.data.TelemetryLedger

class KinlinkObserverService : Service() {
    private lateinit var observer: NetworkObserver
    private lateinit var ledger: TelemetryLedger
    private lateinit var mobileBudget: MobileBudgetTracker
    private lateinit var recoveryModeStore: RecoveryModeStore
    private var recovery: AutopilotRecoveryController? = null
    private val handoffAudit = NetworkHandoffAudit()
    private val handoffOutcomeTracker = HandoffOutcomeTracker()
    private val recoveryEffectivenessTracker = RecoveryEffectivenessTracker()
    private val interruptionTracker = ConnectivityInterruptionTracker()
    private val degradedQualityEpisodeTracker = DegradedQualityEpisodeTracker()
    private val problemTransitionTracker = PassiveProblemTransitionTracker()
    private var latestTruth = NetworkTruth()
    private lateinit var postUpdateSelfTestStore: PostUpdateSelfTestStore
    private var runningVersionCode: Long = -1L
    private var lastNotificationText: String? = null
    private lateinit var runtimeBudgetSampler: RuntimeBudgetSampler
    private var runtimeBudgetStart: RuntimeBudgetSnapshot? = null
    private var coreRuntimeReady = true
    private var runtimeBudgetCheckpointWritten = false
    private var runtimeCallbackEvents = 0
    private var fieldQualificationReceiptWritten = false
    private var fieldQualificationBlockedWritten = false
    private val runtimeBudgetHandler by lazy { android.os.Handler(android.os.Looper.getMainLooper()) }
    private val runtimeBudgetCheckpointRunnable = Runnable { maybeRecordRuntimeBudgetCheckpoint() }
    private var powerReceiverRegistered = false
    private val powerStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
                resetRuntimeBudgetBaseline("POWER_DISCONNECTED")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        lastNotificationText = "Résilience Wi-Fi active · données mobiles protégées"
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
        recoveryModeStore = RecoveryModeStore(this)
        postUpdateSelfTestStore = PostUpdateSelfTestStore(this)
        runtimeBudgetSampler = RuntimeBudgetSampler(this)
        runtimeBudgetStart = runtimeBudgetSampler.sample()
        runningVersionCode = runCatching {
            packageManager.getPackageInfo(packageName, 0).longVersionCode
        }.getOrDefault(-1L)
        fieldQualificationReceiptWritten =
            ledger.countActions(QualificationReceiptNames.fieldQualified(runningVersionCode)) > 0
        fieldQualificationBlockedWritten =
            ledger.countActions(QualificationReceiptNames.fieldBlocked(runningVersionCode)) > 0
        scheduleRuntimeBudgetCheckpoint()
        ContextCompat.registerReceiver(
            this,
            powerStateReceiver,
            IntentFilter(Intent.ACTION_POWER_DISCONNECTED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        powerReceiverRegistered = true

        if (postUpdateSelfTestStore.needsCoreTest(runningVersionCode)) {
            val modeReadable = runCatching { recoveryModeStore.current() }.isSuccess
            val coreSelfTest = RuntimeSelfTestPolicy.core(
                databaseVersion = runCatching { ledger.schemaVersion() }.getOrDefault(-1),
                recoveryModeReadable = modeReadable,
                schemaIntegrity = runCatching { ledger.schemaIntegrityOk() }.getOrDefault(false)
            )
            coreRuntimeReady = coreSelfTest.pass
            runCatching {
                ledger.appendAction(
                    QualificationReceiptNames.coreSelfTest(runningVersionCode),
                    coreSelfTest.pass,
                    coreSelfTest.summary
                )
            }
            if (coreSelfTest.pass) {
                postUpdateSelfTestStore.markCoreTested(runningVersionCode)
            }
        }
        StartupReceiptStore(this).consume()?.let { startup ->
            runCatching {
                ledger.appendAction(
                    "STARTUP_${startup.source.name}",
                    startup.success,
                    startup.detail
                )
            }
        }
        val lifecycleDecision = LifecycleSafetyGuard(this).noteStart()
        runCatching {
            ledger.appendAction(
                "SERVICE_START",
                lifecycleDecision.recoveryAllowed,
                "starts10m=${lifecycleDecision.startsInWindow}; ${lifecycleDecision.reason}"
            )
        }
        if (lifecycleDecision.recoveryAllowed && coreRuntimeReady) {
            recovery = AutopilotRecoveryController(this, ledger) { baseline ->
                recoveryEffectivenessTracker.start(baseline)
            }
        } else if (!coreRuntimeReady) {
            runCatching {
                ledger.appendAction(
                    "RECOVERY_SUSPENDED_SELF_TEST",
                    false,
                    "Self-test core non validé : récupération active suspendue, observation maintenue."
                )
            }
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
            runtimeCallbackEvents += 1
            val budget = mobileBudget.sample()
            val truth = rawTruth.copy(budgetState = budget.state)
            latestTruth = truth
            runCatching {
                if (postUpdateSelfTestStore.needsObserverTest(runningVersionCode)) {
                    val observerSelfTest = RuntimeSelfTestPolicy.observerCallback(true)
                    ledger.appendAction(
                        QualificationReceiptNames.observerSelfTest(runningVersionCode),
                        observerSelfTest.pass,
                        observerSelfTest.summary
                    )
                    if (observerSelfTest.pass) {
                        postUpdateSelfTestStore.markObserverTested(runningVersionCode)
                    }
                }
                ledger.append(truth)
                maybeRecordRuntimeBudgetCheckpoint()
                handoffAudit.observe(truth.transport)?.let { transition ->
                    recovery?.onTransportTransition()
                    handoffOutcomeTracker.onTransition(transition)
                    ledger.appendAction(
                        QualificationReceiptNames.handoff(transition.kind, runningVersionCode),
                        true,
                        transition.summary + " Fenêtre calme 5 s avant toute récupération."
                    )
                }
                handoffOutcomeTracker.observe(truth)?.let { outcome ->
                    ledger.appendAction(
                        QualificationReceiptNames.handoffOutcome(outcome.outcome, runningVersionCode),
                        outcome.outcome != HandoffOutcome.MOBILE_PRESENT_UNVALIDATED,
                        outcome.summary
                    )
                }
                recoveryEffectivenessTracker.observe(truth)?.let { evidence ->
                    ledger.appendAction(
                        "RECOVERY_OUTCOME_${evidence.result.name}",
                        evidence.result == RecoveryEffectiveness.IMPROVED ||
                            evidence.result == RecoveryEffectiveness.UNCHANGED,
                        "${evidence.summary} baseline=${evidence.baseline.name}; current=${evidence.current.name}"
                    )
                }
                interruptionTracker.observe(truth)?.let { interruption ->
                    ledger.appendAction(
                        "INTERRUPTION_${interruption.severity.name}",
                        interruption.severity == InterruptionSeverity.MICRO,
                        "${interruption.summary} ${interruption.fromTransport.name}->${interruption.toTransport.name}",
                        durationMillis = interruption.durationMillis
                    )
                }
                degradedQualityEpisodeTracker.observe(truth)?.let { episode ->
                    ledger.appendAction(
                        "LOW_QUALITY_EPISODE_${episode.severity.name}",
                        true,
                        episode.summary,
                        durationMillis = episode.durationMillis
                    )
                }
                val stability = ledger.stabilityWindow()
                val passiveProblem = PassiveProblemClassifier.classify(
                    truth,
                    stability.assessment.score,
                    stability.assessment.flapping
                )
                problemTransitionTracker.observe(passiveProblem.cause)?.let { cause ->
                    ledger.appendAction(
                        "PASSIVE_CAUSE_${cause.name}",
                        true,
                        "${passiveProblem.summary} confidence=${passiveProblem.confidence}%"
                    )
                }
                updateNotificationFor(truth, passiveProblem)
                if (ActiveRecoveryPolicy.allowed(recoveryModeStore.current(), truth.transport)) {
                    recovery?.onTruth(truth, stability.assessment.score)
                }
                maybeRecordFieldCandidateQualification()
            }
        }
        observer.start()
    }

    private fun scheduleRuntimeBudgetCheckpoint() {
        runtimeBudgetHandler.removeCallbacks(runtimeBudgetCheckpointRunnable)
        runtimeBudgetHandler.postDelayed(
            runtimeBudgetCheckpointRunnable,
            RuntimeBudgetCheckpointPolicy.MIN_CHECKPOINT_AGE_MS + 1_000L
        )
    }

    private fun resetRuntimeBudgetBaseline(reason: String) {
        if (!::runtimeBudgetSampler.isInitialized) return
        runtimeBudgetStart = runtimeBudgetSampler.sample()
        runtimeCallbackEvents = 0
        runtimeBudgetCheckpointWritten = false
        scheduleRuntimeBudgetCheckpoint()
        if (::ledger.isInitialized) {
            runCatching {
                ledger.appendAction(
                    "RUNTIME_BUDGET_BASELINE_RESET_V$runningVersionCode",
                    true,
                    "reason=$reason; new 30-minute resource window started"
                )
            }
        }
    }
    private fun maybeRecordRuntimeBudgetCheckpoint() {
        if (!::runtimeBudgetSampler.isInitialized) return
        val start = runtimeBudgetStart ?: return
        if (!RuntimeBudgetCheckpointPolicy.shouldCheckpoint(
                start.elapsedMillis,
                android.os.SystemClock.elapsedRealtime(),
                runtimeBudgetCheckpointWritten
            )
        ) return

        val evidence = RuntimeBudgetPolicy.evidence(start, runtimeBudgetSampler.sample())
        val rate = evidence.batteryPercentPerHour?.let {
            String.format(java.util.Locale.US, "%.2f", it)
        } ?: "inconclusive"
        val resourceAssessment = RuntimeResourceQualificationPolicy.evaluate(
            RuntimeResourceEvidence(
                durationMillis = evidence.durationMillis,
                pssDeltaMiB = evidence.pssDeltaMiB,
                batteryPercentPerHour = evidence.batteryPercentPerHour,
                backgroundChurnEvents = runtimeCallbackEvents
            )
        )

        runCatching {
            ledger.appendAction(
                "RUNTIME_BUDGET_CHECKPOINT",
                true,
                "durationMs=${evidence.durationMillis}; pssStartMiB=${evidence.startPssMiB}; pssEndMiB=${evidence.endPssMiB}; pssDeltaMiB=${evidence.pssDeltaMiB}; batteryDelta=${evidence.batteryDeltaPercent ?: -1}; batteryPctPerHour=$rate; callbackEvents=$runtimeCallbackEvents"
            )
            ledger.appendAction(
                QualificationReceiptNames.resourceGate(resourceAssessment.verdict, runningVersionCode),
                resourceAssessment.verdict == RuntimeResourceVerdict.PASS,
                "reasons=${resourceAssessment.reasons.sorted().joinToString(",")}; callbackEvents=$runtimeCallbackEvents"
            )
        }
        runtimeBudgetCheckpointWritten = true
        maybeRecordFieldCandidateQualification()
    }

    private fun maybeRecordFieldCandidateQualification() {
        if (runningVersionCode < 8L || fieldQualificationReceiptWritten) return

        val assessment = FieldCandidateQualificationPolicy.evaluate(
            FieldCandidateQualificationEvidence(
                coreSelfTestPasses =
                    ledger.countSuccessfulActions(QualificationReceiptNames.coreSelfTest(runningVersionCode)),
                observerSelfTestPasses =
                    ledger.countSuccessfulActions(QualificationReceiptNames.observerSelfTest(runningVersionCode)),
                mobileValidatedHandoffs =
                    ledger.countSuccessfulActions(QualificationReceiptNames.handoffOutcome(HandoffOutcome.MOBILE_VALIDATED, runningVersionCode)),
                cellularToWifiReturns =
                    ledger.countSuccessfulActions(QualificationReceiptNames.handoff(HandoffKind.CELLULAR_TO_WIFI, runningVersionCode)),
                runtimeResourcePasses =
                    ledger.countSuccessfulActions(QualificationReceiptNames.resourceGate(RuntimeResourceVerdict.PASS, runningVersionCode)),
                runtimeResourceBlocks =
                    ledger.countActions(QualificationReceiptNames.resourceGate(RuntimeResourceVerdict.BLOCKED, runningVersionCode)),
                latestRuntimeResourcePassMillis =
                    ledger.latestActionTimestamp(
                        QualificationReceiptNames.resourceGate(RuntimeResourceVerdict.PASS, runningVersionCode)
                    ),
                latestRuntimeResourceBlockMillis =
                    ledger.latestActionTimestamp(
                        QualificationReceiptNames.resourceGate(RuntimeResourceVerdict.BLOCKED, runningVersionCode)
                    )
            )
        )

        when (assessment.verdict) {
            FieldCandidateQualificationVerdict.PASS -> {
                ledger.appendAction(
                    QualificationReceiptNames.fieldQualified(runningVersionCode),
                    true,
                    "0.7 field evidence complete: self-tests, validated mobile handoff, Wi-Fi return and runtime resource PASS."
                )
                fieldQualificationReceiptWritten = true
                updateNotificationFor(latestTruth)
            }
            FieldCandidateQualificationVerdict.BLOCKED -> {
                if (!fieldQualificationBlockedWritten) {
                    ledger.appendAction(
                        QualificationReceiptNames.fieldBlocked(runningVersionCode),
                        false,
                        "0.7 field qualification blocked: ${assessment.missing.sorted().joinToString(",")}."
                    )
                    fieldQualificationBlockedWritten = true
                    updateNotificationFor(latestTruth)
                }
            }
            FieldCandidateQualificationVerdict.PENDING -> Unit
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_REFRESH_MODE && ::recoveryModeStore.isInitialized) {
            updateNotificationFor(
                latestTruth,
                PassiveProblemClassifier.classify(latestTruth)
            )
        }
        return START_STICKY
    }

    override fun onDestroy() {
        runtimeBudgetHandler.removeCallbacks(runtimeBudgetCheckpointRunnable)
        if (powerReceiverRegistered) {
            runCatching { unregisterReceiver(powerStateReceiver) }
            powerReceiverRegistered = false
        }
        if (::ledger.isInitialized) {
            maybeRecordRuntimeBudgetCheckpoint()
        }
        if (::observer.isInitialized) observer.stop()
        recovery?.close()
        if (::ledger.isInitialized) {
            if (::runtimeBudgetSampler.isInitialized) {
                runtimeBudgetStart?.let { start ->
                    val evidence = RuntimeBudgetPolicy.evidence(start, runtimeBudgetSampler.sample())
                    val rate = evidence.batteryPercentPerHour?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "insufficient-session"
                    runCatching {
                        ledger.appendAction(
                            "RUNTIME_BUDGET_SESSION",
                            true,
                            "durationMs=${evidence.durationMillis}; pssStartMiB=${evidence.startPssMiB}; pssEndMiB=${evidence.endPssMiB}; pssDeltaMiB=${evidence.pssDeltaMiB}; batteryDelta=${evidence.batteryDeltaPercent ?: -1}; batteryPctPerHour=$rate"
                        )
                    }
                }
            }
            runCatching { ledger.appendAction("SERVICE_STOP", true, "Service arrêté proprement.") }
            ledger.close()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateNotificationFor(
        truth: NetworkTruth,
        passiveProblem: PassiveProblemAssessment = PassiveProblemClassifier.classify(truth)
    ) {
        val observationOnly = recoveryModeStore.current() == RecoveryMode.OBSERVATION_ONLY
        val text = if (fieldQualificationReceiptWritten) {
            "KINLINK 0.7 · qualification terrain complète"
        } else if (fieldQualificationBlockedWritten) {
            "KINLINK 0.7 · qualification bloquée · voir diagnostic"
        } else if (observationOnly) {
            "Mode sûr · observation uniquement · Android garde le contrôle"
        } else when (truth.transport) {
            Transport.CELLULAR -> "Données mobiles · Android contrôle · KINLINK observe seulement"
            Transport.WIFI -> when (passiveProblem.cause) {
                PassiveProblemCause.LOW_CAPACITY -> "Wi-Fi connecté mais capacité limitée · surveillance passive"
                PassiveProblemCause.CAPTIVE_PORTAL -> "Wi-Fi · connexion au portail requise"
                PassiveProblemCause.ADDRESSING_SUSPECT -> "Wi-Fi · adresse IP à surveiller"
                PassiveProblemCause.ROUTE_CONFIGURATION_SUSPECT -> "Wi-Fi · route réseau à surveiller"
                PassiveProblemCause.DNS_CONFIGURATION_SUSPECT -> "Wi-Fi · configuration DNS à surveiller"
                PassiveProblemCause.WAN_UNVALIDATED -> "Wi-Fi local présent · Internet non confirmé"
                PassiveProblemCause.FLAPPING -> "Wi-Fi instable · KINLINK limite les actions"
                else -> "Résilience Wi-Fi active · données mobiles hors contrôle KINLINK"
            }
            Transport.ETHERNET -> "Ethernet · observation seulement"
            Transport.VPN -> "VPN détecté · observation seulement"
            Transport.NONE -> "Aucun réseau · observation passive"
            Transport.UNKNOWN -> "Réseau en transition · observation passive"
        }
        if (!NotificationUpdatePolicy.shouldPublish(lastNotificationText, text)) return
        lastNotificationText = text
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("KINLINK Autopilot")
            .setContentText(text)
            .setOngoing(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "KINLINK Autopilot", NotificationManager.IMPORTANCE_LOW)
        )
    }

    companion object {
        private const val CHANNEL_ID = "kinlink_observer"
        private const val NOTIFICATION_ID = 114
        const val ACTION_REFRESH_MODE = "com.terminator364.kinlink.REFRESH_RECOVERY_MODE"
    }
}
