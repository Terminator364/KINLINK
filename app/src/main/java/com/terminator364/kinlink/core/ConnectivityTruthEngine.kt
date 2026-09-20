package com.terminator364.kinlink.core

import android.net.NetworkCapabilities
import android.net.LinkProperties
import java.net.Inet4Address
import java.net.Inet6Address

object ConnectivityTruthEngine {
    /** Android-facing observation converted to a pure value before any policy reduction. */
    data class Snapshot(
        val transport: Transport = Transport.NONE,
        val validated: Boolean = false,
        val partial: Boolean = false,
        val captive: Boolean = false,
        val metered: Boolean = false,
        val interfaceName: String? = null,
        val gateway: String? = null,
        val downstreamKbps: Int = 0,
        val upstreamKbps: Int = 0,
        val dnsServerCount: Int = 0,
        val privateDnsActive: Boolean = false,
        val hasIpv4Address: Boolean = false,
        val hasIpv6Address: Boolean = false,
        val hasIpv4DefaultRoute: Boolean = false,
        val hasIpv6DefaultRoute: Boolean = false
    )

    fun reduce(capabilities: NetworkCapabilities?, linkProperties: LinkProperties?): NetworkTruth {
        if (capabilities == null) {
            return reduce(Snapshot())
        }

        val transport = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> Transport.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> Transport.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> Transport.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> Transport.VPN
            else -> Transport.UNKNOWN
        }

        val captive = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)
        // Android does not expose a stable public partial-connectivity capability
        // across the supported SDK toolchains.  M0 therefore reports UNKNOWN
        // rather than inferring a partial WAN state from hidden APIs.
        val partial = false
        val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val internetState = when {
            captive -> InternetState.CAPTIVE_PORTAL
            partial -> InternetState.PARTIAL
            validated -> InternetState.VALIDATED
            else -> InternetState.UNKNOWN
        }

        val context = when {
            captive -> ContextType.CAPTIVE_PORTAL
            transport == Transport.CELLULAR -> ContextType.MOBILE_RESILIENT
            transport == Transport.WIFI -> ContextType.UNKNOWN_WIFI
            else -> ContextType.UNKNOWN
        }

        val gateway = linkProperties?.routes
            ?.firstOrNull { it.isDefaultRoute }
            ?.gateway
            ?.hostAddress

        return reduce(
            Snapshot(
                transport = transport,
                validated = validated,
                partial = partial,
                captive = captive,
                metered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
                interfaceName = linkProperties?.interfaceName,
                gateway = gateway,
                downstreamKbps = capabilities.linkDownstreamBandwidthKbps.coerceAtLeast(0),
                upstreamKbps = capabilities.linkUpstreamBandwidthKbps.coerceAtLeast(0),
                dnsServerCount = linkProperties?.dnsServers?.size ?: 0,
                privateDnsActive = linkProperties?.isPrivateDnsActive ?: false,
                hasIpv4Address = linkProperties?.linkAddresses?.any { it.address is Inet4Address } == true,
                hasIpv6Address = linkProperties?.linkAddresses?.any { it.address is Inet6Address } == true,
                hasIpv4DefaultRoute = linkProperties?.routes?.any {
                    it.isDefaultRoute && it.destination.address is Inet4Address
                } == true,
                hasIpv6DefaultRoute = linkProperties?.routes?.any {
                    it.isDefaultRoute && it.destination.address is Inet6Address
                } == true
            )
        )
    }

    fun reduce(snapshot: Snapshot): NetworkTruth {
        val transport = snapshot.transport
        val captive = snapshot.captive
        val partial = snapshot.partial
        val validated = snapshot.validated
        val internetState = when {
            transport == Transport.NONE -> InternetState.OFFLINE
            captive -> InternetState.CAPTIVE_PORTAL
            partial -> InternetState.PARTIAL
            validated -> InternetState.VALIDATED
            else -> InternetState.UNKNOWN
        }
        val context = when {
            transport == Transport.NONE -> ContextType.OFFLINE
            captive -> ContextType.CAPTIVE_PORTAL
            transport == Transport.CELLULAR -> ContextType.MOBILE_RESILIENT
            transport == Transport.WIFI -> ContextType.UNKNOWN_WIFI
            else -> ContextType.UNKNOWN
        }
        val lanState = when {
            transport != Transport.WIFI && transport != Transport.ETHERNET -> LanState.UNKNOWN
            snapshot.interfaceName.isNullOrBlank() -> LanState.DOWN
            else -> LanState.LINK_PRESENT
        }

        val failureDomain = when {
            internetState == InternetState.VALIDATED -> FailureDomain.NONE
            captive -> FailureDomain.ROUTER
            lanState == LanState.LINK_PRESENT && internetState != InternetState.VALIDATED -> FailureDomain.ISP
            partial -> FailureDomain.UNKNOWN
            transport == Transport.NONE -> FailureDomain.NO_LINK
            else -> FailureDomain.UNKNOWN
        }

        return NetworkTruth(
            transport = transport,
            internetState = internetState,
            lanState = lanState,
            budgetState = BudgetState.BALANCE_UNKNOWN,
            failureDomain = failureDomain,
            context = context,
            metered = snapshot.metered,
            interfaceName = snapshot.interfaceName,
            gateway = snapshot.gateway,
            downstreamKbps = snapshot.downstreamKbps.coerceAtLeast(0),
            upstreamKbps = snapshot.upstreamKbps.coerceAtLeast(0),
            dnsServerCount = snapshot.dnsServerCount.coerceAtLeast(0),
            privateDnsActive = snapshot.privateDnsActive,
            hasIpv4Address = snapshot.hasIpv4Address,
            hasIpv6Address = snapshot.hasIpv6Address,
            hasIpv4DefaultRoute = snapshot.hasIpv4DefaultRoute,
            hasIpv6DefaultRoute = snapshot.hasIpv6DefaultRoute,
            confidence = if (validated || captive) 0.9 else 0.55
        )
    }
}
