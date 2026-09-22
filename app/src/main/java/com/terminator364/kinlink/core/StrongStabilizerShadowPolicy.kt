package com.terminator364.kinlink.core

enum class StrongStabilizerShadowMode {
    DISABLED,
    SHADOW_ONLY,
    PER_APP_CANARY
}

data class StrongStabilizerShadowInput(
    val safeMode: Boolean,
    val emergencyObservationOnly: Boolean,
    val resourcesConstrained: Boolean,
    val handoffInProgress: Boolean,
    val nativeEscapeVerified: Boolean,
    val perAppConsent: Boolean,
    val lanPreservationVerified: Boolean,
    val dnsSafetyVerified: Boolean,
    val routeLoopSafetyVerified: Boolean,
    val mtuSafetyVerified: Boolean,
    val ipv4Verified: Boolean,
    val ipv6Verified: Boolean,
    val meteredNetwork: Boolean,
    val meteredCanaryAllowed: Boolean,
    val harmObserved: Boolean
)

data class StrongStabilizerShadowAssessment(
    val mode: StrongStabilizerShadowMode,
    val blockers: Set<String>
)

data class StrongStabilizerPromotionEvidence(
    val measurableBenefitPass: Boolean,
    val resourceBudgetPass: Boolean,
    val rollbackPass: Boolean,
    val handoffRegressionPass: Boolean,
    val dnsBehaviorPass: Boolean,
    val lanPreservationPass: Boolean,
    val ipv4Pass: Boolean,
    val ipv6Pass: Boolean,
    val quicPass: Boolean,
    val nativeEscapePass: Boolean,
    val harmObserved: Boolean
)

object StrongStabilizerShadowPolicy {
    fun evaluate(input: StrongStabilizerShadowInput): StrongStabilizerShadowAssessment {
        val hardBlockers = linkedSetOf<String>()
        if (input.safeMode) hardBlockers += "SAFE_MODE"
        if (input.emergencyObservationOnly) hardBlockers += "EMERGENCY_OBSERVATION_ONLY"
        if (input.resourcesConstrained) hardBlockers += "RESOURCE_CONSTRAINED"
        if (input.handoffInProgress) hardBlockers += "HANDOFF_IN_PROGRESS"
        if (!input.nativeEscapeVerified) hardBlockers += "NATIVE_ESCAPE_UNVERIFIED"
        if (input.harmObserved) hardBlockers += "HARM_OBSERVED"

        if (hardBlockers.isNotEmpty()) {
            return StrongStabilizerShadowAssessment(
                mode = StrongStabilizerShadowMode.DISABLED,
                blockers = hardBlockers
            )
        }

        val canaryBlockers = linkedSetOf<String>()
        if (!input.perAppConsent) canaryBlockers += "PER_APP_CONSENT_REQUIRED"
        if (!input.lanPreservationVerified) canaryBlockers += "LAN_PRESERVATION_UNVERIFIED"
        if (!input.dnsSafetyVerified) canaryBlockers += "DNS_SAFETY_UNVERIFIED"
        if (!input.routeLoopSafetyVerified) canaryBlockers += "ROUTE_LOOP_SAFETY_UNVERIFIED"
        if (!input.mtuSafetyVerified) canaryBlockers += "MTU_SAFETY_UNVERIFIED"
        if (!input.ipv4Verified) canaryBlockers += "IPV4_UNVERIFIED"
        if (!input.ipv6Verified) canaryBlockers += "IPV6_UNVERIFIED"
        if (input.meteredNetwork && !input.meteredCanaryAllowed) {
            canaryBlockers += "METERED_CANARY_NOT_ALLOWED"
        }

        return StrongStabilizerShadowAssessment(
            mode = if (canaryBlockers.isEmpty()) {
                StrongStabilizerShadowMode.PER_APP_CANARY
            } else {
                StrongStabilizerShadowMode.SHADOW_ONLY
            },
            blockers = canaryBlockers
        )
    }

    fun promotionReady(evidence: StrongStabilizerPromotionEvidence): Boolean {
        if (evidence.harmObserved) return false
        return evidence.measurableBenefitPass &&
            evidence.resourceBudgetPass &&
            evidence.rollbackPass &&
            evidence.handoffRegressionPass &&
            evidence.dnsBehaviorPass &&
            evidence.lanPreservationPass &&
            evidence.ipv4Pass &&
            evidence.ipv6Pass &&
            evidence.quicPass &&
            evidence.nativeEscapePass
    }
}
