package com.terminator364.kinlink.ui

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import androidx.core.content.ContextCompat
import android.view.View
import android.view.WindowInsets
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.terminator364.kinlink.R
import com.terminator364.kinlink.core.ConnectivityStateClassifier
import com.terminator364.kinlink.core.NetworkObserver
import com.terminator364.kinlink.core.KinlinkObserverService
import com.terminator364.kinlink.core.NetworkTruth
import com.terminator364.kinlink.core.MobileVault
import com.terminator364.kinlink.core.WifiDoctor
import com.terminator364.kinlink.core.WifiDoctorProbe
import com.terminator364.kinlink.data.DiagnosticExporter
import com.terminator364.kinlink.data.TelemetryLedger

class MainActivity : Activity() {
    private lateinit var observer: NetworkObserver
    private lateinit var ledger: TelemetryLedger
    private lateinit var stateText: TextView
    private lateinit var transportText: TextView
    private lateinit var internetText: TextView
    private lateinit var mobileText: TextView
    private lateinit var detailText: TextView
    private lateinit var heroDetailText: TextView
    private lateinit var adviceTitleText: TextView
    private lateinit var adviceText: TextView
    private lateinit var technicalToggle: TextView
    private lateinit var diagnosticExport: TextView
    private lateinit var wifiDoctorButton: TextView
    private var latestTruth = NetworkTruth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextCompat.startForegroundService(this, Intent(this, KinlinkObserverService::class.java))
        setContentView(R.layout.activity_main)
        stateText = findViewById(R.id.stateText)
        transportText = findViewById(R.id.transportText)
        internetText = findViewById(R.id.internetText)
        mobileText = findViewById(R.id.mobileText)
        detailText = findViewById(R.id.detailText)
        heroDetailText = findViewById(R.id.heroDetailText)
        adviceTitleText = findViewById(R.id.adviceTitleText)
        adviceText = findViewById(R.id.adviceText)
        technicalToggle = findViewById(R.id.technicalToggle)
        diagnosticExport = findViewById(R.id.diagnosticExport)
        wifiDoctorButton = findViewById(R.id.wifiDoctorButton)
        installSystemBarInsets()
        technicalToggle.setOnClickListener { toggleTechnicalDetails() }
        diagnosticExport.setOnClickListener { exportDiagnostic() }
        wifiDoctorButton.setOnClickListener { runWifiDoctor() }
        ledger = TelemetryLedger(this)
        observer = NetworkObserver(this) { truth ->
            ledger.append(truth)
            runOnUiThread { render(truth) }
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

    private fun toggleTechnicalDetails() {
        val nowVisible = detailText.visibility == View.VISIBLE
        detailText.visibility = if (nowVisible) View.GONE else View.VISIBLE
        diagnosticExport.visibility = if (nowVisible) View.GONE else View.VISIBLE
        wifiDoctorButton.visibility = if (nowVisible) View.GONE else View.VISIBLE
        technicalToggle.text = if (nowVisible) "Voir les détails techniques" else "Masquer les détails techniques"
    }

    private fun runWifiDoctor() {
        wifiDoctorButton.isEnabled = false
        Thread {
            val result = WifiDoctorProbe(this).run()
            runOnUiThread {
                wifiDoctorButton.isEnabled = true
                adviceTitleText.text = if (result.success) "Wi-Fi Doctor : réponse reçue" else "Wi-Fi Doctor : problème détecté"
                adviceText.text = result.summary
                detailText.text = detailText.text.toString() + "\n\nTest Wi-Fi explicite : ${result.summary}"
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

    private fun render(truth: NetworkTruth) {
        latestTruth = truth
        val assessment = ConnectivityStateClassifier.classify(truth)
        val vaultDecision = MobileVault.decide(truth, assessment)
        val doctorAdvice = WifiDoctor.advise(assessment)
        stateText.text = assessment.headline
        heroDetailText.text = assessment.explanation
        transportText.text = "Connexion en cours · ${transportLabel(truth)}"
        internetText.text = "Internet · ${internetLabel(truth)}"
        mobileText.text = if (assessment.avoidAutomaticMobileUse) {
            "Données mobiles · protégées"
        } else {
            "Données mobiles · politique active"
        }
        adviceTitleText.text = doctorAdvice.title
        adviceText.text = "${doctorAdvice.message}\n\n${vaultDecision.why} · ${vaultDecision.result}"
        detailText.text = buildString {
            append("État KINLINK : ${assessment.state.name}\n")
            append("Réseau local : ${lanLabel(truth)}\n")
            append("Contexte : ${contextLabel(truth)}\n")
            append("Diagnostic : ${failureLabel(truth)}\n")
            append("Confiance : ${(truth.confidence * 100).toInt()} %\n\n")
            append("WHY : ${vaultDecision.why}\n")
            append("WHAT : ${vaultDecision.what.name}\n")
            append("RESULT : ${vaultDecision.result}\n\n")
            append("Exporter le diagnostic ne lance aucun speedtest, probe mobile ou changement réseau.")
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
        "ISP" -> "Accès Internet indisponible"
        "ROUTER" -> "Connexion au routeur requise"
        else -> "En cours d’analyse"
    }
}
