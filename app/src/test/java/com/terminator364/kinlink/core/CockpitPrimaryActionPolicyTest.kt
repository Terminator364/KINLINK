package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class CockpitPrimaryActionPolicyTest {
    @Test fun onlyCurrentTransportActionIsShown() {
        assertEquals(
            CockpitPrimaryAction.WIFI_ASSIST,
            CockpitPrimaryActionPolicy.select(
                Transport.WIFI,
                RecoveryMode.AUTOMATIC
            )
        )
        assertEquals(
            CockpitPrimaryAction.MOBILE_ASSIST,
            CockpitPrimaryActionPolicy.select(
                Transport.CELLULAR,
                RecoveryMode.AUTOMATIC
            )
        )
    }

    @Test fun safeModeHidesActiveAssistance() {
        for (transport in Transport.values()) {
            assertEquals(
                "transport=$transport",
                CockpitPrimaryAction.NONE,
                CockpitPrimaryActionPolicy.select(
                    transport,
                    RecoveryMode.OBSERVATION_ONLY
                )
            )
        }
    }

    @Test fun nonWifiNonCellularHasNoPrimaryAssistAction() {
        for (transport in Transport.values().filter {
            it != Transport.WIFI && it != Transport.CELLULAR
        }) {
            assertEquals(
                CockpitPrimaryAction.NONE,
                CockpitPrimaryActionPolicy.select(
                    transport,
                    RecoveryMode.AUTOMATIC
                )
            )
        }
    }
}
