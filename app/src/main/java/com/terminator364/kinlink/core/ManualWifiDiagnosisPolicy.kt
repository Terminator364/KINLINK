package com.terminator364.kinlink.core

enum class ManualWifiDiagnosisCause {
    ANDROID_VALIDATED,
    DNS_CONFIRMED_HTTP_CONFIRMED,
    DNS_FAILED,
    DNS_CONFIRMED_HTTP_FAILED,
    INCONCLUSIVE
}

object ManualWifiDiagnosisPolicy {
    fun classify(
        androidValidated: Boolean,
        dnsSucceeded: Boolean,
        httpSucceeded: Boolean
    ): ManualWifiDiagnosisCause = when {
        androidValidated -> ManualWifiDiagnosisCause.ANDROID_VALIDATED
        dnsSucceeded && httpSucceeded -> ManualWifiDiagnosisCause.DNS_CONFIRMED_HTTP_CONFIRMED
        !dnsSucceeded -> ManualWifiDiagnosisCause.DNS_FAILED
        dnsSucceeded && !httpSucceeded -> ManualWifiDiagnosisCause.DNS_CONFIRMED_HTTP_FAILED
        else -> ManualWifiDiagnosisCause.INCONCLUSIVE
    }
}
