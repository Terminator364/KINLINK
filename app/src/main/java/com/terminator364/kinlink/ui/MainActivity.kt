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
        heroEyebrow.text = if (observationOnly) "MODE SÛR" else "AUTOPILOT"
        stateText.text = if (observationOnly) "Observation uniquement" else assessment.headline

        val qualityLabel = passiveQuality.quality.name.lowercase()
            .replaceFirstChar { it.uppercase() }
        heroDetailText.text = if (observationOnly) {
            "${transportLabel(truth)} · surveillance passive · Android garde le contrôle"
        } else {
            "${transportLabel(truth)} · indice passif ${passiveScore.score}/100 · $qualityLabel"
        }

        transportText.text = "Réseau · ${transportLabel(truth)}"
        internetText.text = "Internet · ${internetLabel(truth)}"
        mobileText.text = when (truth.budgetState) {
            BudgetState.BUNDLE_EXHAUSTED -> "Mobile · protection data atteinte"
            BudgetState.BUNDLE_LOW -> "Mobile · protection data bientôt atteinte"
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
                reliability.lowQualityEpisodeCount + reliability.mobileLowQualityEpisodeCount,
                reliability.lowQualityCumulativeMillis + reliability.mobileLowQualityCumulativeMillis,
                maxOf(reliability.lowQualityLongestMillis, reliability.mobileLowQualityLongestMillis)
            )
            "24 h · ${burden.name.lowercase().replaceFirstChar { it.uppercase() }} · coupures=${reliability.interruptionCount} · Wi‑Fi lent=${reliability.lowQualityEpisodeCount} · mobile lent=${reliability.mobileLowQualityEpisodeCount}"
        }

        wifiDoctorButton.visibility =
            if (truth.transport == com.terminator364.kinlink.core.Transport.WIFI) View.VISIBLE else View.GONE
        mobileAssistButton.visibility =
            if (truth.transport == com.terminator364.kinlink.core.Transport.CELLULAR) View.VISIBLE else View.GONE

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
                        "Liaison mobile utilisable : aucune action inutile. Si elle rechute, Mobile Assist réévalue après cooldown."
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
            latestReliability?.let { reliability ->
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
                append("Interruptions 24 h : ${reliability.interruptionCount}")
                append(" · cumul ${reliability.cumulativeMillis} ms")
                append(" · max ${reliability.longestMillis} ms")
                append(" · ${burden.name}\n")
                append("Wi-Fi lent validé 24 h : ${reliability.lowQualityEpisodeCount} épisode(s)")
                append(" · cumul ${reliability.lowQualityCumulativeMillis} ms")
                append(" · max ${reliability.lowQualityLongestMillis} ms\n")
                append("Mobile lent validé 24 h : ${reliability.mobileLowQualityEpisodeCount} épisode(s)")
                append(" · cumul ${reliability.mobileLowQualityCumulativeMillis} ms")
                append(" · max ${reliability.mobileLowQualityLongestMillis} ms\n")
                reliability.dominantCause?.let { cause ->
                    append("Cause dominante 24 h : $cause\n")
                }
            }
            if (budget.counterResetDetected) {
                append("Compteur mobile : baseline réinitialisée après reset/reboot\n")
            }
            append("\nWHY : ${vaultDecision.why}\n")
            append("WHAT : ${vaultDecision.what.name}\n")
            append("RESULT : ${vaultDecision.result}\n\n")
            append(
                "Règle anti-faux-positif : un échec de serveur de test ne peut jamais " +
                    "dégrader seul un réseau déjà VALIDATED par Android."
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
        if (!snapshot.supported) return "Data mobile · compteur Android indisponible"

        val used = String.format(Locale.US, "%.1f", snapshot.usedTodayMiB)
        val limit = snapshot.dailyLimitMiB
        return if (limit == null) {
            "Data mobile · $used MiB observés · seuil OFF"
        } else {
            "Data mobile · $used / $limit MiB observés"
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
