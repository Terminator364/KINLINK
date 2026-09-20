package com.terminator364.kinlink.ui

import android.app.Activity
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stateText = findViewById(R.id.stateText)
        transportText = findViewById(R.id.transportText)
        internetText = findViewById(R.id.internetText)
        mobileText = findViewById(R.id.mobileText)
        detailText = findViewById(R.id.detailText)
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
            t.internetState.name == "VALIDATED" -> "AUTOPILOT · INTERNET DISPONIBLE"
            t.lanState.name == "LINK_PRESENT" -> "LAN PRÉSENT · INTERNET INDISPONIBLE"
            t.internetState.name == "OFFLINE" -> "HORS LIGNE"
            else -> "RÉSEAU À VÉRIFIER"
        }
        transportText.text = "Connexion : ${t.transport.name.replace('_', ' ')}"
        internetText.text = "Internet : ${t.internetState.name.replace('_', ' ')}"
        mobileText.text = if (t.metered) "Données : réseau facturé · protection à configurer" else "Données : réseau non facturé"
        detailText.text = buildString {
            append("Contexte : ${t.context.name}\n")
            append("LAN : ${t.lanState.name}\n")
            append("Interface : ${t.interfaceName ?: "—"}\n")
            append("Passerelle : ${t.gateway ?: "—"}\n")
            append("Diagnostic initial : ${t.failureDomain.name}\n")
            append("Confiance : ${(t.confidence * 100).toInt()} %")
        }
    }
}
