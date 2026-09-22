package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrustedWifiContextPolicyTest {
    private fun wifi(gateway: String) = NetworkTruth(
        transport = Transport.WIFI,
        internetState = InternetState.VALIDATED,
        lanState = LanState.LINK_PRESENT,
        interfaceName = "wlan0",
        gateway = gateway,
        dnsServerCount = 2,
        privateDnsActive = true,
        hasIpv4Address = true,
        hasIpv4DefaultRoute = true
    )

    @Test fun wifiFingerprintIsStableDigestAndDoesNotExposeRawGateway() {
        val digest = requireNotNull(
            TrustedWifiContextPolicy.fingerprint(wifi("192.168.1.1"))
        )
        assertEquals(64, digest.length)
        assertFalse(digest.contains("192.168.1.1"))
        assertTrue(digest.all { it in '0'..'9' || it in 'a'..'f' })
        assertEquals(digest, TrustedWifiContextPolicy.fingerprint(wifi("192.168.1.1")))
    }

    @Test fun materiallyDifferentTopologyChangesFingerprint() {
        assertNotEquals(
            TrustedWifiContextPolicy.fingerprint(wifi("192.168.1.1")),
            TrustedWifiContextPolicy.fingerprint(wifi("10.0.0.1"))
        )
    }

    @Test fun nonWifiOrIncompleteIdentityDoesNotCreateFingerprint() {
        assertNull(TrustedWifiContextPolicy.fingerprint(NetworkTruth(transport = Transport.CELLULAR)))
        assertNull(
            TrustedWifiContextPolicy.fingerprint(
                NetworkTruth(transport = Transport.WIFI, interfaceName = "wlan0", gateway = null)
            )
        )
    }

    @Test fun trustedContextCannotAuthorizeActiveRecovery() {
        assertFalse(TrustedWifiContextPolicy.mayAuthorizeActiveRecovery())
    }
}
