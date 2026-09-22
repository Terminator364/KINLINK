package com.terminator364.kinlink.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.content.pm.PackageManager
import android.text.InputType
import android.view.View
import android.view.WindowInsets
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.terminator364.kinlink.R
import com.terminator364.kinlink.core.ActiveRecoveryPolicy
import com.terminator364.kinlink.core.AdaptivePolicyEngine
import com.terminator364.kinlink.core.AutopilotProfile
import com.terminator364.kinlink.core.AutopilotProfileStore
import com.terminator364.kinlink.core.AutopilotProfileControlPolicy
import com.terminator364.kinlink.core.BudgetState
import com.terminator364.kinlink.core.ConnectivityStateClassifier
import com.terminator364.kinlink.core.ConnectivityTruthEngine
import com.terminator364.kinlink.core.ContinuousControlPanelPolicy
import com.terminator364.kinlink.core.CockpitPrimaryAction
import com.terminator364.kinlink.core.CockpitPrimaryActionPolicy
import com.terminator364.kinlink.core.DeviceResourceGuard
import com.terminator364.kinlink.core.KinlinkObserverService
import com.terminator364.kinlink.core.MobileBudgetSnapshot
import com.terminator364.kinlink.core.MobileBudgetTracker
import com.terminator364.kinlink.core.MobilePlanConfig
import com.terminator364.kinlink.core.MobilePlanUsageSource
import com.terminator364.kinlink.core.MobilePlanVaultPolicy
import com.terminator364.kinlink.core.MobilePlanVaultStore
import com.terminator364.kinlink.core.MobileUsageAttributionScope
import com.terminator364.kinlink.core.MobileUsageEvidenceStore
import com.terminator364.kinlink.core.MobileUsageObservation
import com.terminator364.kinlink.core.MobileUsageReconciliationPolicy
import com.terminator364.kinlink.core.MobileUsageResolutionStatus
import com.terminator364.kinlink.core.NetworkStatsMobileEvidenceStatus
import com.terminator364.kinlink.core.NetworkStatsMobileUsageReader
import com.terminator364.kinlink.core.NetworkStatsMobileUsageRequest
import com.terminator364.kinlink.core.NetworkStatsQueryGateStatus
import com.terminator364.kinlink.core.NetworkStatsQuerySessionGate
import com.terminator364.kinlink.core.MobileVaultAssessment
import com.terminator364.kinlink.core.MobileVaultFormPolicy
import com.terminator364.kinlink.core.MobileVaultZone
import com.terminator364.kinlink.core.MobileAssistController
import com.terminator364.kinlink.core.MobileAssistEvidenceSummaryPolicy
import com.terminator364.kinlink.core.MobileAssistManualPolicy
import com.terminator364.kinlink.core.MobileAssistManualAction
import com.terminator364.kinlink.core.MobileAssistManualBlockReason
import com.terminator364.kinlink.core.MobileAssistPolicy
import com.terminator364.kinlink.core.MobileVault
import com.terminator364.kinlink.core.NetworkObserver
import com.terminator364.kinlink.core.PassiveLinkQuality
import com.terminator364.kinlink.core.PassiveLinkQualityPolicy
import com.terminator364.kinlink.core.PassiveQualityScorePolicy
import com.terminator364.kinlink.core.PassiveProblemClassifier
import com.terminator364.kinlink.core.PassiveGuidancePolicy
import com.terminator364.kinlink.core.RecoveryBlockReason
import com.terminator364.kinlink.core.RecoveryMode
import com.terminator364.kinlink.core.RecoveryModeStore
import com.terminator364.kinlink.core.ResourceGuardSnapshot
import com.terminator364.kinlink.core.ReliabilitySummaryPolicy
import com.terminator364.kinlink.core.RecentReliabilityPolicy
import com.terminator364.kinlink.core.ProfileRecommendationPolicy
import com.terminator364.kinlink.core.QualificationReceiptNames
import com.terminator364.kinlink.core.FieldCandidateQualificationEvidence
import com.terminator364.kinlink.core.FieldCandidateQualificationPolicy
import com.terminator364.kinlink.core.FieldCandidateQualificationVerdict
import com.terminator364.kinlink.core.HandoffKind
import com.terminator364.kinlink.core.HandoffOutcome
import com.terminator364.kinlink.core.RuntimeResourceVerdict
import com.terminator364.kinlink.core.SessionHealthPolicy
import com.terminator364.kinlink.core.UserExperienceTruthPolicy
import com.terminator364.kinlink.core.UserExperiencePresentationPolicy
import com.terminator364.kinlink.core.PlatformCapabilityDiscovery
import com.terminator364.kinlink.core.LocalNetworkAccessState
import com.terminator364.kinlink.core.DataSaverState
import com.terminator364.kinlink.core.NetworkTruth
import com.terminator364.kinlink.core.NotificationPermissionPolicy
import com.terminator364.kinlink.core.TrustedWifiContextPolicy
import com.terminator364.kinlink.core.TrustedWifiContextStore
import com.terminator364.kinlink.core.WifiDoctor
import com.terminator364.kinlink.core.WifiOptimizer
import com.terminator364.kinlink.data.DiagnosticExporter
import com.terminator364.kinlink.data.StabilityWindow
import com.terminator364.kinlink.data.RecentReliabilityWindow
import com.terminator364.kinlink.data.TelemetryLedger
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {
    private lateinit var observer: NetworkObserver
    private lateinit var ledger: TelemetryLedger
    private lateinit var mobileBudget: MobileBudgetTracker
    private lateinit var mobilePlanVaultStore: MobilePlanVaultStore
    private lateinit var mobileUsageEvidenceStore: MobileUsageEvidenceStore
    private lateinit var profileStore: AutopilotProfileStore
    private lateinit var recoveryModeStore: RecoveryModeStore

    private lateinit var heroEyebrow: TextView
    private lateinit var stateText: TextView
    private lateinit var transportText: TextView
    private lateinit var internetText: TextView
    private lateinit var mobileText: TextView
    private lateinit var mobileBudgetText: TextView
    private lateinit var qualityScoreText: TextView
    private lateinit var qualityProgress: ProgressBar
    private lateinit var reliabilityText: TextView
    private lateinit var detailText: TextView
    private lateinit var heroDetailText: TextView
    private lateinit var adviceTitleText: TextView
    private lateinit var adviceText: TextView
    private lateinit var beforeScoreText: TextView
    private lateinit var nowScoreText: TextView
    private lateinit var deltaScoreText: TextView
    private lateinit var maintainedText: TextView
    private lateinit var evidenceText: TextView
    private lateinit var incidentMarkerButton: TextView
    private lateinit var technicalToggle: TextView
    private lateinit var diagnosticExport: TextView
    private lateinit var wifiDoctorButton: TextView
    private lateinit var mobileAssistButton: TextView
    private lateinit var budgetButton: TextView
    private lateinit var modeConservativeButton: TextView
    private lateinit var modeBalancedButton: TextView
    private lateinit var modeMaxButton: TextView
    private lateinit var safeModeButton: TextView
    private lateinit var diagnosticSurfaceButton: TextView
    private lateinit var weekSurfaceButton: TextView
    private lateinit var mobileVaultSurfaceButton: TextView
    private lateinit var settingsSurfaceButton: TextView
    private lateinit var versionText: TextView

    private var latestTruth = NetworkTruth()
    private var currentProfile = AutopilotProfile.BALANCED
    private var latestBudget = MobileBudgetSnapshot(
        supported = false,
        usedTodayBytes = 0L,
        dailyLimitBytes = null,
        state = BudgetState.BALANCE_UNKNOWN
    )
    private var latestStability: StabilityWindow? = null
    private var latestReliability: RecentReliabilityWindow? = null
    private var latestUserIncidentTsWallMs: Long? = null
    private var installedVersionName: String = "?"
    private var installedVersionCode: Long = -1L
    private var lastQualificationUiRefreshElapsed: Long = 0L
    private var lastAssistEvidenceRefreshElapsed: Long = 0L
    private var cachedAssistEvidenceLabel: String =
        "Preuve 24 h · aucune action Mobile Assist évaluée"
    private val networkStatsSessionGate = NetworkStatsQuerySessionGate()
    private val networkStatsWorker = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "KINLINK-NetworkStats").apply { isDaemon = true }
    }
    private val networkStatsDeadlineWorker =
        Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "KINLINK-NetworkStats-Deadline").apply {
                isDaemon = true
            }
        }
    @Volatile
    private var networkStatsFuture: Future<*>? = null

    private var evidenceReceiverRegistered = false
    private val evidenceUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (
                intent?.action ==
                    KinlinkObserverService.ACTION_MOBILE_ASSIST_EVIDENCE_UPDATED
            ) {
                lastAssistEvidenceRefreshElapsed = 0L
                render(latestTruth)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextCompat.startForegroundService(this, Intent(this, KinlinkObserverService::class.java))
        setContentView(R.layout.activity_main)
        ensureNotificationVisibilityPermission()

        heroEyebrow = findViewById(R.id.heroEyebrow)
        stateText = findViewById(R.id.stateText)
        transportText = findViewById(R.id.transportText)
        internetText = findViewById(R.id.internetText)
        mobileText = findViewById(R.id.mobileText)
        mobileBudgetText = findViewById(R.id.mobileBudgetText)
        qualityScoreText = findViewById(R.id.qualityScoreText)
        qualityProgress = findViewById(R.id.qualityProgress)
        reliabilityText = findViewById(R.id.reliabilityText)
        detailText = findViewById(R.id.detailText)
        heroDetailText = findViewById(R.id.heroDetailText)
        adviceTitleText = findViewById(R.id.adviceTitleText)
        adviceText = findViewById(R.id.adviceText)
        beforeScoreText = findViewById(R.id.beforeScoreText)
        nowScoreText = findViewById(R.id.nowScoreText)
        deltaScoreText = findViewById(R.id.deltaScoreText)
        maintainedText = findViewById(R.id.maintainedText)
        evidenceText = findViewById(R.id.evidenceText)
        incidentMarkerButton = findViewById(R.id.incidentMarkerButton)
        technicalToggle = findViewById(R.id.technicalToggle)
        diagnosticExport = findViewById(R.id.diagnosticExport)
        wifiDoctorButton = findViewById(R.id.wifiDoctorButton)
        mobileAssistButton = findViewById(R.id.mobileAssistButton)
        budgetButton = findViewById(R.id.budgetButton)
        modeConservativeButton = findViewById(R.id.modeConservativeButton)
        modeBalancedButton = findViewById(R.id.modeBalancedButton)
        modeMaxButton = findViewById(R.id.modeMaxButton)
        safeModeButton = findViewById(R.id.safeModeButton)
        diagnosticSurfaceButton = findViewById(R.id.diagnosticSurfaceButton)
        weekSurfaceButton = findViewById(R.id.weekSurfaceButton)
        mobileVaultSurfaceButton = findViewById(R.id.mobileVaultSurfaceButton)
        settingsSurfaceButton = findViewById(R.id.settingsSurfaceButton)
        versionText = findViewById(R.id.versionText)
        runCatching { packageManager.getPackageInfo(packageName, 0) }.getOrNull()?.let { info ->
            installedVersionName = info.versionName ?: "?"
            installedVersionCode = info.longVersionCode
        }

        installSystemBarInsets()
        ContextCompat.registerReceiver(
            this,
            evidenceUpdatedReceiver,
            IntentFilter(
                KinlinkObserverService.ACTION_MOBILE_ASSIST_EVIDENCE_UPDATED
            ),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        evidenceReceiverRegistered = true

        ledger = TelemetryLedger(this)
        latestUserIncidentTsWallMs = runCatching {
            ledger.latestActionReceipt("USER_INCIDENT_MARKER")?.tsWallMs
        }.getOrNull()
        mobileBudget = MobileBudgetTracker(this)
        mobilePlanVaultStore = MobilePlanVaultStore(this)
        mobileUsageEvidenceStore = MobileUsageEvidenceStore(this)
        profileStore = AutopilotProfileStore(this)
        recoveryModeStore = RecoveryModeStore(this)
        refreshFieldQualificationLabel(force = true)
        currentProfile = profileStore.current()
        refreshModeButtons()
        refreshSafeModeButton()
        refreshOptionalNetworkStatsEvidence()

        incidentMarkerButton.setOnClickListener { recordUserIncidentMarker() }
        technicalToggle.setOnClickListener { toggleTechnicalDetails() }
        diagnosticExport.setOnClickListener { exportDiagnostic() }
        wifiDoctorButton.setOnClickListener { optimizeWifi() }
        mobileAssistButton.setOnClickListener { optimizeMobile() }
        budgetButton.setOnClickListener { configureMobileBudget() }
        diagnosticSurfaceButton.setOnClickListener {
            openProductSurface(ProductSurfaceActivity.SURFACE_DIAGNOSTIC)
        }
        weekSurfaceButton.setOnClickListener {
            openProductSurface(ProductSurfaceActivity.SURFACE_WEEK)
        }
        mobileVaultSurfaceButton.setOnClickListener {
            openProductSurface(ProductSurfaceActivity.SURFACE_MOBILE_VAULT)
        }
        settingsSurfaceButton.setOnClickListener {
            openProductSurface(ProductSurfaceActivity.SURFACE_SETTINGS)
        }
        modeConservativeButton.setOnClickListener {
            selectProfile(AutopilotProfile.CONSERVATIVE)
        }
        modeBalancedButton.setOnClickListener {
            selectProfile(AutopilotProfile.BALANCED)
        }
        modeMaxButton.setOnClickListener {
            selectProfile(AutopilotProfile.MAXIMUM_STABILITY)
        }
        safeModeButton.setOnClickListener {
            val mode = recoveryModeStore.toggle()
            runCatching {
                ledger.appendAction(
                    "RECOVERY_MODE_${mode.name}",
                    true,
                    if (mode == RecoveryMode.OBSERVATION_ONLY)
                        "Mode sûr activé par l’utilisateur : récupération active suspendue."
                    else
                        "Autopilot actif réactivé par l’utilisateur."
                )
            }
            refreshSafeModeButton()
            ContextCompat.startForegroundService(
                this,
                Intent(this, KinlinkObserverService::class.java).apply {
                    action = KinlinkObserverService.ACTION_REFRESH_MODE
                }
            )
            render(latestTruth, latestBudget, latestStability)
        }

        observer = NetworkObserver(this) { rawTruth ->
            val budget = mobileBudget.sample()
            val enrichedTruth = rawTruth.copy(
                budgetState = effectiveBudgetState(budget)
            )
            val stability = runCatching { ledger.stabilityWindow() }.getOrNull()
            val reliability = runCatching { ledger.recentReliabilityWindow() }.getOrNull()

            runOnUiThread {
                latestBudget = budget
                latestStability = stability
                latestReliability = reliability
                render(enrichedTruth, budget, stability)
            }
        }
        handleLaunchIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        observer.start()
    }

    override fun onStop() {
        observer.stop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (::profileStore.isInitialized) {
            currentProfile = profileStore.current()
            refreshModeButtons()
        }
        if (::recoveryModeStore.isInitialized) {
            refreshSafeModeButton()
        }
        if (::ledger.isInitialized) {
            render(latestTruth, latestBudget, latestStability)
        }
    }

    override fun onDestroy() {
        if (evidenceReceiverRegistered) {
            runCatching { unregisterReceiver(evidenceUpdatedReceiver) }
            evidenceReceiverRegistered = false
        }
        networkStatsSessionGate.disableForSession()
        networkStatsFuture?.cancel(true)
        networkStatsDeadlineWorker.shutdownNow()
        networkStatsWorker.shutdownNow()
        ledger.close()
        super.onDestroy()
    }

    private fun handleLaunchIntent(launchIntent: Intent?) {
        when (launchIntent?.getStringExtra(EXTRA_PRODUCT_ACTION)) {
            ACTION_OPEN_MOBILE_VAULT -> {
                launchIntent.removeExtra(EXTRA_PRODUCT_ACTION)
                configureMobileBudget()
            }
            ACTION_EXPORT_DIAGNOSTIC -> {
                launchIntent.removeExtra(EXTRA_PRODUCT_ACTION)
                exportDiagnostic()
            }
        }
    }

    private fun openProductSurface(surface: String) {
        val truth = latestTruth
        val wifiDigest = TrustedWifiContextPolicy.fingerprint(truth)
        val trustedWifi = TrustedWifiContextStore(this)
        val trustedLabel = when {
            wifiDigest == null ->
                "Wi-Fi de confiance · identité stable indisponible sans permission supplémentaire"
            trustedWifi.isHome(wifiDigest) ->
                "Wi-Fi maison reconnu localement"
            trustedWifi.isTrusted(wifiDigest) ->
                "Wi-Fi de confiance reconnu localement"
            else ->
                "Wi-Fi actuel non mémorisé"
        }
        startActivity(
            Intent(this, ProductSurfaceActivity::class.java).apply {
                putExtra(ProductSurfaceActivity.EXTRA_SURFACE, surface)
                putExtra(ProductSurfaceActivity.EXTRA_WIFI_DIGEST, wifiDigest)
                putExtra(ProductSurfaceActivity.EXTRA_WIFI_LABEL, trustedLabel)
                if (surface == ProductSurfaceActivity.SURFACE_DIAGNOSTIC) {
                    putExtra(
                        ProductSurfaceActivity.EXTRA_DIAGNOSTIC_BODY,
                        buildProductDiagnosticSummary(truth)
                    )
                }
            }
        )
    }

    private fun buildProductDiagnosticSummary(truth: NetworkTruth): String =
        buildString {
            append("1. Téléphone · KINLINK observe localement\n")
            append("2. Radio / transport · ${transportLabel(truth)}\n")
            append("3. LAN / route · ${lanLabel(truth)} · ")
            append(
                if (truth.hasIpv4DefaultRoute || truth.hasIpv6DefaultRoute)
                    "route par défaut présente"
                else
                    "route par défaut non confirmée"
            )
            append("\n4. DNS / IP · ${truth.dnsServerCount} DNS · ")
            append("IPv4=${if (truth.hasIpv4Address) "oui" else "non"} · ")
            append("IPv6=${if (truth.hasIpv6Address) "oui" else "non"}\n")
            append("5. Internet · ${internetLabel(truth)}\n")
            append("6. Service distant · non testé activement par défaut\n\n")
            append("Cause la plus probable · ${failureLabel(truth)}\n")
            append("Réseau payant · ${if (truth.metered) "oui" else "non"}\n")
            append("Confiance technique Android · ")
            append(String.format(Locale.US, "%.0f%%", truth.confidence * 100.0))
            append("\n")
            append(
                TrustedWifiContextStore(this@MainActivity).let { store ->
                    val digest = TrustedWifiContextPolicy.fingerprint(truth)
                    when {
                        digest == null -> "Contexte Wi-Fi · non mémorisable sans identité stable"
                        store.isHome(digest) -> "Contexte Wi-Fi · maison"
                        store.isTrusted(digest) -> "Contexte Wi-Fi · de confiance"
                        else -> "Contexte Wi-Fi · non mémorisé"
                    }
                }
            )
        }

    private fun ensureNotificationVisibilityPermission() {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        if (NotificationPermissionPolicy.shouldRequest(Build.VERSION.SDK_INT, granted)) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_POST_NOTIFICATIONS
            )
        }
    }

    private fun selectProfile(profile: AutopilotProfile) {
        currentProfile = profile
        profileStore.set(profile)
        refreshModeButtons()
        runCatching {
            ledger.appendAction(
                "AUTOPILOT_PROFILE_${profile.name}",
                true,
                "Mode Autopilot sélectionné : ${profileLabel(profile)}"
            )
        }
        render(latestTruth, latestBudget, latestStability)
    }

    private fun refreshModeButtons() {
        val selectedColor = android.graphics.Color.WHITE
        val normalColor = android.graphics.Color.parseColor("#0B57D0")

        fun style(button: TextView, selected: Boolean) {
            button.setBackgroundResource(
                if (selected) R.drawable.button_selected
                else R.drawable.button_outline
            )
            button.setTextColor(if (selected) selectedColor else normalColor)
        }

        style(
            modeConservativeButton,
            currentProfile == AutopilotProfile.CONSERVATIVE
        )
        style(
            modeBalancedButton,
            currentProfile == AutopilotProfile.BALANCED
        )
        style(
            modeMaxButton,
            currentProfile == AutopilotProfile.MAXIMUM_STABILITY
        )
    }

    private fun refreshSafeModeButton() {
        val observationOnly = recoveryModeStore.current() == RecoveryMode.OBSERVATION_ONLY
        safeModeButton.text = if (observationOnly) {
            "Mode sûr · ON"
        } else {
            "Mode sûr · OFF"
        }
    }

    private fun profileLabel(profile: AutopilotProfile): String = when (profile) {
        AutopilotProfile.CONSERVATIVE -> "Prudent"
        AutopilotProfile.BALANCED -> "Stable"
        AutopilotProfile.MAXIMUM_STABILITY -> "Max"
    }

    private fun recordUserIncidentMarker() {
        val stability = latestStability
        val passiveQuality = PassiveLinkQualityPolicy.assess(latestTruth)
        val passiveProblem = PassiveProblemClassifier.classify(
            latestTruth,
            instabilityScore = stability?.assessment?.score ?: 0,
            flapping = stability?.assessment?.flapping ?: false
        )
        val sessionHealth = SessionHealthPolicy.assess(
            latestTruth,
            instabilityScore = stability?.assessment?.score ?: 0,
            flapping = stability?.assessment?.flapping ?: false,
            passiveProblem = passiveProblem
        )
        val summary = buildString {
            append("transport=${latestTruth.transport.name}; ")
            append("internet=${latestTruth.internetState.name}; ")
            append("quality=${passiveQuality.quality.name}; ")
            append("cause=${passiveProblem.cause.name}; ")
            append("causeConfidence=${passiveProblem.confidence}; ")
            append("sessionHealth=${sessionHealth.health.name}; ")
            append("dnsServers=${latestTruth.dnsServerCount}; ")
            append("privateDns=${latestTruth.privateDnsActive}; ")
            append("ipv4=${latestTruth.hasIpv4Address}; ipv6=${latestTruth.hasIpv6Address}; ")
            append("route4=${latestTruth.hasIpv4DefaultRoute}; route6=${latestTruth.hasIpv6DefaultRoute}; ")
            append("instability=${stability?.assessment?.score ?: 0}; ")
            append("recoveryMode=${recoveryModeStore.current().name}")
        }
        runCatching {
            ledger.appendAction(
                "USER_INCIDENT_MARKER",
                true,
                summary
            )
        }.onSuccess {
            latestUserIncidentTsWallMs = System.currentTimeMillis()
            render(latestTruth, latestBudget, latestStability)
            Toast.makeText(
                this,
                "Problème pris en compte : ton ressenti prime sur les estimations Android.",
                Toast.LENGTH_LONG
            ).show()
        }.onFailure {
            Toast.makeText(this, "Impossible d’enregistrer l’état", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleTechnicalDetails() {
        val nowVisible = detailText.visibility == View.VISIBLE
        detailText.visibility = if (nowVisible) View.GONE else View.VISIBLE
        diagnosticExport.visibility = if (nowVisible) View.GONE else View.VISIBLE
        technicalToggle.text =
            if (nowVisible) "Détails techniques" else "Masquer les détails"
    }

    private fun configureMobileBudget() {
        val current = mobilePlanVaultStore.read()?.config
        val evidence = mobileUsageEvidenceStore.read()
        val usageReader = NetworkStatsMobileUsageReader(this)

        fun field(hint: String, value: String = "") = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            this.hint = hint
            if (value.isNotBlank()) setText(value)
            minHeight = 48
        }

        val totalInput = field(
            "Forfait total · 5 GB",
            MobileVaultFormPolicy.formatEditable(current?.totalBytes)
        )
        val usedInput = field(
            "Consommé · optionnel",
            MobileVaultFormPolicy.formatEditable(
                evidence?.userReconciledUsedBytes
            )
        )
        val reserveInput = field(
            "Réserve protégée · 500 MB",
            MobileVaultFormPolicy.formatEditable(
                current?.protectedReserveBytes
            )
        )
        val rescueInput = field(
            "Secours · 200 MB",
            MobileVaultFormPolicy.formatEditable(
                current?.rescueAllowanceBytes
            )
        )
        val criticalInput = field(
            "Critique · 100 MB",
            MobileVaultFormPolicy.formatEditable(
                current?.criticalInteractiveAllowanceBytes
            )
        )
        val cycleStartInput = field(
            "Début cycle · AAAA-MM-JJ",
            MobileVaultFormPolicy.formatCycleStartDate(
                current?.cycleStartAtEpochMillis
            )
        )
        val expiryInput = field(
            "Expiration · AAAA-MM-JJ",
            MobileVaultFormPolicy.formatInclusiveExpiryDate(
                current?.expiryAtEpochMillis
            )
        )

        val usageAccessAction = TextView(this).apply {
            val granted = usageReader.hasUsageAccess()
            text = if (granted) {
                "Accès d’utilisation Android · accordé · optionnel"
            } else {
                "Accès d’utilisation Android · non accordé · toucher pour les réglages"
            }
            gravity = android.view.Gravity.CENTER
            minHeight = (48 * resources.displayMetrics.density).toInt()
            setPadding(0, 8, 0, 8)
        }
        val saveAction = TextView(this).apply {
            text = "Enregistrer"
            gravity = android.view.Gravity.CENTER
            minHeight = (48 * resources.displayMetrics.density).toInt()
            setPadding(0, 8, 0, 8)
        }
        val cancelAction = TextView(this).apply {
            text = "Annuler"
            gravity = android.view.Gravity.CENTER
            minHeight = (48 * resources.displayMetrics.density).toInt()
            setPadding(0, 8, 0, 8)
        }
        val clearAction = TextView(this).apply {
            text = "Effacer le forfait"
            gravity = android.view.Gravity.CENTER
            minHeight = (48 * resources.displayMetrics.density).toInt()
            setPadding(0, 8, 0, 8)
        }
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad / 2, pad, pad / 2)
            addView(totalInput)
            addView(usedInput)
            addView(reserveInput)
            addView(rescueInput)
            addView(criticalInput)
            addView(cycleStartInput)
            addView(expiryInput)
            addView(usageAccessAction)
            addView(saveAction)
            addView(cancelAction)
            addView(clearAction)
        }
        val scroll = ScrollView(this).apply { addView(form) }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Mobile Vault · forfait")
            .setMessage(
                "MB/GB · réserves protégées · données locales. " +
                    "NetworkStats Android reste agrégé et optionnel."
            )
            .setView(scroll)
            .create()

        dialog.setOnShowListener {
            saveAction.setOnClickListener {
                val total = MobileVaultFormPolicy.parseDecimalBytes(
                    totalInput.text.toString()
                )
                val usedText = usedInput.text.toString().trim()
                val used = if (usedText.isBlank()) null
                    else MobileVaultFormPolicy.parseDecimalBytes(usedText)
                val reserve = MobileVaultFormPolicy.parseDecimalBytes(
                    reserveInput.text.toString()
                ) ?: 0L
                val rescue = MobileVaultFormPolicy.parseDecimalBytes(
                    rescueInput.text.toString()
                ) ?: 0L
                val critical = MobileVaultFormPolicy.parseDecimalBytes(
                    criticalInput.text.toString()
                ) ?: 0L
                val cycleStartText = cycleStartInput.text.toString().trim()
                val cycleStart = if (cycleStartText.isBlank()) null
                    else MobileVaultFormPolicy.parseCycleStartDate(
                        cycleStartText
                    )
                val expiryText = expiryInput.text.toString().trim()
                val expiry = if (expiryText.isBlank()) null
                    else MobileVaultFormPolicy.parseInclusiveExpiryDate(
                        expiryText
                    )

                if (total == null || total <= 0L) {
                    totalInput.error = "Indique la taille du forfait en MB ou GB."
                    return@setOnClickListener
                }
                if (usedText.isNotBlank() && used == null) {
                    usedInput.error = "Format attendu : 500 MB ou 1.5 GB."
                    return@setOnClickListener
                }
                if (cycleStartText.isNotBlank() && cycleStart == null) {
                    cycleStartInput.error = "Date attendue : AAAA-MM-JJ."
                    return@setOnClickListener
                }
                if (cycleStart != null && cycleStart > System.currentTimeMillis()) {
                    cycleStartInput.error = "Le début du cycle ne peut pas être dans le futur."
                    return@setOnClickListener
                }
                if (expiryText.isNotBlank() && expiry == null) {
                    expiryInput.error = "Date attendue : AAAA-MM-JJ."
                    return@setOnClickListener
                }
                if (cycleStart != null && expiry != null && cycleStart >= expiry) {
                    cycleStartInput.error =
                        "Le début du cycle doit précéder l’expiration."
                    return@setOnClickListener
                }

                val config = MobilePlanConfig(
                    totalBytes = total,
                    expiryAtEpochMillis = expiry,
                    protectedReserveBytes = reserve,
                    rescueAllowanceBytes = rescue,
                    criticalInteractiveAllowanceBytes = critical,
                    cycleStartAtEpochMillis = cycleStart
                )
                if (!MobilePlanVaultPolicy.valid(config)) {
                    reserveInput.error =
                        "La somme des réserves doit rester inférieure au forfait."
                    return@setOnClickListener
                }
                val cycleChanged =
                    current?.cycleStartAtEpochMillis != cycleStart
                val saved = mobilePlanVaultStore.write(config)
                val usageSaved = if (used == null) {
                    mobileUsageEvidenceStore.clearUserReconciled()
                } else {
                    mobileUsageEvidenceStore.writeUserReconciled(used)
                }
                if (!saved || !usageSaved) {
                    Toast.makeText(
                        this,
                        "Impossible d’enregistrer le Mobile Vault",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (cycleChanged) {
                    mobileUsageEvidenceStore.clearNetworkStats()
                }
                runCatching {
                    ledger.appendAction(
                        "MOBILE_VAULT_PLAN_SAVED",
                        true,
                        if (cycleStart == null) {
                            "Plan local enregistré sans début de cycle; NetworkStats non utilisé."
                        } else {
                            "Plan local enregistré avec début de cycle; NetworkStats reste agrégé et optionnel."
                        }
                    )
                }
                dialog.dismiss()
                refreshBudgetUi()
                refreshOptionalNetworkStatsEvidence()
            }
            cancelAction.setOnClickListener {
                dialog.dismiss()
            }
            usageAccessAction.setOnClickListener {
                runCatching {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }.onFailure {
                    Toast.makeText(
                        this,
                        "Réglage Usage Access indisponible sur cet appareil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            clearAction.setOnClickListener {
                mobilePlanVaultStore.clear()
                mobileUsageEvidenceStore.clearUserReconciled()
                mobileUsageEvidenceStore.clearNetworkStats()
                runCatching {
                    ledger.appendAction(
                        "MOBILE_VAULT_PLAN_CLEARED",
                        true,
                        "Plan local supprimé par l’utilisateur."
                    )
                }
                dialog.dismiss()
                refreshBudgetUi()
            }
        }
        dialog.show()
    }

    private fun refreshOptionalNetworkStatsEvidence() {
        val plan = mobilePlanVaultStore.read() ?: return
        val cycleStart = plan.config.cycleStartAtEpochMillis ?: return
        val endAt = System.currentTimeMillis()
        if (endAt <= cycleStart) return

        val start = networkStatsSessionGate.tryStart()
        if (start.status != NetworkStatsQueryGateStatus.STARTED) return
        val token = requireNotNull(start.token)
        val request = NetworkStatsMobileUsageRequest(
            cycleStartAtEpochMillis = cycleStart,
            endAtEpochMillis = endAt
        )

        val future = networkStatsWorker.submit {
            val evidence =
                NetworkStatsMobileUsageReader(applicationContext).query(request)
            if (!networkStatsSessionGate.complete(token)) {
                return@submit
            }

            val stored =
                evidence.status == NetworkStatsMobileEvidenceStatus.AVAILABLE &&
                    mobileUsageEvidenceStore.writeNetworkStats(evidence)
            runOnUiThread {
                if (!isDestroyed && stored) {
                    refreshBudgetUi()
                }
            }
        }
        networkStatsFuture = future

        networkStatsDeadlineWorker.schedule(
            {
                if (networkStatsSessionGate.timeoutAndDisable(token)) {
                    future.cancel(true)
                    runOnUiThread {
                        if (!isDestroyed) {
                            runCatching {
                                ledger.appendAction(
                                    "MOBILE_VAULT_NETWORK_STATS_TIMEOUT",
                                    false,
                                    "NetworkStats a dépassé le budget de latence; voie optionnelle désactivée pour cette session."
                                )
                            }
                        }
                    }
                }
            },
            NetworkStatsQuerySessionGate.DEFAULT_TIMEOUT_MILLIS,
            TimeUnit.MILLISECONDS
        )
    }

    private fun refreshBudgetUi() {
        val snapshot = mobileBudget.sample()
        latestBudget = snapshot
        val enriched = latestTruth.copy(
            budgetState = effectiveBudgetState(snapshot)
        )
        render(enriched, snapshot, latestStability)
    }

    private fun optimizeWifi() {
        when (ActiveRecoveryPolicy.blockReason(recoveryModeStore.current(), latestTruth.transport)) {
            RecoveryBlockReason.OBSERVATION_ONLY -> {
                adviceTitleText.text = "Mode sûr actif"
                adviceText.text = "KINLINK observe uniquement. Aucune optimisation active n’est exécutée."
                return
            }
            RecoveryBlockReason.NON_WIFI -> {
                adviceTitleText.text = "Wi-Fi requis"
                adviceText.text = "Aucune action lancée : KINLINK ne touche pas aux données mobiles."
                return
            }
            RecoveryBlockReason.NONE -> Unit
        }
        wifiDoctorButton.isEnabled = false
        adviceTitleText.text = "Analyse de résilience Wi-Fi"
        adviceText.text =
            "Arbitrage Android + micro-tests bornés. Aucune donnée mobile n’est utilisée."

        Thread {
            val result = WifiOptimizer(this).optimize()
            runCatching { ledger.appendAction("WIFI_OPTIMIZE_${result.manualDiagnosisCause.name}", result.success, result.summary) }
            runOnUiThread {
                wifiDoctorButton.isEnabled = true
                adviceTitleText.text = when {
                    result.success && result.androidValidated -> "Wi-Fi validé"
                    result.success -> "Wi-Fi confirmé"
                    else -> "Wi-Fi non confirmé"
                }
                adviceText.text = result.summary
                detailText.text = detailText.text.toString() +
                    "\n\nAction résilience : ${result.action.name}" +
                    "\nAndroid VALIDATED : ${if (result.androidValidated) "oui" else "non"}" +
                    "\nMicro-tests HTTP tentés : ${result.probeAttempts}" +
                    "\nDNS Wi-Fi : ${result.dnsProbeSucceeded?.let { if (it) "confirmé" else "non confirmé" } ?: "non testé"}" +
                    "\nLatence DNS : ${result.dnsLatencyMillis?.let { "$it ms" } ?: "n/a"}" +
                    "\nDiagnostic manuel : ${result.manualDiagnosisCause.name}" +
                    "\nSignal Android : ${if (result.frameworkHintSent) "envoyé" else "non nécessaire"}" +
                    "\nMétriques réseau : ${if (result.bandwidthRefreshRequested) "rafraîchissement demandé" else "inchangées"}"
            }
        }.start()
    }

    private fun optimizeMobile() {
        val cm = getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork
        val caps = network?.let(cm::getNetworkCapabilities)
        val activeCellular =
            network != null &&
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val validated =
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val notSuspended =
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED) == true

        val nowWall = System.currentTimeMillis()
        val lastAction = runCatching {
            ledger.latestActionTimestamp(MobileAssistController.ACTION_PREFIX)
        }.getOrNull()
        val sinceLast =
            lastAction?.let { (nowWall - it).coerceAtLeast(0L) } ?: Long.MAX_VALUE
        val profileTuning =
            AutopilotProfileControlPolicy.tuning(currentProfile)
        val recentActions = runCatching {
            ledger.countActionsSince(
                MobileAssistController.ACTION_PREFIX,
                nowWall - MobileAssistPolicy.HOURLY_WINDOW_MS
            )
        }.getOrDefault(profileTuning.mobileAssistMaxActionsPerHour)
        val resourceSnapshot = runCatching {
            DeviceResourceGuard(this).snapshot()
        }.getOrNull()
        val resourceConstrained = resourceSnapshot?.constrained ?: true
        val budgetProtected =
            latestTruth.budgetState == BudgetState.BUNDLE_LOW ||
                latestTruth.budgetState == BudgetState.BUNDLE_EXHAUSTED ||
                latestTruth.budgetState == BudgetState.BUNDLE_EXPIRED
        val recentIneffective = runCatching {
            ledger.countExactActionSince(
                MobileAssistController.EVIDENCE_NO_BETTER,
                nowWall - 24L * 60L * 60L * 1000L
            ) + ledger.countExactActionSince(
                MobileAssistController.EVIDENCE_RELAPSED,
                nowWall - 24L * 60L * 60L * 1000L
            )
        }.getOrDefault(0)
        val recentUserIssue = latestUserIncidentTsWallMs?.let {
            (nowWall - it).coerceAtLeast(0L) <= 60L * 60L * 1000L
        } ?: false

        val decision = MobileAssistManualPolicy.decide(
            isCellular = activeCellular,
            validated = validated,
            notSuspended = notSuspended,
            observationOnly =
                recoveryModeStore.current() == RecoveryMode.OBSERVATION_ONLY,
            budgetProtected = budgetProtected,
            resourceConstrained = resourceConstrained,
            recentActions = recentActions,
            millisSinceLastAction = sinceLast,
            profile = currentProfile,
            preferSystemPanel = recentIneffective >= 2 || recentUserIssue
        )

        when (decision.action) {
            MobileAssistManualAction.OPEN_SYSTEM_CONNECTIVITY_PANEL -> {
                val opened = runCatching {
                    startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
                }.recoverCatching {
                    startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                }.isSuccess

                runCatching {
                    ledger.appendAction(
                        "MOBILE_ASSIST_ACTION_MANUAL_SYSTEM_PANEL",
                        opened,
                        if (opened)
                            "Panneau Android ouvert par action utilisateur; aucun probe mobile lancé."
                        else
                            "Impossible d’ouvrir le panneau Android; aucune autre action appliquée."
                    )
                }
                adviceTitleText.text = "Mobile Assist · contrôle Android"
                adviceText.text = if (opened)
                    "Android ne confirme pas une liaison mobile utilisable. Le contrôle système est ouvert sans speedtest ni bascule forcée."
                else
                    "KINLINK n’a lancé aucun trafic de test et n’a rien forcé."
            }

            MobileAssistManualAction.REFRESH_LINK_METRICS -> {
                val preActionTruth = ConnectivityTruthEngine.reduce(
                    caps,
                    cm.getLinkProperties(requireNotNull(network))
                ).copy(
                    budgetState = latestTruth.budgetState,
                    observedAtMillis = SystemClock.elapsedRealtime()
                )
                val baselineQuality =
                    PassiveLinkQualityPolicy.assess(preActionTruth).quality
                val baselineScore =
                    PassiveQualityScorePolicy.score(preActionTruth).score
                val baselineObservedAt = preActionTruth.observedAtMillis

                val refreshed = runCatching {
                    cm.requestBandwidthUpdate(requireNotNull(network))
                }.getOrDefault(false)

                runCatching {
                    ledger.appendAction(
                        "MOBILE_ASSIST_ACTION_MANUAL_REFRESH_METRICS",
                        refreshed,
                        if (refreshed)
                            "Rafraîchissement métrique Android demandé manuellement; aucun probe/speedtest mobile."
                        else
                            "Android n’a pas accepté le rafraîchissement; aucune action supplémentaire."
                    )
                }

                if (refreshed) {
                    ContextCompat.startForegroundService(
                        this,
                        Intent(this, KinlinkObserverService::class.java).apply {
                            action = KinlinkObserverService.ACTION_TRACK_MANUAL_MOBILE_ASSIST
                            putExtra(
                                KinlinkObserverService.EXTRA_BASELINE_QUALITY,
                                baselineQuality.name
                            )
                            putExtra(
                                KinlinkObserverService.EXTRA_BASELINE_SCORE,
                                baselineScore
                            )
                            putExtra(
                                KinlinkObserverService.EXTRA_BASELINE_OBSERVED_AT,
                                baselineObservedAt
                            )
                        }
                    )
                }

                adviceTitleText.text = "Mobile Assist · mesure actualisée"
                adviceText.text = if (refreshed)
                    "Android a actualisé ses métriques. Cela améliore l’observation, pas directement le débit : KINLINK attend une preuve réelle avant de parler d’amélioration."
                else
                    "Android n’a pas accepté l’actualisation. Aucun changement réseau n’a été forcé et aucun gain n’est annoncé."
            }

            MobileAssistManualAction.NONE -> {
                adviceTitleText.text = "Mobile Assist · protection active"
                adviceText.text = when (decision.blockReason) {
                    MobileAssistManualBlockReason.NOT_CELLULAR ->
                        "Les données mobiles ne sont pas le transport actif."
                    MobileAssistManualBlockReason.COOLDOWN ->
                        "Une action mobile vient déjà d’être lancée. Pause anti-répétition de 30 secondes."
                    MobileAssistManualBlockReason.HOURLY_CAP ->
                        "Plafond horaire Mobile Assist atteint. KINLINK évite les actions répétées."
                    MobileAssistManualBlockReason.OBSERVATION_ONLY ->
                        "Mode sûr actif : aucun rafraîchissement mobile n’est exécuté."
                    MobileAssistManualBlockReason.RESOURCE_CONSTRAINED ->
                        "Protection téléphone : " + resourceGuardReason(resourceSnapshot)
                    MobileAssistManualBlockReason.BUDGET_PROTECTED ->
                        "Protection du forfait active : KINLINK ne demande pas de travail radio supplémentaire."
                    MobileAssistManualBlockReason.NONE ->
                        "Aucune action mobile nécessaire."
                }
            }
        }
    }

    private fun exportDiagnostic() {
        runCatching {
            DiagnosticExporter(this).share(
                ledger.diagnosticSummary(
                    latestTruth,
                    recoveryModeStore.current().name,
                    installedVersionCode
                )
            )
        }.onFailure {
            Toast.makeText(this, "Impossible de préparer le diagnostic", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mobileAssistEvidenceLabel(): String {
        if (!::ledger.isInitialized) return cachedAssistEvidenceLabel
        val nowElapsed = SystemClock.elapsedRealtime()
        if (nowElapsed - lastAssistEvidenceRefreshElapsed < 30_000L) {
            return cachedAssistEvidenceLabel
        }
        lastAssistEvidenceRefreshElapsed = nowElapsed

        val counts = runCatching {
            ledger.actionCountsByPrefixSince(
                "MOBILE_ASSIST_EVIDENCE_",
                System.currentTimeMillis() - 24L * 60L * 60L * 1000L
            )
        }.getOrDefault(emptyMap())

        cachedAssistEvidenceLabel =
            MobileAssistEvidenceSummaryPolicy.summarize(counts).label
        return cachedAssistEvidenceLabel
    }

    private fun render(
        truth: NetworkTruth,
        budget: MobileBudgetSnapshot = latestBudget,
        stability: StabilityWindow? = latestStability,
        resourceSnapshotOverride: ResourceGuardSnapshot? = null
    ) {
        latestTruth = truth
        refreshFieldQualificationLabel()

        val assessment = ConnectivityStateClassifier.classify(truth)
        val vaultDecision = MobileVault.decide(truth, assessment)
        val passiveQuality = PassiveLinkQualityPolicy.assess(truth)
        val passiveScore = PassiveQualityScorePolicy.score(truth)
        val recentUserIssue = latestUserIncidentTsWallMs?.let {
            (System.currentTimeMillis() - it).coerceAtLeast(0L) <= 60L * 60L * 1000L
        } ?: false
        val userExperience = UserExperienceTruthPolicy.assess(
            truth = truth,
            reliability = latestReliability,
            recentUserIssue = recentUserIssue
        )
        val passiveProblem = PassiveProblemClassifier.classify(
            truth,
            instabilityScore = stability?.assessment?.score ?: 0,
            flapping = stability?.assessment?.flapping ?: false
        )
        val passiveGuidance = PassiveGuidancePolicy.guidance(passiveProblem)
        val sessionHealth = SessionHealthPolicy.assess(
            truth,
            instabilityScore = stability?.assessment?.score ?: 0,
            flapping = stability?.assessment?.flapping ?: false,
            passiveProblem = passiveProblem
        )
        val adaptiveDecision = AdaptivePolicyEngine.evaluate(
            truth = truth,
            instabilityScore = stability?.assessment?.score ?: 0,
            profile = currentProfile
        )
        val observationOnly =
            recoveryModeStore.current() == RecoveryMode.OBSERVATION_ONLY

        heroEyebrow.text = if (observationOnly) "MODE SÛR" else "AUTOPILOT"
        stateText.text = if (observationOnly) {
            "Observation · ${userExperience.headline}"
        } else {
            userExperience.headline
        }

        val qualityLabel = passiveQuality.quality.name
            .lowercase()
            .replaceFirstChar { it.uppercase() }
        heroDetailText.text = userExperience.detail
        transportText.text = "Réseau · ${transportLabel(truth)}"
        internetText.text = "Internet · ${internetLabel(truth)}"
        mobileText.text = when (truth.budgetState) {
            BudgetState.BUNDLE_EXHAUSTED ->
                "Mobile · protection data atteinte"
            BudgetState.BUNDLE_LOW ->
                "Mobile · protection data bientôt atteinte"
            else -> when (truth.transport.name) {
                "CELLULAR" -> "Mobile · Assist actif · routage Android"
                else -> "Mobile · protégé · aucune prise de contrôle"
            }
        }
        qualityScoreText.text = "Expérience · " + userExperience.statusLabel
        qualityProgress.visibility = View.GONE
        mobileBudgetText.text = mobileBudgetLabel(budget)

        val latestEvidence = runCatching {
            ledger.latestActionReceipt("MOBILE_ASSIST_EVIDENCE_")
        }.getOrNull()
        val evidenceAge = latestEvidence?.let {
            (System.currentTimeMillis() - it.tsWallMs).coerceAtLeast(0L)
        }
        val resourceSnapshot = resourceSnapshotOverride ?: runCatching {
            DeviceResourceGuard(this).snapshot()
        }.getOrNull()
        val resourceConstrained = resourceSnapshot?.constrained ?: true
        val controlPanel = ContinuousControlPanelPolicy.build(
            currentScore = passiveScore.score,
            observationOnly = observationOnly,
            resourceConstrained = resourceConstrained,
            latestEvidenceAction = latestEvidence?.action,
            latestEvidenceSummary = latestEvidence?.summary,
            latestEvidenceAgeMillis = evidenceAge
        )
        val confirmedBenefit =
            latestEvidence?.action?.endsWith("SUSTAINED_BETTER") == true
        beforeScoreText.text =
            if (confirmedBenefit) controlPanel.beforeScore?.toString() ?: "—" else "—"
        nowScoreText.text =
            if (confirmedBenefit) controlPanel.nowScore.toString() else "—"
        deltaScoreText.text =
            if (confirmedBenefit) {
                controlPanel.delta?.let { if (it > 0) "+$it" else it.toString() } ?: "—"
            } else {
                "—"
            }
        maintainedText.text =
            if (confirmedBenefit) controlPanel.maintenance
            else "Amélioration · aucun gain causal confirmé"
        evidenceText.text =
            if (resourceConstrained) {
                "Protection · " + resourceGuardReason(resourceSnapshot)
            } else if (confirmedBenefit) {
                controlPanel.evidence
            } else {
                mobileAssistEvidenceLabel()
            }
        val reliability = latestReliability
        reliabilityText.text = reliabilityHumanLabel(reliability)
        when (
            CockpitPrimaryActionPolicy.select(
                truth.transport,
                recoveryModeStore.current()
            )
        ) {
            CockpitPrimaryAction.WIFI_ASSIST -> {
                wifiDoctorButton.visibility = View.VISIBLE
                mobileAssistButton.visibility = View.GONE
            }
            CockpitPrimaryAction.MOBILE_ASSIST -> {
                wifiDoctorButton.visibility = View.GONE
                mobileAssistButton.visibility = View.VISIBLE
            }
            CockpitPrimaryAction.NONE -> {
                wifiDoctorButton.visibility = View.GONE
                mobileAssistButton.visibility = View.GONE
            }
        }

        if (observationOnly) {
            adviceTitleText.text = controlPanel.title
            adviceText.text =
                "Observation uniquement. La boucle reste visible mais toute action active est suspendue."
        } else {
            adviceTitleText.text =
                UserExperiencePresentationPolicy.controlTitle(
                    userExperience.state,
                    controlPanel.title
                )

            val handling = when {
                recentUserIssue ->
                    "Ton signalement est prioritaire : KINLINK ne classe plus la connexion comme bonne sur la seule base des estimations Android."
                userExperience.degraded ->
                    userExperience.detail
                else ->
                    userExperience.detail
            }
            adviceText.text = handling + "\nMode " + profileLabel(currentProfile) + " · " + mobileAssistEvidenceLabel()
        }

        detailText.text = buildString {
            append("État KINLINK : ${operationalStateLabel(assessment.state.name)}\n")
            append("Expérience utilisateur : ${userExperience.statusLabel}\n")
            append("Profil Autopilot : ${profileLabel(currentProfile)}\n")
            append("Mode de récupération : ${recoveryModeLabel(recoveryModeStore.current().name)}\n")
            val capabilities = PlatformCapabilityDiscovery(this@MainActivity).snapshot()
            append(
                "Capacités appareil : API ${capabilities.apiLevel} · Wi‑Fi=" +
                    if (capabilities.wifiFeature) "oui" else "non"
            )
            append(" · téléphonie=" + if (capabilities.telephonyFeature) "oui" else "non")
            append("\n")
            append(
                "Accès LAN KINLINK : " + when (capabilities.localNetworkAccess) {
                    LocalNetworkAccessState.NOT_REQUIRED_PRE_API_37 -> "permission dédiée non requise sur cet Android"
                    LocalNetworkAccessState.GRANTED -> "autorisé"
                    LocalNetworkAccessState.NOT_GRANTED -> "non autorisé · aucune fonction LAN directe ne doit démarrer"
                } + "\n"
            )
            append(
                "Data Saver Android : " + when (capabilities.dataSaver) {
                    DataSaverState.DISABLED -> "désactivé"
                    DataSaverState.WHITELISTED -> "actif · KINLINK autorisé"
                    DataSaverState.ENABLED -> "actif · KINLINK restreint en arrière-plan"
                    DataSaverState.UNKNOWN -> "état inconnu"
                } + "\n"
            )
            append(
                "Options futures : Usage Access=" +
                    if (capabilities.networkUsageAccess) "accordé" else "non accordé"
            )
            append(
                " · exclusion de routes VPN=" +
                    if (capabilities.vpnExcludeRouteSupported) "supportée" else "non supportée"
            )
            append(
                " · diagnostics Android avancés en Lite=" +
                    if (capabilities.connectivityDiagnosticsEligibleInLite) "éligibles" else "non éligibles"
            )
            append("\n")
            append("Réseau local : ${lanLabel(truth)}\n")
            append("Contexte : ${contextLabel(truth)}\n")
            append("Diagnostic : ${failureLabel(truth)}\n")
            append("Confiance technique de l’observation Android : ${(truth.confidence * 100).toInt()} %\n")
            append("Protection data : ${budgetStateLabel(truth.budgetState.name)}\n")
            append("Intention Autopilot : ${autopilotIntentLabel(adaptiveDecision.intent.name)}\n")
            append("Estimation Android : ↓${truth.downstreamKbps} kbps / ↑${truth.upstreamKbps} kbps · indication, pas débit réel\n")
            append("Classe interne : ${passiveQualityLabel(passiveQuality.quality.name)} · ne prouve pas la qualité ressentie\n")
            append("Indice interne non-QoE : ${passiveScore.score}/100 · diagnostic seulement\n")
            append("Cause passive : ${passiveCauseLabel(passiveProblem.cause.name)} · confiance ${passiveProblem.confidence}%\n")
            append("Cause passive détail : ${passiveProblem.summary}\n")
            append("Santé de session : ${sessionHealthLabel(sessionHealth.health.name)} · ${sessionHealth.summary}\n")
            append("DNS Android : ${truth.dnsServerCount} serveur(s) · DNS privé ${if (truth.privateDnsActive) "actif" else "non signalé"}\n")
            append("Pile IP : IPv4=${truth.hasIpv4Address} / IPv6=${truth.hasIpv6Address} · route4=${truth.hasIpv4DefaultRoute} / route6=${truth.hasIpv6DefaultRoute}\n")
            stability?.let {
                append("Instabilité 15 min : ${it.assessment.score}/100")
                append(" · ${it.transitions} transition(s)")
                if (it.assessment.flapping) append(" · oscillations répétées")
                append("\n")
            }
            reliability?.let {
                append("Interruptions 24 h : ${it.interruptionCount}")
                append(" · cumul ${it.cumulativeMillis} ms")
                append(" · max ${it.longestMillis} ms\n")
                append("Wi‑Fi lent 24 h : ${it.lowQualityEpisodeCount} épisode(s)")
                append(" · cumul ${it.lowQualityCumulativeMillis} ms\n")
                append("Mobile lent 24 h : ${it.mobileLowQualityEpisodeCount} épisode(s)")
                append(" · cumul ${it.mobileLowQualityCumulativeMillis} ms\n")
                it.dominantCause?.let { cause ->
                    append("Cause dominante 24 h : ${passiveCauseLabel(cause.removePrefix("PASSIVE_CAUSE_"))}\n")
                }
            }
            append("Preuve Mobile Assist : ${mobileAssistEvidenceLabel()}\n")
            if (budget.counterResetDetected) {
                append("Compteur mobile : baseline réinitialisée après reset/reboot\n")
            }
            append("\nPourquoi : ${vaultDecision.why}\n")
            append("Action data : ${vaultActionLabel(vaultDecision.what.name)}\n")
            append("Résultat : ${vaultDecision.result}\n\n")
            append(
                "Règle de preuve : requestBandwidthUpdate actualise des métriques Android; " +
                    "une hausse observée ensuite reste corrélative, pas une preuve de débit causé par KINLINK."
            )
        }
    }

    private fun refreshFieldQualificationLabel(force: Boolean = false) {
        if (!::ledger.isInitialized || !::versionText.isInitialized) return
        if (installedVersionCode < 8L) {
            versionText.text = "KINLINK $installedVersionName"
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (!force && now - lastQualificationUiRefreshElapsed < 30_000L) return
        lastQualificationUiRefreshElapsed = now

        val assessment = FieldCandidateQualificationPolicy.evaluate(
            FieldCandidateQualificationEvidence(
                coreSelfTestPasses = ledger.countSuccessfulExactAction(QualificationReceiptNames.coreSelfTest(installedVersionCode)
                ),
                observerSelfTestPasses = ledger.countSuccessfulExactAction(QualificationReceiptNames.observerSelfTest(installedVersionCode)
                ),
                mobileValidatedHandoffs = ledger.countSuccessfulExactAction(QualificationReceiptNames.handoffOutcome(
                        HandoffOutcome.MOBILE_VALIDATED,
                        installedVersionCode
                    )
                ),
                cellularToWifiReturns = ledger.countSuccessfulExactAction(QualificationReceiptNames.handoff(
                        HandoffKind.CELLULAR_TO_WIFI,
                        installedVersionCode
                    )
                ),
                runtimeResourcePasses = ledger.countSuccessfulExactAction(QualificationReceiptNames.resourceGate(
                        RuntimeResourceVerdict.PASS,
                        installedVersionCode
                    )
                ),
                runtimeResourceBlocks = ledger.countExactAction(QualificationReceiptNames.resourceGate(
                        RuntimeResourceVerdict.BLOCKED,
                        installedVersionCode
                    )
                ),
                latestRuntimeResourcePassMillis = ledger.latestExactActionTimestamp(QualificationReceiptNames.resourceGate(
                        RuntimeResourceVerdict.PASS,
                        installedVersionCode
                    )
                ),
                latestRuntimeResourceBlockMillis = ledger.latestExactActionTimestamp(QualificationReceiptNames.resourceGate(
                        RuntimeResourceVerdict.BLOCKED,
                        installedVersionCode
                    )
                )
            )
        )

        val state = when (assessment.verdict) {
            FieldCandidateQualificationVerdict.PASS -> "terrain QUALIFIÉ"
            FieldCandidateQualificationVerdict.BLOCKED -> "terrain BLOQUÉ"
            FieldCandidateQualificationVerdict.PENDING -> "qualification terrain en cours"
        }
        versionText.text = "KINLINK $installedVersionName · $state"
    }

    private fun resourceGuardReason(snapshot: ResourceGuardSnapshot?): String {
        if (snapshot == null) return "état ressources non lisible; KINLINK reste passif."
        val reasons = mutableListOf<String>()
        if (snapshot.powerSaveMode) reasons += "économie batterie"
        if (snapshot.thermalModerateOrWorse) reasons += "température"
        if (snapshot.lowMemory) reasons += "mémoire basse"
        return if (reasons.isEmpty()) {
            "garde-fou actif."
        } else {
            reasons.joinToString(" + ") + "; action active suspendue."
        }
    }

    private fun currentMobileVaultAssessment(): MobileVaultAssessment? {
        val stored = mobilePlanVaultStore.read() ?: return null
        val evidence = mobileUsageEvidenceStore.read()
        val observations = buildList {
            val userBytes = evidence?.userReconciledUsedBytes
            val userAt = evidence?.userReconciledObservedAtEpochMillis
            if (userBytes != null && userAt != null) {
                add(
                    MobileUsageObservation(
                        usedBytes = userBytes,
                        source = MobilePlanUsageSource.USER_RECONCILED,
                        attributionScope = MobileUsageAttributionScope.PLAN_EXACT,
                        observedAtEpochMillis = userAt,
                        confidencePercent = 100
                    )
                )
            }
            evidence?.aggregateCounterState?.let {
                add(
                    MobileUsageObservation(
                        usedBytes = it.provenCycleBytes,
                        source = MobilePlanUsageSource.ANDROID_DEVICE_WIDE_COUNTER,
                        attributionScope = MobileUsageAttributionScope.DEVICE_MOBILE_AGGREGATE,
                        observedAtEpochMillis = stored.updatedAtEpochMillis,
                        confidencePercent = 70
                    )
                )
            }
            evidence?.networkStatsEvidence
                ?.takeIf {
                    stored.config.cycleStartAtEpochMillis != null &&
                        it.cycleStartAtEpochMillis ==
                            stored.config.cycleStartAtEpochMillis
                }
                ?.let {
                    add(
                        MobileUsageObservation(
                            usedBytes = it.usedBytes,
                            source = MobilePlanUsageSource.NETWORK_STATS_OPTIONAL,
                            attributionScope =
                                MobileUsageAttributionScope.DEVICE_MOBILE_AGGREGATE,
                            observedAtEpochMillis =
                                it.observedAtEpochMillis,
                            confidencePercent = it.confidencePercent
                        )
                    )
                }
        }
        val resolution = MobileUsageReconciliationPolicy.reconcile(
            observations,
            System.currentTimeMillis(),
            maxAgeMillis = 31L * 24L * 60L * 60L * 1000L
        )
        return MobilePlanVaultPolicy.evaluate(
            stored.config,
            resolution.usage,
            System.currentTimeMillis()
        )
    }

    private fun effectiveBudgetState(snapshot: MobileBudgetSnapshot): BudgetState {
        val plan = currentMobileVaultAssessment() ?: return snapshot.state
        return when (plan.zone) {
            MobileVaultZone.NORMAL -> {
                if (snapshot.state == BudgetState.BUNDLE_EXHAUSTED ||
                    snapshot.state == BudgetState.BUNDLE_LOW
                ) snapshot.state else BudgetState.BUNDLE_OK
            }
            MobileVaultZone.PROTECTED_RESERVE,
            MobileVaultZone.RESCUE_ONLY,
            MobileVaultZone.CRITICAL_INTERACTIVE_ONLY -> BudgetState.BUNDLE_LOW
            MobileVaultZone.EXHAUSTED -> BudgetState.BUNDLE_EXHAUSTED
            MobileVaultZone.EXPIRED -> BudgetState.BUNDLE_EXPIRED
            MobileVaultZone.UNKNOWN -> BudgetState.BALANCE_UNKNOWN
        }
    }

    private fun mobileVaultLabel(assessment: MobileVaultAssessment): String {
        val remaining = assessment.remainingBytes?.let(::formatMobileBytes)
        return when (assessment.zone) {
            MobileVaultZone.NORMAL ->
                "Mobile Vault · ${remaining ?: "solde inconnu"} restant(s) · zone normale"
            MobileVaultZone.PROTECTED_RESERVE ->
                "Mobile Vault · réserve protégée · ${remaining ?: "reste inconnu"}"
            MobileVaultZone.RESCUE_ONLY ->
                "Mobile Vault · secours uniquement · ${remaining ?: "reste inconnu"}"
            MobileVaultZone.CRITICAL_INTERACTIVE_ONLY ->
                "Mobile Vault · réserve critique · ${remaining ?: "reste inconnu"}"
            MobileVaultZone.EXHAUSTED ->
                "Mobile Vault · forfait épuisé"
            MobileVaultZone.EXPIRED ->
                "Mobile Vault · forfait expiré"
            MobileVaultZone.UNKNOWN ->
                "Mobile Vault · forfait configuré · consommation à réconcilier"
        }
    }

    private fun mobileBudgetLabel(snapshot: MobileBudgetSnapshot): String {
        currentMobileVaultAssessment()?.let { return mobileVaultLabel(it) }
        if (!snapshot.supported) {
            return "Données mobiles · compteur Android indisponible"
        }
        val used = formatMobileBytes(snapshot.usedTodayBytes)
        val limit = snapshot.dailyLimitBytes?.let(::formatMobileBytes)
        return if (limit == null) {
            "Données mobiles · $used observés · aucune limite"
        } else {
            "Données mobiles · $used / $limit observés"
        }
    }

    private fun formatMobileBytes(bytes: Long): String {
        val safe = bytes.coerceAtLeast(0L).toDouble()
        return if (safe >= 1_000_000_000.0) {
            String.format(Locale.US, "%.2f GB", safe / 1_000_000_000.0)
        } else {
            String.format(Locale.US, "%.0f MB", safe / 1_000_000.0)
        }
    }

    private fun reliabilityHumanLabel(reliability: RecentReliabilityWindow?): String {
        if (reliability == null) return "Historique 24 h · collecte en cours"
        val burden = RecentReliabilityPolicy.classify(
            reliability.interruptionCount,
            reliability.cumulativeMillis,
            reliability.longestMillis,
            reliability.lowQualityEpisodeCount + reliability.mobileLowQualityEpisodeCount,
            reliability.lowQualityCumulativeMillis + reliability.mobileLowQualityCumulativeMillis,
            maxOf(reliability.lowQualityLongestMillis, reliability.mobileLowQualityLongestMillis)
        )
        val burdenLabel = when (burden) {
            com.terminator364.kinlink.core.RecentReliabilityBurden.QUIET -> "calme"
            com.terminator364.kinlink.core.RecentReliabilityBurden.NOTICEABLE -> "à surveiller"
            com.terminator364.kinlink.core.RecentReliabilityBurden.UNSTABLE -> "instable"
            com.terminator364.kinlink.core.RecentReliabilityBurden.SEVERE -> "très instable"
        }
        return "Historique 24 h · $burdenLabel · " +
            "${reliability.interruptionCount} coupure(s) · " +
            UserExperiencePresentationPolicy.platformStallLabel(
                reliability.platformDataStallCount
            ) + " · " +
            "${reliability.lowQualityEpisodeCount} épisode(s) Wi‑Fi lent · " +
            "${reliability.mobileLowQualityEpisodeCount} épisode(s) mobile lent"
    }
    private fun operationalStateLabel(code: String): String = when (code) {
        "WIFI_HEALTHY" -> "Wi‑Fi validé"
        "WIFI_DEGRADED" -> "Wi‑Fi dégradé"
        "WIFI_BROWNOUT" -> "Wi‑Fi à reconnecter"
        "MOBILE_HEALTHY" -> "Mobile validé"
        "MOBILE_DEGRADED" -> "Mobile dégradé"
        "DATA_LOW" -> "Data proche de la limite"
        "DATA_EXHAUSTED" -> "Limite data atteinte"
        "DATA_EXPIRED" -> "Forfait signalé expiré"
        "BALANCE_UNKNOWN" -> "Budget data inconnu"
        "LAN_OK_WAN_DOWN" -> "LAN disponible, Internet non confirmé"
        "OFFLINE" -> "Hors ligne"
        "RECOVERING" -> "Vérification en cours"
        else -> "État non classé"
    }

    private fun recoveryModeLabel(code: String): String = when (code) {
        "AUTOMATIC" -> "Automatique"
        "OBSERVATION_ONLY" -> "Observation uniquement"
        else -> "Non identifié"
    }

    private fun budgetStateLabel(code: String): String = when (code) {
        "BUNDLE_OK" -> "dans la limite"
        "BUNDLE_LOW" -> "proche de la limite"
        "BUNDLE_EXHAUSTED" -> "limite atteinte"
        "BUNDLE_EXPIRED" -> "forfait signalé expiré"
        "BALANCE_UNKNOWN" -> "solde/forfait non connu"
        else -> "non identifié"
    }

    private fun autopilotIntentLabel(code: String): String = when (code) {
        "HOLD_STEADY" -> "Maintenir l’état"
        "PROTECT_MOBILE" -> "Protéger les données mobiles"
        "OBSERVE_WIFI" -> "Surveiller le Wi‑Fi"
        "MOBILE_ASSIST" -> "Assistance mobile bornée"
        "CAPTIVE_PORTAL_ACTION" -> "Connexion au portail requise"
        "RECOVERY_CANDIDATE" -> "Récupération potentielle"
        "WAIT_FOR_EVIDENCE" -> "Attendre davantage de preuve"
        else -> "Intention non classée"
    }

    private fun passiveQualityLabel(code: String): String = when (code) {
        "COMFORTABLE" -> "indicateurs Android favorables"
        "LIMITED" -> "indicateurs Android limités"
        "CONSTRAINED" -> "indicateurs Android très limités"
        "UNKNOWN" -> "indéterminée"
        else -> "indéterminée"
    }

    private fun passiveCauseLabel(code: String): String = when (code) {
        "NONE" -> "aucune panne passive certaine"
        "NO_LINK" -> "aucun lien réseau"
        "CAPTIVE_PORTAL" -> "portail captif"
        "ADDRESSING_SUSPECT" -> "adressage IP à vérifier"
        "ROUTE_CONFIGURATION_SUSPECT" -> "route réseau à vérifier"
        "DNS_CONFIGURATION_SUSPECT" -> "configuration DNS à vérifier"
        "NETWORK_SUSPENDED" -> "réseau Wi‑Fi suspendu"
        "WEAK_WIFI_SIGNAL" -> "signal Wi‑Fi faible"
        "CONGESTION_SUSPECT" -> "congestion Wi‑Fi possible"
        "LOW_CAPACITY" -> "capacité Wi‑Fi limitée"
        "MOBILE_NETWORK_SUSPENDED" -> "réseau mobile suspendu"
        "MOBILE_WEAK_SIGNAL" -> "signal mobile faible"
        "MOBILE_CONGESTION_SUSPECT" -> "congestion mobile possible"
        "MOBILE_LOW_CAPACITY" -> "capacité mobile limitée"
        "FLAPPING" -> "oscillations réseau"
        "WAN_UNVALIDATED" -> "Internet non validé"
        "MOBILE_UNVALIDATED" -> "Internet mobile non validé"
        "UNKNOWN" -> "cause indéterminée"
        else -> code.lowercase().replace('_', ' ')
    }

    private fun sessionHealthLabel(code: String): String = when (code) {
        "HEALTHY" -> "stable"
        "WATCH" -> "à surveiller"
        "DEGRADED" -> "dégradée"
        "CRITICAL" -> "critique"
        else -> "indéterminée"
    }

    private fun vaultActionLabel(code: String): String = when (code) {
        "NO_ACTION" -> "Aucune action"
        "HOLD_MOBILE_RECOVERY" -> "Suspendre les actions mobiles KINLINK"
        "KEEP_WIFI_PREFERRED" -> "Préserver le Wi‑Fi"
        "PRESERVE_LAN" -> "Préserver le réseau local"
        else -> code.lowercase().replace('_', ' ')
    }

    private fun installSystemBarInsets() {
        val root = findViewById<ScrollView>(R.id.rootScroll)
        root.setOnApplyWindowInsetsListener { view, insets ->
            val bars = insets.getInsets(WindowInsets.Type.systemBars())
            view.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }
        root.requestApplyInsets()
    }

    private fun transportLabel(t: NetworkTruth): String = when (t.transport.name) {
        "WIFI" -> "Wi‑Fi"
        "CELLULAR" -> "Données mobiles"
        "ETHERNET" -> "Réseau filaire"
        "VPN" -> "VPN"
        else -> "Aucune connexion"
    }

    private fun internetLabel(t: NetworkTruth): String = when (t.internetState.name) {
        "VALIDATED" -> "Disponible"
        "CAPTIVE_PORTAL" -> "Connexion requise"
        "PARTIAL" -> "Accès limité"
        "OFFLINE" -> "Indisponible"
        else -> "À vérifier"
    }

    private fun lanLabel(t: NetworkTruth): String = when (t.lanState.name) {
        "LINK_PRESENT", "HEALTHY" -> "Disponible"
        "DOWN" -> "Indisponible"
        else -> "Non vérifié"
    }

    private fun contextLabel(t: NetworkTruth): String = when (t.context.name) {
        "UNKNOWN_WIFI" -> "Wi‑Fi"
        "MOBILE_RESILIENT" -> "Données mobiles"
        "CAPTIVE_PORTAL" -> "Portail de connexion"
        "OFFLINE" -> "Hors ligne"
        else -> "Non identifié"
    }

    private fun failureLabel(t: NetworkTruth): String = when (t.failureDomain.name) {
        "NONE" -> "Aucun problème détecté"
        "NO_LINK" -> "Aucune liaison réseau"
        "ISP" -> "Accès Internet non confirmé"
        "ROUTER" -> "Connexion au routeur requise"
        else -> "En cours d’analyse"
    }
    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 4107
        const val EXTRA_PRODUCT_ACTION = "kinlink.product.action"
        const val ACTION_OPEN_MOBILE_VAULT = "open_mobile_vault"
        const val ACTION_EXPORT_DIAGNOSTIC = "export_diagnostic"
    }

}
