package com.terminator364.kinlink.core

import android.app.AppOpsManager
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Looper
import android.os.Process

/**
 * Optional fail-open Android NetworkStats adapter for Mobile Vault.
 *
 * This block intentionally queries only aggregate device-mobile usage.
 * It never promotes that value to per-plan truth.
 */
class NetworkStatsMobileUsageReader(context: Context) {
    private val appContext = context.applicationContext
    private val manager =
        appContext.getSystemService(NetworkStatsManager::class.java)
    private val appOps =
        appContext.getSystemService(AppOpsManager::class.java)

    @Suppress("DEPRECATION")
    fun query(
        request: NetworkStatsMobileUsageRequest
    ): NetworkStatsMobileUsageEvidence {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return unavailable(
                request,
                NetworkStatsMobileEvidenceStatus.MAIN_THREAD_BLOCKED,
                "NetworkStats bloqué sur le thread UI; aucune requête exécutée."
            )
        }

        NetworkStatsMobileUsagePolicy.validationStatus(request)?.let {
            return unavailable(
                request,
                it,
                if (it == NetworkStatsMobileEvidenceStatus.CYCLE_START_REQUIRED) {
                    "Début de cycle absent; aucune consommation de forfait n'est déduite."
                } else {
                    "Fenêtre NetworkStats invalide; preuve ignorée."
                }
            )
        }

        if (!hasUsageAccess()) {
            return unavailable(
                request,
                NetworkStatsMobileEvidenceStatus.USAGE_ACCESS_REQUIRED,
                "Usage Access non accordé; Mobile Vault reste fonctionnel sans cette preuve."
            )
        }

        val start = requireNotNull(request.cycleStartAtEpochMillis)
        return try {
            val bucket = manager.querySummaryForDevice(
                ConnectivityManager.TYPE_MOBILE,
                null,
                start,
                request.endAtEpochMillis
            )
            val total = NetworkStatsMobileUsagePolicy.safeTotalBytes(
                bucket.rxBytes,
                bucket.txBytes
            ) ?: return unavailable(
                request,
                NetworkStatsMobileEvidenceStatus.PLATFORM_REJECTED,
                "Android a renvoyé des compteurs NetworkStats invalides."
            )
            NetworkStatsMobileUsageEvidence(
                status = NetworkStatsMobileEvidenceStatus.AVAILABLE,
                usedBytes = total,
                cycleStartAtEpochMillis = start,
                observedAtEpochMillis = request.endAtEpochMillis,
                confidencePercent = 75,
                reason =
                    "NetworkStats mobile agrégé; ne constitue pas une vérité par forfait."
            )
        } catch (_: SecurityException) {
            unavailable(
                request,
                NetworkStatsMobileEvidenceStatus.PLATFORM_REJECTED,
                "Android refuse l'accès NetworkStats demandé; aucune estimation n'est inventée."
            )
        } catch (_: RuntimeException) {
            unavailable(
                request,
                NetworkStatsMobileEvidenceStatus.PLATFORM_REJECTED,
                "NetworkStats indisponible sur cet appareil; le cœur KINLINK continue."
            )
        }
    }

    private fun hasUsageAccess(): Boolean {
        val mode = runCatching {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                appContext.packageName
            )
        }.getOrDefault(AppOpsManager.MODE_ERRORED)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun unavailable(
        request: NetworkStatsMobileUsageRequest,
        status: NetworkStatsMobileEvidenceStatus,
        reason: String
    ) = NetworkStatsMobileUsageEvidence(
        status = status,
        usedBytes = null,
        cycleStartAtEpochMillis = request.cycleStartAtEpochMillis,
        observedAtEpochMillis =
            request.endAtEpochMillis.coerceAtLeast(0L),
        confidencePercent = 0,
        reason = reason
    )
}
