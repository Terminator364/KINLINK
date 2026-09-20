package com.terminator364.kinlink.core

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test

class StartupSourcePolicyTest {
    @Test fun classifiesBoot() {
        assertEquals(
            StartupSource.BOOT_COMPLETED,
            StartupSourcePolicy.fromAction(Intent.ACTION_BOOT_COMPLETED)
        )
    }

    @Test fun classifiesPackageReplacement() {
        assertEquals(
            StartupSource.PACKAGE_REPLACED,
            StartupSourcePolicy.fromAction(Intent.ACTION_MY_PACKAGE_REPLACED)
        )
    }

    @Test fun unknownActionIsOther() {
        assertEquals(StartupSource.OTHER, StartupSourcePolicy.fromAction("x"))
    }
}
