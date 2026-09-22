package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrongStabilizerShadowPolicyTest {
    private fun readyInput(
        safeMode: Boolean = false,
        emergencyObservationOnly: Boolean = false,
        resourcesConstrained: Boolean = false,
        handoffInProgress: Boolean = false,
        nativeEscapeVerified: Boolean = true,
        perAppConsent: Boolean = true,
        lanPreservationVerified: Boolean = true,
        dnsSafetyVerified: Boolean = true,
        routeLoopSafetyVerified: Boolean = true,
        mtuSafetyVerified: Boolean = true,
        ipv4Verified: Boolean = true,
        ipv6Verified: Boolean = true,
        meteredNetwork: Boolean = false,
        meteredCanaryAllowed: Boolean = false,
        harmObserved: Boolean = false
    ) = StrongStabilizerShadowInput(
        safeMode = safeMode,
        emergencyObservationOnly = emergencyObservationOnly,
        resourcesConstrained = resourcesConstrained,
        handoffInProgress = handoffInProgress,
        nativeEscapeVerified = nativeEscapeVerified,
        perAppConsent = perAppConsent,
        lanPreservationVerified = lanPreservationVerified,
        dnsSafetyVerified = dnsSafetyVerified,
        routeLoopSafetyVerified = routeLoopSafetyVerified,
        mtuSafetyVerified = mtuSafetyVerified,
        ipv4Verified = ipv4Verified,
        ipv6Verified = ipv6Verified,
        meteredNetwork = meteredNetwork,
        meteredCanaryAllowed = meteredCanaryAllowed,
        harmObserved = harmObserved
    )

    @Test fun safeModeHardDisablesStrongStabilizer() {
        val a = StrongStabilizerShadowPolicy.evaluate(readyInput(safeMode = true))
        assertEquals(StrongStabilizerShadowMode.DISABLED, a.mode)
        assertTrue(a.blockers.contains("SAFE_MODE"))
    }

    @Test fun constrainedResourcesHardDisableCanary() {
        val a = StrongStabilizerShadowPolicy.evaluate(readyInput(resourcesConstrained = true))
        assertEquals(StrongStabilizerShadowMode.DISABLED, a.mode)
        assertTrue(a.blockers.contains("RESOURCE_CONSTRAINED"))
    }

    @Test fun missingNativeEscapeHardDisablesCanary() {
        val a = StrongStabilizerShadowPolicy.evaluate(readyInput(nativeEscapeVerified = false))
        assertEquals(StrongStabilizerShadowMode.DISABLED, a.mode)
        assertTrue(a.blockers.contains("NATIVE_ESCAPE_UNVERIFIED"))
    }

    @Test fun incompleteSafetyEvidenceStaysShadowOnly() {
        val a = StrongStabilizerShadowPolicy.evaluate(
            readyInput(dnsSafetyVerified = false, mtuSafetyVerified = false)
        )
        assertEquals(StrongStabilizerShadowMode.SHADOW_ONLY, a.mode)
        assertTrue(a.blockers.contains("DNS_SAFETY_UNVERIFIED"))
        assertTrue(a.blockers.contains("MTU_SAFETY_UNVERIFIED"))
    }

    @Test fun meteredNetworkNeedsExplicitCanaryAllowance() {
        val a = StrongStabilizerShadowPolicy.evaluate(
            readyInput(meteredNetwork = true, meteredCanaryAllowed = false)
        )
        assertEquals(StrongStabilizerShadowMode.SHADOW_ONLY, a.mode)
        assertTrue(a.blockers.contains("METERED_CANARY_NOT_ALLOWED"))
    }

    @Test fun fullyVerifiedPerAppCanaryCanBeReady() {
        val a = StrongStabilizerShadowPolicy.evaluate(
            readyInput(meteredNetwork = true, meteredCanaryAllowed = true)
        )
        assertEquals(StrongStabilizerShadowMode.PER_APP_CANARY, a.mode)
        assertTrue(a.blockers.isEmpty())
    }

    @Test fun promotionRequiresEveryEvidenceGate() {
        val pass = StrongStabilizerPromotionEvidence(
            measurableBenefitPass = true,
            resourceBudgetPass = true,
            rollbackPass = true,
            handoffRegressionPass = true,
            dnsBehaviorPass = true,
            lanPreservationPass = true,
            ipv4Pass = true,
            ipv6Pass = true,
            quicPass = true,
            nativeEscapePass = true,
            harmObserved = false
        )
        assertTrue(StrongStabilizerShadowPolicy.promotionReady(pass))
        assertFalse(
            StrongStabilizerShadowPolicy.promotionReady(
                pass.copy(resourceBudgetPass = false)
            )
        )
        assertFalse(
            StrongStabilizerShadowPolicy.promotionReady(
                pass.copy(harmObserved = true)
            )
        )
    }
}
