package com.terminator364.kinlink.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.terminator364.kinlink.R
import com.terminator364.kinlink.core.AutopilotProfile
import com.terminator364.kinlink.core.AutopilotProfileStore
import com.terminator364.kinlink.core.KinlinkObserverService
import com.terminator364.kinlink.core.MobilePlanVaultStore
import com.terminator364.kinlink.core.MobileUsageEvidenceStore
import com.terminator364.kinlink.core.MobileVaultFormPolicy
import com.terminator364.kinlink.core.MobileVaultOfflineFallbackPolicy
import com.terminator364.kinlink.core.MobileVaultZone
import com.terminator364.kinlink.core.PlatformCapabilityDiscovery
import com.terminator364.kinlink.core.DataSaverState
import com.terminator364.kinlink.core.LocalNetworkAccessState
import com.terminator364.kinlink.core.RecoveryMode
import com.terminator364.kinlink.core.RecoveryModeStore
import com.terminator364.kinlink.core.TrustedWifiContextStore
import com.terminator364.kinlink.data.TelemetryLedger
import java.util.Locale

class ProductSurfaceActivity : Activity() {
    private lateinit var eyebrow: TextView
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var body: TextView
    private lateinit var contextAction: TextView
    private lateinit var primaryAction: TextView
    private lateinit var secondaryAction: TextView
    private lateinit var dangerAction: TextView
    private lateinit var backAction: TextView
    private lateinit var profileStore: AutopilotProfileStore
    private lateinit var recoveryModeStore: RecoveryModeStore
    private lateinit var trustedWifiStore: TrustedWifiContextStore
    private lateinit var ledger: TelemetryLedger
    private var surface: String = SURFACE_DIAGNOSTIC
    private var wifiDigest: String? = null
    private var wifiLabel: String = "Wi-Fi de confiance · non disponible"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_surface)
        eyebrow = findViewById(R.id.productSurfaceEyebrow)
        title = findViewById(R.id.productSurfaceTitle)
        subtitle = findViewById(R.id.productSurfaceSubtitle)
        body = findViewById(R.id.productSurfaceBody)
        contextAction = findViewById(R.id.productSurfaceContextAction)
        primaryAction = findViewById(R.id.productSurfacePrimaryAction)
        secondaryAction = findViewById(R.id.productSurfaceSecondaryAction)
        dangerAction = findViewById(R.id.productSurfaceDangerAction)
        backAction = findViewById(R.id.productSurfaceBackAction)
        profileStore = AutopilotProfileStore(this)
        recoveryModeStore = RecoveryModeStore(this)
        trustedWifiStore = TrustedWifiContextStore(this)
        ledger = TelemetryLedger(this)
        surface = intent.getStringExtra(EXTRA_SURFACE) ?: SURFACE_DIAGNOSTIC
        wifiDigest = intent.getStringExtra(EXTRA_WIFI_DIGEST)
        wifiLabel = intent.getStringExtra(EXTRA_WIFI_LABEL) ?: wifiLabel
        installSystemBarInsets()
        backAction.setOnClickListener { finish() }
        renderSurface()
    }

    override fun onDestroy() {
        ledger.close()
        super.onDestroy()
    }

    private fun renderSurface() {
        hideActions()
        when (surface) {
            SURFACE_DIAGNOSTIC -> renderDiagnostic()
            SURFACE_WEEK -> renderWeek()
            SURFACE_MOBILE_VAULT -> renderMobileVault()
            SURFACE_SETTINGS -> renderSettings()
            else -> renderDiagnostic()
        }
    }

    private fun renderDiagnostic() {
        eyebrow.text = "DIAGNOSTIC"
        title.text = "Où se situe le problème ?"
        subtitle.text = "Lecture locale, sans speed-test automatique ni consommation inutile de données."
        body.text = intent.getStringExtra(EXTRA_DIAGNOSTIC_BODY)
            ?: "Aucun état réseau récent n’est disponible."

        when (intent.getStringExtra(EXTRA_DIAGNOSTIC_ACTION)) {
            DIAGNOSTIC_ACTION_WIFI ->
                primaryAction.visibleAction("Agir · lancer Wi-Fi Doctor") {
                    returnToMain(MainActivity.ACTION_RUN_WIFI_DOCTOR)
                }
            DIAGNOSTIC_ACTION_MOBILE ->
                primaryAction.visibleAction("Agir · lancer Mobile Assist") {
                    returnToMain(MainActivity.ACTION_RUN_MOBILE_ASSIST)
                }
            else ->
                primaryAction.visibleAction("Actualiser depuis l’accueil") {
                    finish()
                }
        }
        secondaryAction.visibleAction("Exporter le diagnostic") {
            returnToMain(MainActivity.ACTION_EXPORT_DIAGNOSTIC)
        }
    }

    private fun renderWeek() {
        eyebrow.text = "CETTE SEMAINE"
        title.text = "Ce que KINLINK a réellement observé"
        subtitle.text = "Résumé local sur 7 jours. Ce sont des observations, pas des promesses de débit."
        body.text = weeklySummary()
        primaryAction.visibleAction("Actualiser le résumé") {
            body.text = weeklySummary()
        }
        secondaryAction.visibleAction("Partager ce rapport") {
            shareWeeklySummary()
        }
    }

    private fun renderMobileVault() {
        eyebrow.text = "MOBILE VAULT"
        title.text = "Ton forfait, tes réserves"
        subtitle.text = "Le forfait local reste utilisable même sans opérateur connecté, sans cloud et sans permission téléphonie."
        body.text = mobileVaultSummary()
        primaryAction.visibleAction("Modifier le forfait") {
            returnToMain(MainActivity.ACTION_OPEN_MOBILE_VAULT)
        }
        secondaryAction.visibleAction("Actualiser") { body.text = mobileVaultSummary() }
    }

    private fun renderSettings() {
        eyebrow.text = "RÉGLAGES"
        title.text = "Simple, local, réversible"
        subtitle.text = "Les réglages importants restent visibles et l’urgence rend immédiatement la main au réseau natif Android."
        body.text = settingsSummary()

        wifiDigest?.let { digest ->
            contextAction.visibleAction(
                if (trustedWifiStore.isHome(digest)) "Oublier ce Wi-Fi maison"
                else "Marquer ce Wi-Fi comme maison"
            ) {
                if (trustedWifiStore.isHome(digest)) {
                    trustedWifiStore.forget(digest)
                    runCatching { ledger.appendAction("TRUSTED_WIFI_HOME_REMOVED", true, "Contexte Wi-Fi maison oublié; aucune identité réseau brute n’était persistée.") }
                } else {
                    trustedWifiStore.markHome(digest)
                    runCatching { ledger.appendAction("TRUSTED_WIFI_HOME_MARKED", true, "Contexte Wi-Fi maison mémorisé sous empreinte locale SHA-256; aucune identité réseau brute persistée.") }
                }
                renderSettings()
            }
        }

        primaryAction.visibleAction("Autopilot · ${profileLabel(profileStore.current())} · changer") {
            val next = profileStore.cycle()
            runCatching { ledger.appendAction("AUTOPILOT_PROFILE_${next.name}", true, "Mode Autopilot changé depuis Réglages.") }
            notifyServiceModeChanged()
            renderSettings()
        }

        secondaryAction.visibleAction(
            if (recoveryModeStore.current() == RecoveryMode.OBSERVATION_ONLY)
                "Mode sûr · ON · réactiver l’Autopilot"
            else
                "Mode sûr · OFF · passer en observation"
        ) {
            val next = recoveryModeStore.toggle()
            runCatching { ledger.appendAction("RECOVERY_MODE_${next.name}", true, "Mode de récupération changé depuis Réglages.") }
            notifyServiceModeChanged()
            renderSettings()
        }

        dangerAction.visibleAction("Urgence · couper les actions KINLINK") {
            recoveryModeStore.set(RecoveryMode.OBSERVATION_ONLY)
            runCatching {
                ledger.appendAction(
                    "EMERGENCY_NATIVE_NETWORK_ESCAPE",
                    true,
                    "Actions actives KINLINK suspendues; Android garde le contrôle du réseau natif."
                )
            }
            notifyServiceModeChanged()
            renderSettings()
        }
    }

    private fun mobileVaultSummary(): String {
        val stored = MobilePlanVaultStore(this).read()
            ?: return "Aucun forfait local configuré.\n\nTu peux configurer le total, la consommation connue, le début du cycle, l’expiration et les réserves."
        val evidence = MobileUsageEvidenceStore(this).read()
        val decision = MobileVaultOfflineFallbackPolicy.evaluate(
            config = stored.config,
            userReconciledUsedBytes = evidence?.userReconciledUsedBytes,
            observedAtEpochMillis = evidence?.userReconciledObservedAtEpochMillis,
            nowEpochMillis = System.currentTimeMillis()
        )
        val config = stored.config
        val assessment = decision.assessment
        return buildString {
            append("Forfait total · ${formatBytes(config.totalBytes)}\n")
            append("Consommation saisie · " + (evidence?.userReconciledUsedBytes?.let(::formatBytes) ?: "inconnue"))
            append("\nDébut du cycle · " + MobileVaultFormPolicy.formatCycleStartDate(config.cycleStartAtEpochMillis).ifBlank { "non renseigné" })
            append("\nExpiration · " + MobileVaultFormPolicy.formatInclusiveExpiryDate(config.expiryAtEpochMillis).ifBlank { "non renseignée" })
            append("\n\nRéserve protégée · ${formatBytes(config.protectedReserveBytes)}")
            append("\nSecours · ${formatBytes(config.rescueAllowanceBytes)}")
            append("\nCritique interactif · ${formatBytes(config.criticalInteractiveAllowanceBytes)}")
            append("\n\nÉtat · ${vaultZoneLabel(assessment?.zone)}")
            append("\nReste estimable · " + (assessment?.remainingBytes?.let(::formatBytes) ?: "inconnu"))
            append("\n\nRègle de sécurité · si la consommation exacte n’est pas connue, KINLINK n’invente pas un solde et bloque les dépenses autonomes.")
        }
    }

    private fun weeklySummary(): String {
        val since = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        val interruptions = runCatching { ledger.interruptionDurationStatsSince(since) }.getOrDefault(Triple(0, 0L, 0L))
        val wifiSlow = runCatching { ledger.lowQualityDurationStatsSince(since) }.getOrDefault(Triple(0, 0L, 0L))
        val mobileSlow = runCatching { ledger.mobileLowQualityDurationStatsSince(since) }.getOrDefault(Triple(0, 0L, 0L))
        val incidents = runCatching { ledger.countActionsSince("USER_INCIDENT_MARKER", since) }.getOrDefault(0)
        val recoveries = runCatching { ledger.countActionsSince("RECOVERY_", since) }.getOrDefault(0)
        val assists = runCatching { ledger.countActionsSince("MOBILE_ASSIST_", since) }.getOrDefault(0)
        val recent = runCatching { ledger.recentActionsSince(since, 5) }.getOrDefault(emptyList())
        return buildString {
            append("Coupures · ${interruptions.first}\n")
            append("Temps cumulé de coupure · ${formatDuration(interruptions.second)}\n")
            append("Plus longue coupure · ${formatDuration(interruptions.third)}\n\n")
            append("Wi-Fi lent · ${wifiSlow.first} épisode(s) · ${formatDuration(wifiSlow.second)}\n")
            append("Mobile lent · ${mobileSlow.first} épisode(s) · ${formatDuration(mobileSlow.second)}\n")
            append("Problèmes signalés par toi · $incidents\n\n")
            append("Actions de récupération enregistrées · $recoveries\n")
            append("Événements Mobile Assist · $assists\n\n")
            if (recent.isEmpty()) append("Activité récente · aucune action notable enregistrée cette semaine.")
            else {
                append("Activité récente\n")
                recent.forEach {
                    append("• ${humanAction(it.action)}${if (it.success) " · terminé" else " · non confirmé"}\n")
                }
            }
        }
    }

    private fun settingsSummary(): String = buildString {
        append("Autopilot · ${profileLabel(profileStore.current())}\n")
        append("Mode sûr · " + if (recoveryModeStore.current() == RecoveryMode.OBSERVATION_ONLY)
            "ON — KINLINK observe mais ne lance pas d’action active"
        else "OFF — récupération bornée autorisée")
        append("\n${currentWifiLabel()}\n\n")
        append("Confidentialité · l’identité brute du Wi-Fi n’est pas persistée. La mémoire maison utilise uniquement une empreinte SHA-256 locale et ne peut pas autoriser une récupération active.")
        append("\n\nUrgence · suspend toutes les actions KINLINK sans prendre le contrôle du routage : Android continue d’utiliser son réseau natif.")
        append("\n\n")
        append(capabilitySummary())
    }

    private fun capabilitySummary(): String {
        val capability = runCatching {
            PlatformCapabilityDiscovery(this).snapshot()
        }.getOrNull() ?: return "Capacités appareil · lecture indisponible"

        val dataSaver = when (capability.dataSaver) {
            DataSaverState.DISABLED -> "désactivé"
            DataSaverState.WHITELISTED -> "KINLINK autorisé"
            DataSaverState.ENABLED -> "actif"
            DataSaverState.UNKNOWN -> "état inconnu"
        }
        val lan = when (capability.localNetworkAccess) {
            LocalNetworkAccessState.NOT_REQUIRED_PRE_API_37 ->
                "permission LAN spéciale non requise sur cet Android"
            LocalNetworkAccessState.GRANTED ->
                "accès LAN accordé"
            LocalNetworkAccessState.NOT_GRANTED ->
                "accès LAN non accordé"
        }
        return buildString {
            append("Capacités appareil · Android API ${capability.apiLevel}")
            append("\nWi-Fi · ${if (capability.wifiFeature) "disponible" else "absent"}")
            append("\nTéléphonie · ${if (capability.telephonyFeature) "disponible" else "absente"}")
            append("\nÉconomiseur de données · $dataSaver")
            append("\nUsage Access · ${if (capability.networkUsageAccess) "accordé (optionnel)" else "non accordé (optionnel)"}")
            append("\nLAN · $lan")
            append("\nDiagnostic Android avancé · non revendiqué en mode Lite")
        }
    }

    private fun shareWeeklySummary() {
        val report = weeklySummary()
        runCatching {
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Rapport KINLINK · Cette semaine"
                        )
                        putExtra(Intent.EXTRA_TEXT, report)
                    },
                    "Partager le rapport KINLINK"
                )
            )
        }
    }

    private fun currentWifiLabel(): String {
        val digest = wifiDigest ?: return wifiLabel
        return when {
            trustedWifiStore.isHome(digest) -> "Wi-Fi maison reconnu localement"
            trustedWifiStore.isTrusted(digest) -> "Wi-Fi de confiance reconnu localement"
            else -> "Wi-Fi actuel non mémorisé"
        }
    }

    private fun notifyServiceModeChanged() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, KinlinkObserverService::class.java).apply {
                action = KinlinkObserverService.ACTION_REFRESH_MODE
            }
        )
    }

    private fun returnToMain(action: String) {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(MainActivity.EXTRA_PRODUCT_ACTION, action)
            }
        )
        finish()
    }

    private fun hideActions() {
        for (view in arrayOf(contextAction, primaryAction, secondaryAction, dangerAction)) {
            view.visibility = View.GONE
            view.setOnClickListener(null)
        }
    }

    private fun TextView.visibleAction(label: String, action: () -> Unit) {
        text = label
        visibility = View.VISIBLE
        setOnClickListener { action() }
    }

    private fun profileLabel(profile: AutopilotProfile): String = when (profile) {
        AutopilotProfile.CONSERVATIVE -> "Prudent"
        AutopilotProfile.BALANCED -> "Stable"
        AutopilotProfile.MAXIMUM_STABILITY -> "Max"
    }

    private fun vaultZoneLabel(zone: MobileVaultZone?): String = when (zone) {
        MobileVaultZone.NORMAL -> "zone normale"
        MobileVaultZone.PROTECTED_RESERVE -> "réserve protégée"
        MobileVaultZone.RESCUE_ONLY -> "secours uniquement"
        MobileVaultZone.CRITICAL_INTERACTIVE_ONLY -> "critique interactif uniquement"
        MobileVaultZone.EXHAUSTED -> "forfait épuisé"
        MobileVaultZone.EXPIRED -> "forfait expiré"
        MobileVaultZone.UNKNOWN, null -> "consommation à réconcilier"
    }

    private fun formatBytes(bytes: Long): String {
        val safe = bytes.coerceAtLeast(0L).toDouble()
        return if (safe >= 1_000_000_000.0)
            String.format(Locale.US, "%.2f GB", safe / 1_000_000_000.0)
        else
            String.format(Locale.US, "%.0f MB", safe / 1_000_000.0)
    }

    private fun formatDuration(millis: Long): String {
        val seconds = millis.coerceAtLeast(0L) / 1000L
        if (seconds < 60L) return "${seconds}s"
        val minutes = seconds / 60L
        if (minutes < 60L) return "${minutes} min"
        val hours = minutes / 60L
        val remainder = minutes % 60L
        return if (remainder == 0L) "${hours} h" else "${hours} h ${remainder} min"
    }

    private fun humanAction(action: String): String = when {
        action.startsWith("USER_INCIDENT_MARKER") -> "problème utilisateur enregistré"
        action.startsWith("RECOVERY_MODE_") -> "mode de récupération modifié"
        action.startsWith("MOBILE_ASSIST_") -> "assistance mobile observée"
        action.startsWith("AUTOPILOT_PROFILE_") -> "profil Autopilot modifié"
        action.startsWith("TRUSTED_WIFI_") -> "contexte Wi-Fi mis à jour"
        action.startsWith("EMERGENCY_") -> "action d’urgence appliquée"
        else -> action.lowercase().replace('_', ' ')
    }

    private fun installSystemBarInsets() {
        val root = findViewById<ScrollView>(R.id.productSurfaceScroll)
        root.setOnApplyWindowInsetsListener { view, insets ->
            val bars = insets.getInsets(WindowInsets.Type.systemBars())
            view.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }
        root.requestApplyInsets()
    }

    companion object {
        const val EXTRA_SURFACE = "kinlink.surface"
        const val EXTRA_DIAGNOSTIC_BODY = "kinlink.surface.diagnostic"
        const val EXTRA_DIAGNOSTIC_ACTION = "kinlink.surface.diagnostic_action"
        const val EXTRA_WIFI_DIGEST = "kinlink.surface.wifi_digest"
        const val EXTRA_WIFI_LABEL = "kinlink.surface.wifi_label"
        const val SURFACE_DIAGNOSTIC = "diagnostic"
        const val SURFACE_WEEK = "week"
        const val SURFACE_MOBILE_VAULT = "mobile_vault"
        const val SURFACE_SETTINGS = "settings"
        const val DIAGNOSTIC_ACTION_NONE = "none"
        const val DIAGNOSTIC_ACTION_WIFI = "wifi"
        const val DIAGNOSTIC_ACTION_MOBILE = "mobile"
    }
}
