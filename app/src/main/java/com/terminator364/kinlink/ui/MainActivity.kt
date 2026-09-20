package com.terminator364.kinlink.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.ScrollView
import android.widget.TextView
import com.terminator364.kinlink.R
import com.terminator364.kinlink.core.NetworkObserver
import com.terminator364.kinlink.core.NetworkTruth
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        installSystemBarInsets()
        technicalToggle.setOnClickListener {
            val nowVisible = detailText.visibility == View.VISIBLE
            detailText.visibility = if (nowVisible) View.GONE else View.VISIBLE
            technicalToggle.text = if (nowVisible) "Voir les détails techniques" else "Masquer les détails techniques"
        }
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

    private fun render(t: NetworkTruth) {
        stateText.text = when {
            t.internetState.name == "VALIDATED" -> "Internet disponible"
            t.lanState.name == "LINK_PRESENT" -> "Internet indisponible"
            t.internetState.name == "OFFLINE" -> "Vous êtes hors ligne"
            else -> "Vérification en cours"
        }
        heroDetailText.text = when {
            t.internetState.name == "VALIDATED" -> "Tout fonctionne normalement. KINLINK veille discrètement."
            t.lanState.name == "LINK_PRESENT" -> "Votre réseau local est préservé. KINLINK ne force aucun basculement."
            else -> "KINLINK observe sans consommer de données de test."
        }
        transportText.text = "Connexion en cours · ${transportLabel(t)}"
        internetText.text = "Internet · ${internetLabel(t)}"
        mobileText.text = when {
            t.transport.name == "WIFI" -> "Données mobiles · préservées"
            t.metered -> "Données mobiles · utilisées selon votre forfait"
            else -> "Données mobiles · non utilisées"
        }
        adviceTitleText.text = when {
            t.internetState.name == "VALIDATED" -> "Aucune action nécessaire"
            t.lanState.name == "LINK_PRESENT" -> "Votre réseau local reste disponible"
            t.internetState.name == "OFFLINE" -> "Connexion indisponible"
            else -> "KINLINK vérifie la situation"
        }
        adviceText.text = when {
            t.internetState.name == "VALIDATED" -> "KINLINK reste en observation et protège vos données mobiles."
            t.lanState.name == "LINK_PRESENT" -> "Les fonctions locales peuvent continuer à fonctionner même sans Internet."
            t.internetState.name == "OFFLINE" -> "Aucune donnée mobile ne sera utilisée automatiquement pour forcer une connexion."
            else -> "Aucune action automatique n’est lancée tant que la situation n’est pas claire."
        }
        detailText.text = buildString {
            append("Réseau local : ${lanLabel(t)}\n")
            append("Contexte : ${contextLabel(t)}\n")
            append("Interface : ${t.interfaceName ?: "Non disponible"}\n")
            append("Passerelle locale : ${t.gateway ?: "Non disponible"}\n")
            append("Diagnostic : ${failureLabel(t)}\n")
            append("Confiance : ${(t.confidence * 100).toInt()} %")
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
