package com.terminator364.kinlink.core

enum class MobilePlanUsageSource {
    USER_RECONCILED,
    ANDROID_DEVICE_WIDE_COUNTER,
    NETWORK_STATS_OPTIONAL,
    CARRIER_ADAPTER,
    UNKNOWN
}

data class MobilePlanConfig(
    val totalBytes: Long,
    val expiryAtEpochMillis: Long?,
    val protectedReserveBytes: Long,
    val rescueAllowanceBytes: Long,
    val criticalInteractiveAllowanceBytes: Long
)

data class MobilePlanUsage(
    val usedBytes: Long?,
    val source: MobilePlanUsageSource,
    val observedAtEpochMillis: Long
)

enum class MobileVaultZone {
    NORMAL,
    PROTECTED_RESERVE,
    RESCUE_ONLY,
    CRITICAL_INTERACTIVE_ONLY,
    EXHAUSTED,
    EXPIRED,
    UNKNOWN
}

enum class MobileSpendClass {
    NORMAL,
    RESCUE,
    CRITICAL_INTERACTIVE
}

data class MobileVaultAssessment(
    val zone: MobileVaultZone,
    val remainingBytes: Long?,
    val normalAllowanceBytes: Long?,
    val configurationValid: Boolean,
    val autonomousMobileActionAllowed: Boolean,
    val reason: String
)

object MobilePlanVaultPolicy {
    fun evaluate(
        config: MobilePlanConfig?,
        usage: MobilePlanUsage?,
        nowEpochMillis: Long
    ): MobileVaultAssessment {
        if (config == null) {
            return unknown("Forfait non configuré.")
        }
        if (!valid(config)) {
            return MobileVaultAssessment(
                zone = MobileVaultZone.UNKNOWN,
                remainingBytes = null,
                normalAllowanceBytes = null,
                configurationValid = false,
                autonomousMobileActionAllowed = false,
                reason = "Configuration Mobile Vault incohérente; aucune dépense autonome."
            )
        }

        val normalAllowance = safeSubtract(
            config.totalBytes,
            safeAdd(
                config.protectedReserveBytes,
                safeAdd(
                    config.rescueAllowanceBytes,
                    config.criticalInteractiveAllowanceBytes
                )
            )
        )

        val expiry = config.expiryAtEpochMillis
        if (expiry != null && nowEpochMillis >= expiry) {
            return MobileVaultAssessment(
                zone = MobileVaultZone.EXPIRED,
                remainingBytes = usage?.usedBytes?.let {
                    (config.totalBytes - it.coerceAtLeast(0L)).coerceAtLeast(0L)
                },
                normalAllowanceBytes = normalAllowance,
                configurationValid = true,
                autonomousMobileActionAllowed = false,
                reason = "Forfait signalé expiré; aucune dépense autonome."
            )
        }

        val used = usage?.usedBytes
            ?: return MobileVaultAssessment(
                zone = MobileVaultZone.UNKNOWN,
                remainingBytes = null,
                normalAllowanceBytes = normalAllowance,
                configurationValid = true,
                autonomousMobileActionAllowed = false,
                reason = "Consommation du cycle inconnue; protection fail-safe."
            )
        val remaining = (config.totalBytes - used.coerceAtLeast(0L)).coerceAtLeast(0L)

        val critical = config.criticalInteractiveAllowanceBytes
        val rescueBoundary = safeAdd(critical, config.rescueAllowanceBytes)
        val reserveBoundary = safeAdd(rescueBoundary, config.protectedReserveBytes)

        val zone = when {
            remaining <= 0L -> MobileVaultZone.EXHAUSTED
            remaining <= critical -> MobileVaultZone.CRITICAL_INTERACTIVE_ONLY
            remaining <= rescueBoundary -> MobileVaultZone.RESCUE_ONLY
            remaining <= reserveBoundary -> MobileVaultZone.PROTECTED_RESERVE
            else -> MobileVaultZone.NORMAL
        }
        return MobileVaultAssessment(
            zone = zone,
            remainingBytes = remaining,
            normalAllowanceBytes = normalAllowance,
            configurationValid = true,
            autonomousMobileActionAllowed = zone == MobileVaultZone.NORMAL,
            reason = when (zone) {
                MobileVaultZone.NORMAL -> "Budget normal disponible."
                MobileVaultZone.PROTECTED_RESERVE -> "Réserve protégée atteinte; actions autonomes suspendues."
                MobileVaultZone.RESCUE_ONLY -> "Zone secours uniquement."
                MobileVaultZone.CRITICAL_INTERACTIVE_ONLY -> "Réserve critique interactive uniquement."
                MobileVaultZone.EXHAUSTED -> "Forfait épuisé selon les données disponibles."
                MobileVaultZone.EXPIRED -> "Forfait signalé expiré."
                MobileVaultZone.UNKNOWN -> "Budget inconnu."
            }
        )
    }

    fun maySpend(
        zone: MobileVaultZone,
        spendClass: MobileSpendClass
    ): Boolean = when (zone) {
        MobileVaultZone.NORMAL -> true
        MobileVaultZone.PROTECTED_RESERVE -> false
        MobileVaultZone.RESCUE_ONLY ->
            spendClass == MobileSpendClass.RESCUE ||
                spendClass == MobileSpendClass.CRITICAL_INTERACTIVE
        MobileVaultZone.CRITICAL_INTERACTIVE_ONLY ->
            spendClass == MobileSpendClass.CRITICAL_INTERACTIVE
        MobileVaultZone.EXHAUSTED,
        MobileVaultZone.EXPIRED,
        MobileVaultZone.UNKNOWN -> false
    }

    fun valid(config: MobilePlanConfig): Boolean {
        if (config.totalBytes <= 0L) return false
        if (
            config.protectedReserveBytes < 0L ||
            config.rescueAllowanceBytes < 0L ||
            config.criticalInteractiveAllowanceBytes < 0L
        ) return false
        val protected = safeAdd(
            config.protectedReserveBytes,
            safeAdd(
                config.rescueAllowanceBytes,
                config.criticalInteractiveAllowanceBytes
            )
        )
        return protected <= config.totalBytes
    }

    private fun unknown(reason: String) = MobileVaultAssessment(
        zone = MobileVaultZone.UNKNOWN,
        remainingBytes = null,
        normalAllowanceBytes = null,
        configurationValid = true,
        autonomousMobileActionAllowed = false,
        reason = reason
    )

    private fun safeAdd(a: Long, b: Long): Long {
        if (a < 0L || b < 0L) return Long.MAX_VALUE
        return if (Long.MAX_VALUE - a < b) Long.MAX_VALUE else a + b
    }

    private fun safeSubtract(a: Long, b: Long): Long =
        if (b >= a) 0L else a - b
}
