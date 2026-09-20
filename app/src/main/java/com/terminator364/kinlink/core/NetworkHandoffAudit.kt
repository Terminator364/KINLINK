package com.terminator364.kinlink.core

enum class HandoffKind {
    WIFI_TO_CELLULAR,
    WIFI_TO_NONE,
    CELLULAR_TO_WIFI,
    NONE_TO_CELLULAR,
    OTHER
}

data class HandoffTransition(
    val from: Transport,
    val to: Transport,
    val kind: HandoffKind,
    val summary: String
)

/**
 * Passive transition auditor only.
 *
 * This class never requests, binds, validates, disables or enables a network.
 * It exists solely to leave local evidence when Android changes transports.
 */
class NetworkHandoffAudit {
    private var previous: Transport? = null

    fun observe(current: Transport): HandoffTransition? {
        val before = previous
        previous = current
        if (before == null || before == current) return null

        val kind = when {
            before == Transport.WIFI && current == Transport.CELLULAR -> HandoffKind.WIFI_TO_CELLULAR
            before == Transport.WIFI && current == Transport.NONE -> HandoffKind.WIFI_TO_NONE
            before == Transport.CELLULAR && current == Transport.WIFI -> HandoffKind.CELLULAR_TO_WIFI
            before == Transport.NONE && current == Transport.CELLULAR -> HandoffKind.NONE_TO_CELLULAR
            else -> HandoffKind.OTHER
        }

        val summary = when (kind) {
            HandoffKind.WIFI_TO_CELLULAR ->
                "Android a basculé du Wi-Fi vers le mobile; KINLINK observe uniquement et n’applique aucune action réseau."
            HandoffKind.WIFI_TO_NONE ->
                "Le Wi-Fi a disparu; KINLINK libère toute logique de récupération et attend Android."
            HandoffKind.CELLULAR_TO_WIFI ->
                "Android a basculé du mobile vers le Wi-Fi; KINLINK reprend uniquement l’observation Wi-Fi."
            HandoffKind.NONE_TO_CELLULAR ->
                "Android a activé le transport mobile après absence de réseau; KINLINK n’intervient pas."
            HandoffKind.OTHER ->
                "Transition réseau observée: ${before.name} -> ${current.name}; aucune prise de contrôle."
        }

        return HandoffTransition(before, current, kind, summary)
    }
}
