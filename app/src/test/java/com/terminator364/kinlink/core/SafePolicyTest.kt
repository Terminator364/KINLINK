package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafePolicyTest {
    @Test fun mobileVaultNeverRetriesAnExhaustedBundle() {
        val truth = NetworkTruth(transport = Transport.CELLULAR, budgetState = BudgetState.BUNDLE_EXHAUSTED)
        val decision = MobileVault.decide(truth, ConnectivityStateClassifier.classify(truth))
        assertEquals(VaultAction.HOLD_MOBILE_RECOVERY, decision.what)
        assertTrue(decision.result.contains("Aucun retry"))
    }

    @Test fun wifiDoctorKeepsLanAvailableWhenWanIsUnknown() {
        val truth = NetworkTruth(transport = Transport.WIFI, internetState = InternetState.UNKNOWN, lanState = LanState.LINK_PRESENT)
        val advice = WifiDoctor.advise(ConnectivityStateClassifier.classify(truth))
        assertTrue(advice.message.contains("fonctions locales"))
    }
}
