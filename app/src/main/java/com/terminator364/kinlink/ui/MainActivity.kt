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
import com.terminator364.kinlink.core.NetworkTruth
import com.terminator364.kinlink.core.WifiDoctor
import com.terminator364.kinlink.core.WifiOptimizer
import com.terminator364.kinlink.data.DiagnosticExporter
import com.terminator364.kinlink.data.StabilityWindow
import com.terminator364.kinlink.data.TelemetryLedger
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var observer: NetworkObserver
    private lateinit var ledger: TelemetryLedger
    private lateinit var mobileBudget: MobileBudgetTracker
    private lateinit var profileStore: AutopilotProfileStore

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

    private var latestTruth = NetworkTruth()
    private var currentProfile = AutopilotProfile.BALANCED
    private var latestBudget = MobileBudgetSnapshot(
        supported = false,
        usedTodayBytes = 0L,
        dailyLimitBytes = null,
        state = BudgetState.BALANCE_UNKNOWN
    )
    private var latestStability: StabilityWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextCompat.startForegroundService(this, Intent(this, KinlinkObserverService::class.java))
        setContentView(R.layout.activity_main)

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

        installSystemBarInsets()

        ledger = TelemetryLedger(this)
        mobileBudget = MobileBudgetTracker(this)
        profileStore = AutopilotProfileStore(this)
        currentProfile = profileStore.current()
        refreshProfileButton()

        technicalToggle.setOnClickListener { toggleTechnicalDetails() }
        diagnosticExport.setOnClickListener { exportDiagnostic() }
        wifiDoctorButton.setOnClickListener { optimizeWifi() }
        budgetButton.setOnClickListener { configureMobileBudget() }
        profileButton.setOnClickListener {
            currentProfile = profileStore.cycle()
            refreshProfileButton()
            render(latestTruth, latestBudget, latestStability)
        }

        observer = NetworkObserver(this) { rawTruth ->
            val budget = mobileBudget.sample()
            val enrichedTruth = rawTruth.copy(budgetState = budget.state)
            val stability = runCatching { ledger.stabilityWindow() }.getOrNull()

            runOnUiThread {
                latestBudget = budget
                latestStability = stability
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
                "Plafond de garde optionnel en MiB par jour. " +
                    "KINLINK ne lance jamais de speedtest mobile, même sans plafond."
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
        wifiDoctorButton.isEnabled = false
        adviceTitleText.text = "Analyse de résilience Wi-Fi"
        adviceText.text =
            "Arbitrage Android + micro-tests bornés. Aucune donnée mobile n’est utilisée."

        Thread {
            val result = WifiOptimizer(this).optimize()
            runOnUiThread {
                wifiDoctorButton.isEnabled = true
                adviceTitleText.text = when {
                    result.success && result.androidValidated -> "Wi-Fi validé"
                    result.success -> "Wi-Fi confirmé"
                    else -> "Réévaluation Wi-Fi demandée"
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
            DiagnosticExporter(this).share(ledger.diagnosticSummary(latestTruth))
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
        val adaptiveDecision = AdaptivePolicyEngine.evaluate(
            truth = truth,
            instabilityScore = stability?.assessment?.score ?: 0,
            profile = currentProfile
        )

        stateText.text = assessment.headline
        heroDetailText.text = assessment.explanation
        transportText.text = "Connexion en cours · ${transportLabel(truth)}"
        internetText.text = "Internet · ${internetLabel(truth)}"
        mobileText.text = when (truth.budgetState) {
            BudgetState.BUNDLE_EXHAUSTED -> "Données mobiles · plafond KINLINK atteint"
            BudgetState.BUNDLE_LOW -> "Données mobiles · plafond bientôt atteint"
            else -> if (assessment.avoidAutomaticMobileUse) {
                "Données mobiles · protégées"
            } else {
                "Données mobiles · politique active"
            }
        }
        mobileBudgetText.text = mobileBudgetLabel(budget)

        adviceTitleText.text = doctorAdvice.title
        adviceText.text = "${doctorAdvice.message}\n\n${vaultDecision.why} · ${vaultDecision.result}\n\nAutopilot ${profileLabel(currentProfile)} : ${adaptiveDecision.reason}"

        detailText.text = buildString {
            append("État KINLINK : ${assessment.state.name}\n")
            append("Profil Autopilot : ${currentProfile.name}\n")
            append("Réseau local : ${lanLabel(truth)}\n")
            append("Contexte : ${contextLabel(truth)}\n")
            append("Diagnostic : ${failureLabel(truth)}\n")
            append("Confiance Android : ${(truth.confidence * 100).toInt()} %\n")
            append("Budget mobile : ${truth.budgetState.name}\n")
            append("Autopilot : ${adaptiveDecision.intent.name}\n")
            stability?.let {
                append("Instabilité 15 min : ${it.assessment.score}/100")
                append(" · ${it.transitions} transition(s)")
                if (it.assessment.flapping) append(" · FLAPPING")
                append("\n")
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
        if (!snapshot.supported) return "Suivi data mobile · indisponible sur cet appareil"

        val used = String.format(Locale.US, "%.1f", snapshot.usedTodayMiB)
        val limit = snapshot.dailyLimitMiB
        return if (limit == null) {
            "Suivi KINLINK · $used MiB aujourd’hui · plafond non configuré"
        } else {
            "Suivi KINLINK · $used / $limit MiB aujourd’hui"
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
