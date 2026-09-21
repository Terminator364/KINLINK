package com.terminator364.kinlink.core

data class AutopilotControlTuning(
    val vigilanceFloor: Int,
    val mobileAssistCooldownMs: Long,
    val mobileAssistMaxActionsPerHour: Int
)

object AutopilotProfileControlPolicy {
    fun tuning(profile: AutopilotProfile): AutopilotControlTuning = when (profile) {
        AutopilotProfile.CONSERVATIVE -> AutopilotControlTuning(
            vigilanceFloor = 55,
            mobileAssistCooldownMs = 10L * 60L * 1000L,
            mobileAssistMaxActionsPerHour = 3
        )
        AutopilotProfile.BALANCED -> AutopilotControlTuning(
            vigilanceFloor = 65,
            mobileAssistCooldownMs = 5L * 60L * 1000L,
            mobileAssistMaxActionsPerHour = 6
        )
        AutopilotProfile.MAXIMUM_STABILITY -> AutopilotControlTuning(
            vigilanceFloor = 75,
            mobileAssistCooldownMs = 3L * 60L * 1000L,
            mobileAssistMaxActionsPerHour = 8
        )
    }
}
