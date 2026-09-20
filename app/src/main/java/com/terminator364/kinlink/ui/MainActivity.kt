package com.terminator364.kinlink.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import com.terminator364.kinlink.core.KinlinkObserverService
import com.terminator364.kinlink.core.MobileBudgetSnapshot
import com.terminator364.kinlink.core.MobileBudgetTracker
import com.terminator364.kinlink.core.MobileVault
import com.terminator364.kinlink.core.NetworkObserver
import com.terminator364.kinlink.core.PassiveLinkQuality
import com.terminator364.kinlink.core.PassiveLinkQualityPolicy
import com.terminator364.kinlink.core.PassiveProblemClassifier
import com.terminator364.kinlink.core.PassiveGuidancePolicy
import com.terminator364.kinlink.core.RecoveryBlockReason
import com.terminator364.kinlink.core.RecoveryMode
import com.terminator364.kinlink.core.RecoveryModeStore
import com.terminator364.kinlink.core.RecentReliabilityPolicy
import com.terminator364.kinlink.core.SessionHealthPolicy
import com.terminator364.kinlink.core.NetworkTruth
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
    private lateinit var detailText: TextView
    private lateinit var heroDetailText: TextView
    private lateinit var adviceTitleText: TextView
    private lateinit var adviceText: TextView
    private lateinit var technicalToggle: TextView
    private lateinit var diagnosticExport: TextView
    private lateinit var wifiDoctorButton: TextView
    private lateinit var budgetButton: TextView
    private lateinit var profileButton: TextView
    private lateinit var safeModeButton: TextView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextCompat.startForegroundService(this, Intent(this, KinlinkObserverService::class.java))
        setContentView(R.layout.activity_main)

        heroEyebrow = findViewById(R.id.heroEyebrow)
        stateText = findViewById(R.id.stateText)
        transportText = findViewById(R.id.transportText)
        internetText = findViewById(R.id.internetText)
        mobileText = findViewById(R.id.mobileText)
        mobileBudgetText = findViewById(R.id.mobileBudgetText)
        detailText = findViewById(R.id.detailText)
        heroDetailText = findViewById(R.id.heroDetailText)
        adviceTitleText = findViewById(R.id.adviceTitleText)
        adviceText = findViewById(R.id.adviceText)
        technicalToggle = findViewById(R.id.technicalToggle)
        diagnosticExport = findViewById(R.id.diagnosticExport)
        wifiDoctorButton = findViewById(R.id.wifiDoctorButton)
        budgetButton = findViewById(R.id.budgetButton)
        profileButton = findViewById(R.id.profileButton)
        safeModeButton = findViewById(R.id.safeModeButton)
        val installedVersion = runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull() ?: "?"
        findViewById<TextView>(R.id.versionText).text = "KINLINK $installedVersion"

        installSystemBarInsets()

        ledger = TelemetryLedger(this)
        mobileBudget = MobileBudgetTracker(this)
        profileStore = AutopilotProfileStore(this)
        recoveryModeStore = RecoveryModeStore(this)
        currentProfile = profileStore.current()
        refreshProfileButton()
        refreshSafeModeButton()

        technicalToggle.setOnClickListener { toggleTechnicalDetails() }
        diagnosticExport.setOnClickListener { exportDiagnostic() }
        wifiDoctorButton.setOnClickListener { optimizeWifi() }
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

    private fun refreshProfileButton() {
        profileButton.text = "Profil Autopilot · ${profileLabel(currentProfile)}"
    }

    private fun refreshSafeModeButton() {
        val observationOnly = recoveryModeStore.current() == RecoveryMode.OBSERVATION_ONLY
        safeModeButton.text = if (observationOnly) {
            "Mode sûr ACTIF · Observation uniquement"
        } else {
            "Mode sûr · Basculer en observation uniquement"
        }
    }

    private fun profileLabel(profile: AutopilotProfile): String = when (profile) {
        AutopilotProfile.CONSERVATIVE -> "Conservateur"
        AutopilotProfile.BALANCED -> "Équilibré"
        AutopilotProfile.MAXIMUM_STABILITY -> "Stabilité max"
    }

    private fun toggleTechnicalDetails() {
        val nowVisible = detailText.visibility == View.VISIBLE
        detailText.visibility = if (nowVisible) View.GONE else View.VISIBLE
        diagnosticExport.visibility = if (nowVisible) View.GONE else View.VISIBLE
        technicalToggle.text =
            if (nowVisible) "Voir les détails techniques" else "Masquer les détails techniques"
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
            runCatching { ledger.appendAction("WIFI_OPTIMIZE", result.success, result.summary) }
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
                    "\nMicro-tests tentés : ${result.probeAttempts}" +
                    "\nSignal Android : ${if (result.frameworkHintSent) "envoyé" else "non nécessaire"}" +
                    "\nMétriques réseau : ${if (result.bandwidthRefreshRequested) "rafraîchissement demandé" else "inchangées"}"
            }
        }.start()
    }

    private fun exportDiagnostic() {
        runCatching {
            DiagnosticExporter(this).share(
                ledger.diagnosticSummary(latestTruth, recoveryModeStore.current().name)
            )
        }.onFailure {
            Toast.makeText(this, "Impossible de préparer le diagnostic", Toast.LENGTH_SHORT).show()
        }
    }

    private fun render(
        truth: NetworkTruth,
        budget: MobileBudgetSnapshot = latestBudget,
        stability: StabilityWindow? = latestStability
    ) {
        latestTruth = truth
        val assessment = ConnectivityStateClassifier.classify(truth)
        val vaultDecision = MobileVault.decide(truth, assessment)
        val doctorAdvice = WifiDoctor.advise(assessment)
        val passiveQuality = PassiveLinkQualityPolicy.assess(truth)
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

        val observationOnly = recoveryModeStore.current() == RecoveryMode.OBSERVATION_ONLY
        heroEyebrow.text = if (observationOnly) "MODE SÛR" else "AUTOPILOT"
        stateText.text = if (observationOnly) "Observation uniquement" else assessment.headline
        heroDetailText.text = if (observationOnly) {
            "Aucune récupération active. Android garde entièrement le contrôle du réseau."
        } else assessment.explanation
        transportText.text = "Connexion en cours · ${transportLabel(truth)}"
        internetText.text = "Internet · ${internetLabel(truth)}"
        mobileText.text = when (truth.budgetState) {
            BudgetState.BUNDLE_EXHAUSTED -> "Données mobiles · seuil de prudence KINLINK atteint"
            BudgetState.BUNDLE_LOW -> "Données mobiles · seuil de prudence bientôt atteint"
            else -> when (truth.transport.name) {
                "CELLULAR" -> "Données mobiles · Android contrôle · KINLINK observe seulement"
                else -> if (assessment.avoidAutomaticMobileUse) {
                    "Données mobiles · protégées"
                } else {
                    "Données mobiles · aucune prise de contrôle KINLINK"
                }
            }
        }
        mobileBudgetText.text = mobileBudgetLabel(budget)

        if (observationOnly) {
            adviceTitleText.text = "Mode sûr actif"
            adviceText.text = "KINLINK observe et journalise seulement. Aucune optimisation ni récupération active n’est exécutée."
        } else {
            val qualityNotice = when (passiveQuality.quality) {
                PassiveLinkQuality.CONSTRAINED -> "\n\nQualité passive : capacité Android très limitée."
                PassiveLinkQuality.LIMITED -> "\n\nQualité passive : capacité Android limitée."
                else -> ""
            }
            adviceTitleText.text = passiveGuidance.title
            adviceText.text = "${passiveGuidance.message}\n\n${doctorAdvice.message}\n\n${vaultDecision.why} · ${vaultDecision.result}\n\nAutopilot ${profileLabel(currentProfile)} : ${adaptiveDecision.reason}${qualityNotice}"
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
            append("Cause passive : ${passiveProblem.cause.name} · confiance ${passiveProblem.confidence}%\n")
            append("Cause passive détail : ${passiveProblem.summary}\n")
            append("Santé de session : ${sessionHealth.health.name} · ${sessionHealth.summary}\n")
            append("DNS Android : ${truth.dnsServerCount} serveur(s) · DNS privé ${if (truth.privateDnsActive) "actif" else "non signalé"}\n")
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
                    reliability.longestMillis
                )
                append("Interruptions 24 h : ${reliability.interruptionCount}")
                append(" · cumul ${reliability.cumulativeMillis} ms")
                append(" · max ${reliability.longestMillis} ms")
                append(" · ${burden.name}\n")
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
}
