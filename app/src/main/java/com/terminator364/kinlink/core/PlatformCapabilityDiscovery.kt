package com.terminator364.kinlink.core

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import androidx.core.content.ContextCompat

enum class LocalNetworkAccessState {
    NOT_REQUIRED_PRE_API_37,
    GRANTED,
    NOT_GRANTED
}

enum class DataSaverState {
    DISABLED,
    WHITELISTED,
    ENABLED,
    UNKNOWN
}

data class PlatformCapabilitySnapshot(
    val apiLevel: Int,
    val wifiFeature: Boolean,
    val telephonyFeature: Boolean,
    val localNetworkAccess: LocalNetworkAccessState,
    val dataSaver: DataSaverState,
    val networkUsageAccess: Boolean,
    val vpnExcludeRouteSupported: Boolean,
    val connectivityDiagnosticsEligibleInLite: Boolean
)

object PlatformCapabilityPolicy {
    fun localNetworkAccessState(
        apiLevel: Int,
        permissionGranted: Boolean
    ): LocalNetworkAccessState = when {
        apiLevel < 37 -> LocalNetworkAccessState.NOT_REQUIRED_PRE_API_37
        permissionGranted -> LocalNetworkAccessState.GRANTED
        else -> LocalNetworkAccessState.NOT_GRANTED
    }

    fun dataSaverState(status: Int): DataSaverState = when (status) {
        ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED ->
            DataSaverState.DISABLED
        ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED ->
            DataSaverState.WHITELISTED
        ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED ->
            DataSaverState.ENABLED
        else -> DataSaverState.UNKNOWN
    }

    fun vpnExcludeRouteSupported(apiLevel: Int): Boolean = apiLevel >= 33

    fun connectivityDiagnosticsEligibleInLite(): Boolean = false
}

/**
 * Capability discovery is observational only. It never requests a permission,
 * starts a VPN, or opens a socket. The result is used to gate future features
 * before they can fail at runtime.
 */
class PlatformCapabilityDiscovery(private val context: Context) {
    fun snapshot(): PlatformCapabilitySnapshot {
        val pm = context.packageManager
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val sdk = Build.VERSION.SDK_INT
        val localPermissionGranted =
            if (sdk >= 37) {
                ContextCompat.checkSelfPermission(
                    context,
                    "android.permission.ACCESS_LOCAL_NETWORK"
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val usageAllowed = runCatching {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            ) == AppOpsManager.MODE_ALLOWED
        }.getOrDefault(false)

        return PlatformCapabilitySnapshot(
            apiLevel = sdk,
            wifiFeature = pm.hasSystemFeature(PackageManager.FEATURE_WIFI),
            telephonyFeature = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY),
            localNetworkAccess = PlatformCapabilityPolicy.localNetworkAccessState(
                sdk,
                localPermissionGranted
            ),
            dataSaver = PlatformCapabilityPolicy.dataSaverState(
                cm.restrictBackgroundStatus
            ),
            networkUsageAccess = usageAllowed,
            vpnExcludeRouteSupported =
                PlatformCapabilityPolicy.vpnExcludeRouteSupported(sdk),
            connectivityDiagnosticsEligibleInLite =
                PlatformCapabilityPolicy.connectivityDiagnosticsEligibleInLite()
        )
    }
}
