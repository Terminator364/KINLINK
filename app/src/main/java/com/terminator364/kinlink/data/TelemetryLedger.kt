package com.terminator364.kinlink.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.terminator364.kinlink.core.NetworkStabilityPolicy
import com.terminator364.kinlink.core.PassiveLinkQualityPolicy
import com.terminator364.kinlink.core.NetworkTruth
import com.terminator364.kinlink.core.StabilityAssessment
import java.util.UUID

data class StabilityWindow(
    val events: Int,
    val transitions: Int,
    val validatedEvents: Int,
    val assessment: StabilityAssessment
)

data class ActionReceipt(
    val tsWallMs: Long,
    val action: String,
    val success: Boolean,
    val summary: String
)

class TelemetryLedger(context: Context) : SQLiteOpenHelper(context, "kinlink_telemetry.db", null, 3) {
    override fun onCreate(db: SQLiteDatabase) {
        createNetworkEvents(db)
        createActionReceipts(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createActionReceipts(db)
        if (oldVersion < 3) {
            runCatching { db.execSQL("ALTER TABLE network_events ADD COLUMN quality_tier TEXT NOT NULL DEFAULT 'UNKNOWN'") }
        }
    }

    private fun createNetworkEvents(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS network_events (
              event_id TEXT PRIMARY KEY,
              ts_wall_ms INTEGER NOT NULL,
              transport TEXT NOT NULL,
              internet_state TEXT NOT NULL,
              context_type TEXT NOT NULL,
              metered INTEGER NOT NULL,
              interface_name TEXT,
              gateway TEXT,
              failure_domain TEXT NOT NULL,
              confidence REAL NOT NULL,
              quality_tier TEXT NOT NULL DEFAULT 'UNKNOWN'
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_network_events_ts ON network_events(ts_wall_ms)")
    }

    private fun createActionReceipts(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS action_receipts (
              receipt_id TEXT PRIMARY KEY,
              ts_wall_ms INTEGER NOT NULL,
              action TEXT NOT NULL,
              success INTEGER NOT NULL,
              summary TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_action_receipts_ts ON action_receipts(ts_wall_ms)")
    }

    fun append(truth: NetworkTruth) {
        writableDatabase.execSQL(
            """
            INSERT INTO network_events(
              event_id, ts_wall_ms, transport, internet_state, context_type, metered,
              interface_name, gateway, failure_domain, confidence, quality_tier
            ) VALUES(?,?,?,?,?,?,?,?,?,?,?)
            """.trimIndent(),
            arrayOf(
                UUID.randomUUID().toString(), truth.observedAtMillis, truth.transport.name,
                truth.internetState.name, truth.context.name, if (truth.metered) 1 else 0,
                truth.interfaceName, truth.gateway, truth.failureDomain.name, truth.confidence,
                PassiveLinkQualityPolicy.assess(truth).quality.name
            )
        )
        pruneNetworkEvents()
    }

    fun appendAction(action: String, success: Boolean, summary: String) {
        val safeAction = action.take(64)
        val safeSummary = summary.replace("\n", " ").replace("\r", " ").take(320)
        writableDatabase.execSQL(
            """
            INSERT INTO action_receipts(receipt_id, ts_wall_ms, action, success, summary)
            VALUES(?,?,?,?,?)
            """.trimIndent(),
            arrayOf(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                safeAction,
                if (success) 1 else 0,
                safeSummary
            )
        )
        pruneActionReceipts()
    }

    fun countActionsSince(actionPrefix: String, sinceWallMs: Long): Int =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM action_receipts WHERE action LIKE ? AND ts_wall_ms >= ?",
            arrayOf("$actionPrefix%", sinceWallMs.toString())
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    fun countActions(actionPrefix: String): Int =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM action_receipts WHERE action LIKE ?",
            arrayOf("$actionPrefix%")
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    fun actionCountsByPrefix(actionPrefix: String): Map<String, Int> {
        val result = linkedMapOf<String, Int>()
        readableDatabase.rawQuery(
            "SELECT action, COUNT(*) FROM action_receipts WHERE action LIKE ? GROUP BY action",
            arrayOf("$actionPrefix%")
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val action = cursor.getString(0)
                result[action.removePrefix(actionPrefix)] = cursor.getInt(1)
            }
        }
        return result
    }

    fun latestActionTimestamp(actionPrefix: String): Long? =
        readableDatabase.rawQuery(
            "SELECT ts_wall_ms FROM action_receipts WHERE action LIKE ? ORDER BY ts_wall_ms DESC LIMIT 1",
            arrayOf("$actionPrefix%")
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else null
        }

    private fun pruneNetworkEvents() {
        writableDatabase.execSQL(
            "DELETE FROM network_events WHERE event_id IN (SELECT event_id FROM network_events ORDER BY ts_wall_ms DESC LIMIT -1 OFFSET 5000)"
        )
    }

    private fun pruneActionReceipts() {
        writableDatabase.execSQL(
            "DELETE FROM action_receipts WHERE receipt_id IN (SELECT receipt_id FROM action_receipts ORDER BY ts_wall_ms DESC LIMIT -1 OFFSET 500)"
        )
    }

    fun recentCount(): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM network_events", null
    ).use { cursor ->
        cursor.moveToFirst()
        cursor.getInt(0)
    }

    fun recentActions(limit: Int = 10): List<ActionReceipt> {
        val safeLimit = limit.coerceIn(1, 50)
        val result = mutableListOf<ActionReceipt>()
        readableDatabase.rawQuery(
            "SELECT ts_wall_ms, action, success, summary FROM action_receipts ORDER BY ts_wall_ms DESC LIMIT ?",
            arrayOf(safeLimit.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += ActionReceipt(
                    tsWallMs = cursor.getLong(0),
                    action = cursor.getString(1),
                    success = cursor.getInt(2) != 0,
                    summary = cursor.getString(3)
                )
            }
        }
        return result
    }

    fun stabilityWindow(
        nowMillis: Long = System.currentTimeMillis(),
        windowMillis: Long = 15L * 60L * 1000L
    ): StabilityWindow {
        val since = nowMillis - windowMillis
        var events = 0
        var transitions = 0
        var validated = 0
        var previousState: String? = null
        readableDatabase.rawQuery(
            "SELECT internet_state FROM network_events WHERE ts_wall_ms >= ? ORDER BY ts_wall_ms ASC",
            arrayOf(since.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val state = cursor.getString(0)
                events += 1
                if (state == "VALIDATED") validated += 1
                if (previousState != null && previousState != state) transitions += 1
                previousState = state
            }
        }
        return StabilityWindow(
            events,
            transitions,
            validated,
            NetworkStabilityPolicy.assess(events, transitions, validated)
        )
    }

    fun diagnosticSummary(currentTruth: NetworkTruth, recoveryMode: String = "UNKNOWN"): DiagnosticSummary {
        val weekStartMillis = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        val weeklyEvents = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM network_events WHERE ts_wall_ms >= ?",
            arrayOf(weekStartMillis.toString())
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

        val counts = linkedMapOf<String, Int>()
        readableDatabase.rawQuery(
            "SELECT transport || '_' || internet_state, COUNT(*) FROM network_events GROUP BY transport, internet_state",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) counts[cursor.getString(0)] = cursor.getInt(1)
        }

        val qualityCounts = linkedMapOf<String, Int>()
        readableDatabase.rawQuery(
            "SELECT quality_tier, COUNT(*) FROM network_events GROUP BY quality_tier",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) qualityCounts[cursor.getString(0)] = cursor.getInt(1)
        }

