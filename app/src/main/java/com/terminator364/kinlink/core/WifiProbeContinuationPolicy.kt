package com.terminator364.kinlink.core

object WifiProbeContinuationPolicy {
    fun mayContinue(sameActiveNetwork: Boolean, stillWifi: Boolean): Boolean =
        sameActiveNetwork && stillWifi
}
