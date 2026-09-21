package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class UserExperiencePresentationPolicyTest {
    @Test fun offlineNeverUsesGenericQualityTitle() {
        assertEquals(
            "Hors ligne · surveillance passive",
            UserExperiencePresentationPolicy.controlTitle(
                UserExperienceState.OFFLINE,
                "Qualité sous surveillance"
            )
        )
    }

    @Test fun unstableHistoryGetsExplicitTitle() {
        assertEquals(
            "Historique instable · surveillance renforcée",
            UserExperiencePresentationPolicy.controlTitle(
                UserExperienceState.UNSTABLE_HISTORY,
                "Connexion dans la zone cible"
            )
        )
    }

    @Test fun platformStallLabelIsHumanReadable() {
        assertEquals(
            "2 blocage(s) réseau détecté(s)",
            UserExperiencePresentationPolicy.platformStallLabel(2)
        )
    }
}