        val stability = stabilityWindow()
        return DiagnosticSummary(
            generatedAtMillis = System.currentTimeMillis(),
            totalEvents = recentCount(),
            currentTruth = currentTruth,
            stateCounts = counts,
            qualityCounts = qualityCounts,
            weeklyEvents = weeklyEvents,
            recentTransitions = stability.transitions,
            instabilityScore = stability.assessment.score,
            flapping = stability.assessment.flapping,
            recentActions = recentActions(),
            handoffEvents = countActions("HANDOFF_"),
            mobileValidatedOutcomes = countActions("HANDOFF_OUTCOME_MOBILE_VALIDATED"),
            mobilePendingOutcomes = countActions("HANDOFF_OUTCOME_MOBILE_PRESENT_UNVALIDATED"),
            watchdogAborts = countActions("AUTO_RECOVERY_WATCHDOG_"),
            recoveryMode = recoveryMode,
            recoveryImproved = countActions("RECOVERY_OUTCOME_IMPROVED"),
            recoveryUnchanged = countActions("RECOVERY_OUTCOME_UNCHANGED"),
            recoveryDegraded = countActions("RECOVERY_OUTCOME_DEGRADED"),
            recoveryInconclusive = countActions("RECOVERY_OUTCOME_INCONCLUSIVE"),
            microInterruptions = countActions("INTERRUPTION_MICRO"),
            shortInterruptions = countActions("INTERRUPTION_SHORT"),
            longInterruptions = countActions("INTERRUPTION_LONG"),
            passiveCauseCounts = actionCountsByPrefix("PASSIVE_CAUSE_")
        )
    }
}
