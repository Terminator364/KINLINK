package com.terminator364.kinlink.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ManualWifiDiagnosisPolicyTest {
    @Test fun androidValidatedHasPriority() {
        assertEquals(
            ManualWifiDiagnosisCause.ANDROID_VALIDATED,
            ManualWifiDiagnosisPolicy.classify(true, false, false)
        )
    }

    @Test fun dnsFailureIsSeparatedFromHttpFailure() {
        assertEquals(
            ManualWifiDiagnosisCause.DNS_FAILED,
            ManualWifiDiagnosisPolicy.classify(false, false, false)
        )
        assertEquals(
            ManualWifiDiagnosisCause.DNS_CONFIRMED_HTTP_FAILED,
            ManualWifiDiagnosisPolicy.classify(false, true, false)
        )
    }
}
