package com.terminator364.kinlink.data

data class TelemetryMigrationStep(
    val fromVersion: Int,
    val toVersion: Int,
    val sqlStatements: List<String>
)

object TelemetryMigrationPlan {
    const val CURRENT_VERSION = 5

    val steps: List<TelemetryMigrationStep> = listOf(
        TelemetryMigrationStep(
            1, 2,
            listOf(
                """CREATE TABLE IF NOT EXISTS action_receipts (
                  receipt_id TEXT PRIMARY KEY,
                  ts_wall_ms INTEGER NOT NULL,
                  action TEXT NOT NULL,
                  success INTEGER NOT NULL,
                  summary TEXT NOT NULL,
                  duration_ms INTEGER
                )""".trimIndent(),
                "CREATE INDEX IF NOT EXISTS idx_action_receipts_ts ON action_receipts(ts_wall_ms)"
            )
        ),
        TelemetryMigrationStep(
            2, 3,
            listOf(
                "ALTER TABLE network_events ADD COLUMN quality_tier TEXT NOT NULL DEFAULT 'UNKNOWN'"
            )
        ),
        TelemetryMigrationStep(
            3, 4,
            listOf(
                "ALTER TABLE action_receipts ADD COLUMN duration_ms INTEGER"
            )
        ),
        TelemetryMigrationStep(
            4, 5,
            listOf(
                "ALTER TABLE network_events ADD COLUMN ipv4_address INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE network_events ADD COLUMN ipv6_address INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE network_events ADD COLUMN ipv4_default_route INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE network_events ADD COLUMN ipv6_default_route INTEGER NOT NULL DEFAULT 0"
            )
        )
    )

    fun path(oldVersion: Int, newVersion: Int = CURRENT_VERSION): List<TelemetryMigrationStep> {
        if (oldVersion < 1 || newVersion > CURRENT_VERSION || oldVersion > newVersion) return emptyList()
        return steps.filter { it.fromVersion >= oldVersion && it.toVersion <= newVersion }
    }

    fun isContiguousFrom(oldVersion: Int, newVersion: Int = CURRENT_VERSION): Boolean {
        if (oldVersion == newVersion) return true
        val path = path(oldVersion, newVersion)
        if (path.isEmpty()) return false
        var expected = oldVersion
        for (step in path) {
            if (step.fromVersion != expected || step.toVersion != expected + 1) return false
            expected = step.toVersion
        }
        return expected == newVersion
    }
}
