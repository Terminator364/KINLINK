package com.terminator364.kinlink.core

enum class Transport { WIFI, CELLULAR, ETHERNET, VPN, NONE, UNKNOWN }
enum class InternetState { VALIDATED, PARTIAL, CAPTIVE_PORTAL, STALLED, OFFLINE, UNKNOWN }
/**
 * LAN is deliberately independent from InternetState.  LINK_PRESENT means that
 * Android supplied a local interface; it is not a claim that any LAN host was
 * probed or reachable.
 */
enum class LanState { HEALTHY, LINK_PRESENT, DEGRADED, DOWN, UNKNOWN }
enum class BudgetState { BUNDLE_OK, BUNDLE_LOW, BUNDLE_EXHAUSTED, BUNDLE_EXPIRED, BALANCE_UNKNOWN }
enum class FailureDomain { NONE, RADIO, LAN, ROUTER, ISP, DNS, IPV4, IPV6, REMOTE_SERVICE, NO_LINK, UNKNOWN }
enum class ContextType { HOME_WIFI, TRUSTED_WIFI, UNKNOWN_WIFI, MOBILE_RESILIENT, CAPTIVE_PORTAL, OFFLINE, UNKNOWN }

data class NetworkTruth(
    val transport: Transport = Transport.NONE,
    val internetState: InternetState = InternetState.OFFLINE,
    val lanState: LanState = LanState.UNKNOWN,
    val budgetState: BudgetState = BudgetState.BALANCE_UNKNOWN,
    val failureDomain: FailureDomain = FailureDomain.UNKNOWN,
    val context: ContextType = ContextType.UNKNOWN,
    val metered: Boolean = false,
    val interfaceName: String? = null,
    val gateway: String? = null,
    val downstreamKbps: Int = 0,
    val upstreamKbps: Int = 0,
    val confidence: Double = 0.0,
    val observedAtMillis: Long = System.currentTimeMillis()
)

/** A stable, non-identifying representation used to suppress duplicate telemetry. */
fun NetworkTruth.telemetryFingerprint(): String = listOf(
    transport, internetState, lanState, budgetState, failureDomain, context,
    metered, interfaceName, gateway,
    PassiveLinkQualityPolicy.assess(this).quality
).joinToString("|")
