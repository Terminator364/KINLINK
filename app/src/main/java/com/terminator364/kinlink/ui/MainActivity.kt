package com.terminator364.kinlink.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.terminator364.kinlink.R
import com.terminator364.kinlink.core.ActiveRecoveryPolicy
import com.terminator364.kinlink.core.AdaptivePolicyEngine
import com.terminator364.kinlink.core.AutopilotProfile
import com.terminator364.kinlink.core.AutopilotProfileStore
import com.terminator364.kinlink.core.BudgetState
import com.terminator364.kinlink.core.ConnectivityStateClassifier
import com.terminator364.kinlink.core.CockpitPrimaryAction
import com.terminator364.kinlink.core.CockpitPrimaryActionPolicy
import com.terminator364.kinlink.core.DeviceResourceGuard
import com.terminator364.kinlink.core.KinlinkObserverService
import com.terminator364.kinlink.core.MobileBudgetSnapshot
import com.terminator364.kinlink.core.MobileBudgetTracker
import com.terminator364.kinlink.core.MobileAssistController
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
import com.terminator364.kinlink.core.NetworkTruth
import com.terminator364.kinlink.core.NotificationPermissionPolicy
import com.terminator364.kinlink.core.WifiDoctor
import com.terminator364.kinlink.core.WifiOptimizer
import com.terminator364.kinlink.data.DiagnosticExporter
import com.terminator364.kinlink.data.StabilityWindow
import com.terminator364.kinlink.data.RecentReliabilityWindow
import com.terminator364.kinlink.data.TelemetryLedger
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var observer: NetworkObserver
    private lateinit var ledger: TelemetryLedger
    private lateinit var mobileBudget: MobileBudgetTracker
    private lateinit var profileStore: AutopilotProfileStore
    private lateinit var recoveryModeStore: RecoveryModeStore

    private lateinit var heroEyebrow: TextView
    private lateinit var stateText: TextView
    private lateinit var transportText: TextView
    private lateinit var internetText: TextView
    private lateinit var mobileText: TextView
    private lateinit var mobileBudgetText: TextView
    private lateinit var reliabilityText: TextView
    private lateinit var detailText: TextView
    private lateinit var heroDetailText: TextView
    private lateinit var adviceTitleText: TextView
    private lateinit var adviceText: TextView
    private lateinit var incidentMarkerButton: TextView
    private lateinit var technicalToggle: TextView
    private lateinit var diagnosticExport: TextView
    private lateinit var wifiDoctorButton: TextView
    private lateinit var mobileAssistButton: TextView
    private lateinit var budgetButton: TextView
    private lateinit var profileButton: TextView
    private lateinit var safeModeButton: TextView
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
    private var installedVersionName: String = "?"
    private var installedVersionCode: Long = -1L
    private var lastQualificationUiRefreshElapsed: Long = 0L
    private var lastAssistEvidenceRefreshElapsed: Long = 0L
    private var cachedAssistEvidenceLabel: String =
        "Preuve 24 h · aucune action Mobile Assist évaluée"

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
        reliabilityText = findViewById(R.id.reliabilityText)
        detailText = findViewById(R.id.detailText)
        heroDetailText = findViewById(R.id.heroDetailText)
        adviceTitleText = findViewById(R.id.adviceTitleText)
        adviceText = findViewById(R.id.adviceText)
        incidentMarkerButton = findViewById(R.id.incidentMarkerButton)
        technicalToggle = findViewById(R.id.technicalToggle)
        diagnosticExport = findViewById(R.id.diagnosticExport)
        wifiDoctorButton = findViewById(R.id.wifiDoctorButton)
        mobileAssistButton = findViewById(R.id.mobileAssistButton)
        budgetButton = findViewById(R.id.budgetButton)
        profileButton = findViewById(R.id.profileButton)
        safeModeButton = findViewById(R.id.safeModeButton)
        versionText = findViewById(R.id.versionText)
        runCatching { packageManager.getPackageInfo(packageName, 0) }.getOrNull()?.let { info ->
            installedVersionName = info.versionName ?: "?"
            installedVersionCode = info.longVersionCode
        }

        installSystemBarInsets()

        ledger = TelemetryLedger(this)
        mobileBudget = MobileBudgetTracker(this)
        profileStore = AutopilotProfileStore(this)
        recoveryModeStore = RecoveryModeStore(this)
        refreshFieldQualificationLabel(force = true)
        currentProfile = profileStore.current()
        refreshProfileButton()
        refreshSafeModeButton()

        incidentMarkerButton.setOnClickListener { recordUserIncidentMarker() }
        technicalToggle.setOnClickListener { toggleTechnicalDetails() }
        diagnosticExport.setOnClickListener { exportDiagnostic() }
        wifiDoctorButton.setOnClickListener { optimizeWifi() }
        mobileAssistButton.setOnClickListener { optimizeMobile() }
        budgetButton.setOnClickListener { configureMobileBudget() }
        profileButton.setOnClickListener {
            currentProfile = profileStore.cycle()
            refreshProfileButton()
            render(latestTruth, latestBudget, latestStability)
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
            val enrichedTruth = rawTruth.copy(budgetState = budget.state)
            val stability = runCatching { ledger.stabilityWindow() }.getOrNull()
            val reliability = runCatching { ledger.recentReliabilityWindow() }.getOrNull()

            runOnUiThread {
                latestBudget = budget
                latestStability = stability
                latestReliability = reliability
                render(enrichedTruth, budget, stability)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        observer.start()
    }

    override fun onStop() {
        observer.stop()
        super.onStop()
    }

    override fun onDestroy() {
        ledger.close()
        super.onDestroy()
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

    private fun refreshProfileButton() {
        profileButton.text = "Profil · ${profileLabel(currentProfile)}"
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
        AutopilotProfile.CONSERVATIVE -> "Conservateur"
        AutopilotProfile.BALANCED -> "Équilibré"
        AutopilotProfile.MAXIMUM_STABILITY -> "Stabilité max"
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
            Toast.makeText(
                this,
                "État enregistré. Tu pourras l’exporter dans les détails techniques.",
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
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Ex. 100"
            mobileBudget.configuredDailyLimitMiB()?.let { setText(it.toString()) }
        }

        AlertDialog.Builder(this)
            .setTitle("Protection des données mobiles")
            .setMessage(
                "Seuil de prudence optionnel en MiB. Il s’appuie sur la variation du compteur mobile global Android observée par KINLINK. " +
                    "Ce n’est ni la consommation propre de KINLINK ni le solde opérateur. KINLINK ne lance jamais de speedtest mobile."
            )
            .setView(input)
            .setPositiveButton("Enregistrer") { _, _ ->
                val value = input.text.toString().trim().toIntOrNull()
                mobileBudget.setDailyLimitMiB(value)
                refreshBudgetUi()
            }
            .setNeutralButton("Sans plafond") { _, _ ->
                mobileBudget.setDailyLimitMiB(null)
                refreshBudgetUi()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun refreshBudgetUi() {
        val snapshot = mobileBudget.sample()
        latestBudget = snapshot
        val enriched = latestTruth.copy(budgetState = snapshot.state)
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
        val recentActions = runCatching {
            ledger.countActionsSince(
                MobileAssistController.ACTION_PREFIX,
                nowWall - MobileAssistPolicy.HOURLY_WINDOW_MS
            )
        }.getOrDefault(MobileAssistPolicy.MAX_ACTIONS_PER_HOUR)
        val resourceConstrained = runCatching {
            DeviceResourceGuard(this).snapshot().constrained
        }.getOrDefault(true)
        val budgetProtected =
            latestTruth.budgetState == BudgetState.BUNDLE_LOW ||
                latestTruth.budgetState == BudgetState.BUNDLE_EXHAUSTED ||
                latestTruth.budgetState == BudgetState.BUNDLE_EXPIRED

        val decision = MobileAssistManualPolicy.decide(
            isCellular = activeCellular,
            validated = validated,
            notSuspended = notSuspended,
            observationOnly =
                recoveryModeStore.current() == RecoveryMode.OBSERVATION_ONLY,
            budgetProtected = budgetProtected,
            resourceConstrained = resourceConstrained,
            recentActions = recentActions,
            millisSinceLastAction = sinceLast
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

                val quality = PassiveLinkQualityPolicy.assess(latestTruth)
                adviceTitleText.text = "Mobile Assist · mesure actualisée"
                adviceText.text = if (refreshed)
                    "Android a accepté une actualisation des métriques. Cela améliore l’observation, pas directement le débit. Qualité passive : ${quality.quality.name}."
                else
                    "Android n’a pas accepté l’actualisation. Aucun changement réseau n’a été forcé. Qualité passive : ${quality.quality.name}."
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
                        "Batterie, mémoire ou température : KINLINK reste passif pour protéger le téléphone."
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

        val sustained = counts["SUSTAINED_BETTER"] ?: 0
        val relapsed = counts["RELAPSED"] ?: 0
        val lateRelapse = counts["RELAPSED_AFTER_SUSTAINED"] ?: 0
        val noBetter = counts["NO_BETTER"] ?: 0
        val metrics = counts["METRICS_AVAILABLE"] ?: 0
        val inconclusive = counts["INCONCLUSIVE"] ?: 0
        val total = sustained + relapsed + lateRelapse + noBetter + metrics + inconclusive

        cachedAssistEvidenceLabel = if (total == 0) {
            "Preuve 24 h · aucune action Mobile Assist évaluée"
        } else {
            buildString {
                append("Preuve 24 h · mieux durable corrélé=$sustained")
                append(" · rechute=${relapsed + lateRelapse}")
                append(" · sans mieux=$noBetter")
                if (metrics > 0) append(" · métriques=$metrics")
            }
        }
        return cachedAssistEvidenceLabel
    }

    private fun render(
        truth: NetworkTruth,
        budget: MobileBudgetSnapshot = latestBudget,
        stability: StabilityWindow? = latestStability
    ) {
        latestTruth = truth
        refreshFieldQualificationLabel()

        val assessment = ConnectivityStateClassifier.classify(truth)
        val vaultDecision = MobileVault.decide(truth, assessment)
        val passiveQuality = PassiveLinkQualityPolicy.assess(truth)
        val passiveScore = PassiveQualityScorePolicy.score(truth)
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
        stateText.text =
            if (observationOnly) "Observation uniquement" else assessment.headline

        val qualityLabel = passiveQuality.quality.name
            .lowercase()
            .replaceFirstChar { it.uppercase() }
        heroDetailText.text = if (observationOnly) {
            "${transportLabel(truth)} · surveillance passive · Android garde le contrôle"
        } else {
            "${transportLabel(truth)} · indice passif ${passiveScore.score}/100 · $qualityLabel"
        }

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
        mobileBudgetText.text = mobileBudgetLabel(budget)

        val reliability = latestReliability
        reliabilityText.text = if (reliability == null) {
            "24 h · historique en préparation"
        } else {
            val burden = RecentReliabilityPolicy.classify(
                reliability.interruptionCount,
                reliability.cumulativeMillis,
                reliability.longestMillis,
                reliability.lowQualityEpisodeCount +
                    reliability.mobileLowQualityEpisodeCount,
                reliability.lowQualityCumulativeMillis +
                    reliability.mobileLowQualityCumulativeMillis,
                maxOf(
                    reliability.lowQualityLongestMillis,
                    reliability.mobileLowQualityLongestMillis
                )
            )
            "24 h · ${burden.name.lowercase().replaceFirstChar { it.uppercase() }}" +
                " · coupures=${reliability.interruptionCount}" +
                " · Wi‑Fi lent=${reliability.lowQualityEpisodeCount}" +
                " · mobile lent=${reliability.mobileLowQualityEpisodeCount}"
        }

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
            adviceTitleText.text = "Pilotage continu · pause sûre"
            adviceText.text =
                "Observation et preuve uniquement. Aucune action active tant que le mode sûr reste ON."
        } else {
            adviceTitleText.text = when (sessionHealth.health.name) {
                "HEALTHY" -> "Pilotage continu · stable"
                "WATCH" -> "Pilotage continu · surveillance"
                "DEGRADED" -> "Pilotage continu · dégradation détectée"
                else -> "Pilotage continu · protection"
            }

            val handling = when (truth.transport) {
                com.terminator364.kinlink.core.Transport.CELLULAR ->
                    if (passiveQuality.quality == PassiveLinkQuality.COMFORTABLE) {
                        "Liaison mobile utilisable : aucune action inutile. Si elle rechute, KINLINK réévalue après ses garde-fous."
                    } else {
                        passiveGuidance.message
                    }
                com.terminator364.kinlink.core.Transport.WIFI ->
                    if (passiveQuality.quality == PassiveLinkQuality.COMFORTABLE) {
                        "Wi‑Fi utilisable : surveillance. KINLINK n’agit que si la dégradation persiste."
                    } else {
                        passiveGuidance.message
                    }
                else -> passiveGuidance.message
            }
            adviceText.text = handling + "\n" + mobileAssistEvidenceLabel()
        }

        detailText.text = buildString {
            append("État KINLINK : ${assessment.state.name}\n")
            append("Profil Autopilot : ${currentProfile.name}\n")
            append("Mode récupération : ${recoveryModeStore.current().name}\n")
            append("Réseau local : ${lanLabel(truth)}\n")
            append("Contexte : ${contextLabel(truth)}\n")
            append("Diagnostic : ${failureLabel(truth)}\n")
            append("Confiance Android : ${(truth.confidence * 100).toInt()} %\n")
            append("Budget mobile : ${truth.budgetState.name}\n")
            append("Autopilot : ${adaptiveDecision.intent.name}\n")
            append("Capacité Android : ↓${truth.downstreamKbps} kbps / ↑${truth.upstreamKbps} kbps\n")
            append("Qualité passive : ${passiveQuality.quality.name} · ${passiveQuality.summary}\n")
            append("Indice passif : ${passiveScore.score}/100 · ${passiveScore.summary}\n")
            append("Cause passive : ${passiveProblem.cause.name} · confiance ${passiveProblem.confidence}%\n")
            append("Cause passive détail : ${passiveProblem.summary}\n")
            append("Santé de session : ${sessionHealth.health.name} · ${sessionHealth.summary}\n")
            append("DNS Android : ${truth.dnsServerCount} serveur(s) · DNS privé ${if (truth.privateDnsActive) "actif" else "non signalé"}\n")
            append("Pile IP : IPv4=${truth.hasIpv4Address} / IPv6=${truth.hasIpv6Address} · route4=${truth.hasIpv4DefaultRoute} / route6=${truth.hasIpv6DefaultRoute}\n")
            stability?.let {
                append("Instabilité 15 min : ${it.assessment.score}/100")
                append(" · ${it.transitions} transition(s)")
                if (it.assessment.flapping) append(" · FLAPPING")
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
                    append("Cause dominante 24 h : $cause\n")
                }
            }
            append("Preuve Mobile Assist : ${mobileAssistEvidenceLabel()}\n")
            if (budget.counterResetDetected) {
                append("Compteur mobile : baseline réinitialisée après reset/reboot\n")
            }
            append("\nWHY : ${vaultDecision.why}\n")
            append("WHAT : ${vaultDecision.what.name}\n")
            append("RESULT : ${vaultDecision.result}\n\n")
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

    private fun mobileBudgetLabel(snapshot: MobileBudgetSnapshot): String {
        if (!snapshot.supported) return "Compteur mobile Android · indisponible sur cet appareil"

        val used = String.format(Locale.US, "%.1f", snapshot.usedTodayMiB)
        val limit = snapshot.dailyLimitMiB
        return if (limit == null) {
            "Compteur mobile Android observé · $used MiB depuis la baseline KINLINK aujourd’hui · seuil non configuré"
        } else {
            "Compteur mobile Android observé · $used / $limit MiB depuis la baseline KINLINK aujourd’hui"
        }
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
    }

}
