package com.terminator364.kinlink.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.terminator364.kinlink.core.NetworkStabilityPolicy
import com.terminator364.kinlink.core.QualityTrend
import com.terminator364.kinlink.core.QualityTrendPolicy
import com.terminator364.kinlink.core.PassiveLinkQualityPolicy
import com.terminator364.kinlink.core.NetworkTruth
import com.terminator364.kinlink.core.StabilityAssessment
import com.terminator364.kinlink.core.TelemetryRetentionPolicy
import com.terminator364.kinlink.core.RuntimeResourceVerdict
import com.terminator364.kinlink.core.HandoffKind
import com.terminator364.kinlink.core.HandoffOutcome
import com.terminator364.kinlink.core.QualificationReceiptNames
import com.terminator364.kinlink.core.FieldCandidateQualificationEvidence
import com.terminator364.kinlink.core.FieldCandidateQualificationPolicy
import java.util.UUID

data class StabilityWindow(
    val events: Int,
    val transitions: Int,
    val validatedEvents: Int,
    val assessment: StabilityAssessment
)

data class RecentReliabilityWindow(
    val interruptionCount: Int,
    val cumulativeMillis: Long,
    val longestMillis: Long,
    val dominantCause: String?,
    val lowQualityEpisodeCount: Int = 0,
    val lowQualityCumulativeMillis: Long = 0L,
    val lowQualityLongestMillis: Long = 0L,
    val qualityTrend: QualityTrend = QualityTrend.INSUFFICIENT
)

data class ActionReceipt(
    val tsWallMs: Long,
    val action: String,
    val success: Boolean,
    val summary: String
)

class TelemetryLedger(context: Context) : SQLiteOpenHelper(context, "kinlink_telemetry.db", null, 5) {
    companion object {
        private const val INCIDENT_PRE_WINDOW_MS = 5L * 60L * 1000L
    }
    override fun onCreate(db: SQLiteDatabase) {
        createNetworkEvents(db)
        createActionReceipts(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        check(TelemetryMigrationPlan.isContiguousFrom(oldVersion, newVersion)) {
            "Unsupported telemetry migration path v$oldVersion -> v$newVersion"
        }
        TelemetryMigrationPlan.path(oldVersion, newVersion).forEach { step ->
            step.sqlStatements.forEach { sql ->
                db.execSQL(sql)
            }
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
              quality_tier TEXT NOT NULL DEFAULT 'UNKNOWN',
              ipv4_address INTEGER NOT NULL DEFAULT 0,
              ipv6_address INTEGER NOT NULL DEFAULT 0,
              ipv4_default_route INTEGER NOT NULL DEFAULT 0,
              ipv6_default_route INTEGER NOT NULL DEFAULT 0
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
              summary TEXT NOT NULL,
              duration_ms INTEGER
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
              interface_name, gateway, failure_domain, confidence, quality_tier,
              ipv4_address, ipv6_address, ipv4_default_route, ipv6_default_route
            ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """.trimIndent(),
            arrayOf(
                UUID.randomUUID().toString(), truth.observedAtMillis, truth.transport.name,
                truth.internetState.name, truth.context.name, if (truth.metered) 1 else 0,
                null, null, truth.failureDomain.name, truth.confidence,
                PassiveLinkQualityPolicy.assess(truth).quality.name,
                if (truth.hasIpv4Address) 1 else 0,
                if (truth.hasIpv6Address) 1 else 0,
                if (truth.hasIpv4DefaultRoute) 1 else 0,
                if (truth.hasIpv6DefaultRoute) 1 else 0
            )
        )
        pruneNetworkEvents()
    }

    fun appendAction(
        action: String,
        success: Boolean,
        summary: String,
        durationMillis: Long? = null
    ) {
        val safeAction = action.take(64)
        val safeSummary = summary.replace("\n", " ").replace("\r", " ").take(320)
        writableDatabase.execSQL(
            """
            INSERT INTO action_receipts(receipt_id, ts_wall_ms, action, success, summary, duration_ms)
            VALUES(?,?,?,?,?,?)
            """.trimIndent(),
            arrayOf(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                safeAction,
                if (success) 1 else 0,
                safeSummary,
                durationMillis?.coerceAtLeast(0L)
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

    fun countSuccessfulActions(actionPrefix: String): Int =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM action_receipts WHERE action LIKE ? AND success = 1",
            arrayOf("$actionPrefix%")
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    fun actionCountsByPrefixSince(actionPrefix: String, sinceWallMs: Long): Map<String, Int> {
        val result = linkedMapOf<String, Int>()
        readableDatabase.rawQuery(
            "SELECT action, COUNT(*) FROM action_receipts WHERE action LIKE ? AND ts_wall_ms >= ? GROUP BY action",
            arrayOf("$actionPrefix%", sinceWallMs.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val action = cursor.getString(0)
                result[action.removePrefix(actionPrefix)] = cursor.getInt(1)
            }
        }
        return result
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

    private fun pruneNetworkEvents(nowWallMs: Long = System.currentTimeMillis()) {
        writableDatabase.execSQL(
            "DELETE FROM network_events WHERE ts_wall_ms < ?",
            arrayOf(TelemetryRetentionPolicy.networkCutoff(nowWallMs))
        )
        writableDatabase.execSQL(
            "DELETE FROM network_events WHERE event_id IN (SELECT event_id FROM network_events ORDER BY ts_wall_ms DESC LIMIT -1 OFFSET ${TelemetryRetentionPolicy.NETWORK_EVENT_MAX_ROWS})"
        )
    }

    private fun pruneActionReceipts(nowWallMs: Long = System.currentTimeMillis()) {
        val pinnedPatterns = TelemetryRetentionPolicy.PINNED_QUALIFICATION_ACTION_PATTERNS
        val pinnedClause = pinnedPatterns.joinToString(" AND ") { "action NOT LIKE ?" }

        writableDatabase.execSQL(
            "DELETE FROM action_receipts WHERE ts_wall_ms < ? AND $pinnedClause",
            arrayOf(
                TelemetryRetentionPolicy.actionCutoff(nowWallMs),
                *pinnedPatterns.toTypedArray()
            )
        )
        writableDatabase.execSQL(
            """
            DELETE FROM action_receipts
            WHERE receipt_id IN (
                SELECT receipt_id
                FROM action_receipts
                WHERE $pinnedClause
                ORDER BY ts_wall_ms DESC
                LIMIT -1 OFFSET ${TelemetryRetentionPolicy.ACTION_RECEIPT_MAX_ROWS}
            )
            """.trimIndent(),
            pinnedPatterns.toTypedArray()
        )
    }

    fun actionDurationStats(actionPrefix: String): Triple<Long, Long, Int> =
        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(duration_ms), 0), COALESCE(MAX(duration_ms), 0), COUNT(duration_ms) FROM action_receipts WHERE action LIKE ? AND duration_ms IS NOT NULL",
            arrayOf("$actionPrefix%")
        ).use { cursor ->
            cursor.moveToFirst()
            Triple(cursor.getLong(0), cursor.getLong(1), cursor.getInt(2))
        }

    fun interruptionDurationStatsSince(sinceWallMs: Long): Triple<Int, Long, Long> =
        readableDatabase.rawQuery(
            "SELECT COUNT(duration_ms), COALESCE(SUM(duration_ms), 0), COALESCE(MAX(duration_ms), 0) FROM action_receipts WHERE action LIKE 'INTERRUPTION_%' AND duration_ms IS NOT NULL AND ts_wall_ms >= ?",
            arrayOf(sinceWallMs.toString())
        ).use { cursor ->
            cursor.moveToFirst()
            Triple(cursor.getInt(0), cursor.getLong(1), cursor.getLong(2))
        }

    fun lowQualityDurationStatsSince(sinceWallMs: Long): Triple<Int, Long, Long> =
        readableDatabase.rawQuery(
            "SELECT COUNT(duration_ms), COALESCE(SUM(duration_ms), 0), COALESCE(MAX(duration_ms), 0) FROM action_receipts WHERE action LIKE 'LOW_QUALITY_EPISODE_%' AND duration_ms IS NOT NULL AND ts_wall_ms >= ?",
            arrayOf(sinceWallMs.toString())
        ).use { cursor ->
            cursor.moveToFirst()
            Triple(cursor.getInt(0), cursor.getLong(1), cursor.getLong(2))
        }

    fun lowQualityDurationStats(): Pair<Long, Long> =
        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(duration_ms), 0), COALESCE(MAX(duration_ms), 0) FROM action_receipts WHERE action LIKE 'LOW_QUALITY_EPISODE_%' AND duration_ms IS NOT NULL",
            null
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0) to cursor.getLong(1)
        }

    fun interruptionDurationStats(): Pair<Long, Long> =
        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(duration_ms), 0), COALESCE(MAX(duration_ms), 0) FROM action_receipts WHERE action LIKE 'INTERRUPTION_%' AND duration_ms IS NOT NULL",
            null
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0) to cursor.getLong(1)
        }

    fun recentQualityTrend(
        sinceWallMs: Long,
        limit: Int = 8
    ): QualityTrend {
        val names = mutableListOf<String>()
        val safeLimit = limit.coerceIn(3, 20)
        readableDatabase.rawQuery(
            "SELECT quality_tier FROM network_events WHERE ts_wall_ms >= ? ORDER BY ts_wall_ms DESC LIMIT ?",
            arrayOf(sinceWallMs.toString(), safeLimit.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) names += cursor.getString(0)
        }
        names.reverse()
        return QualityTrendPolicy.classify(names)
    }

    fun recentReliabilityWindow(
        nowMillis: Long = System.currentTimeMillis(),
        windowMillis: Long = 24L * 60L * 60L * 1000L
    ): RecentReliabilityWindow {
        val since = nowMillis - windowMillis
        val interruptions = interruptionDurationStatsSince(since)
        val lowQuality = lowQualityDurationStatsSince(since)
        val causes = actionCountsByPrefixSince("PASSIVE_CAUSE_", since)
        val dominant = causes.maxByOrNull { it.value }?.key
        return RecentReliabilityWindow(
            interruptionCount = interruptions.first,
            cumulativeMillis = interruptions.second,
            longestMillis = interruptions.third,
            dominantCause = dominant,
            lowQualityEpisodeCount = lowQuality.first,
            lowQualityCumulativeMillis = lowQuality.second,
            lowQualityLongestMillis = lowQuality.third,
            qualityTrend = recentQualityTrend(since)
        )
    }

    fun schemaIntegrityOk(): Boolean {
        fun columns(table: String): Set<String> {
            val result = linkedSetOf<String>()
            readableDatabase.rawQuery("PRAGMA table_info($table)", null).use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIndex >= 0) result += cursor.getString(nameIndex)
                }
            }
            return result
        }

        val networkColumns = columns("network_events")
        val actionColumns = columns("action_receipts")
        return networkColumns.containsAll(
            setOf(
                "event_id",
                "ts_wall_ms",
                "quality_tier",
                "ipv4_address",
                "ipv6_address",
                "ipv4_default_route",
                "ipv6_default_route"
            )
        ) && actionColumns.containsAll(
            setOf("receipt_id", "ts_wall_ms", "action", "success", "summary", "duration_ms")
        )
    }

    fun schemaVersion(): Int = readableDatabase.version

    fun recentCount(): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM network_events", null
    ).use { cursor ->
        cursor.moveToFirst()
        cursor.getInt(0)
    }

    fun recentActionsSince(sinceWallMs: Long, limit: Int = 30): List<ActionReceipt> {
        val safeLimit = limit.coerceIn(1, 100)
        val result = mutableListOf<ActionReceipt>()
        readableDatabase.rawQuery(
            "SELECT ts_wall_ms, action, success, summary FROM action_receipts WHERE ts_wall_ms >= ? ORDER BY ts_wall_ms ASC LIMIT ?",
            arrayOf(sinceWallMs.toString(), safeLimit.toString())
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

    fun diagnosticSummary(
        currentTruth: NetworkTruth,
        recoveryMode: String = "UNKNOWN",
        runningVersionCode: Long? = null
    ): DiagnosticSummary {
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

        val now = System.currentTimeMillis()
        val latestIncidentMarker = latestActionTimestamp("USER_INCIDENT_MARKER")
        val incidentWindowActions = latestIncidentMarker?.let {
            recentActionsSince((it - INCIDENT_PRE_WINDOW_MS).coerceAtLeast(0L), 50)
        } ?: emptyList()

        val interruptionDurations = interruptionDurationStats()
        val lowQualityDurations = lowQualityDurationStats()
        val interruptions1h = interruptionDurationStatsSince(now - 60L * 60L * 1000L)
        val interruptions24h = interruptionDurationStatsSince(now - 24L * 60L * 60L * 1000L)
        val lowQuality24h = lowQualityDurationStatsSince(now - 24L * 60L * 60L * 1000L)
        val causes24h = actionCountsByPrefixSince("PASSIVE_CAUSE_", now - 24L * 60L * 60L * 1000L)
        val recoveryDurations = actionDurationStats("AUTO_RECOVERY")
        val stability = stabilityWindow(now)
        val fieldAssessment = runningVersionCode?.takeIf { it >= 8L }?.let { version ->
            FieldCandidateQualificationPolicy.evaluate(
                FieldCandidateQualificationEvidence(
                    coreSelfTestPasses =
                        countSuccessfulActions(QualificationReceiptNames.coreSelfTest(version)),
                    observerSelfTestPasses =
                        countSuccessfulActions(QualificationReceiptNames.observerSelfTest(version)),
                    mobileValidatedHandoffs =
                        countSuccessfulActions(
                            QualificationReceiptNames.handoffOutcome(
                                HandoffOutcome.MOBILE_VALIDATED,
                                version
                            )
                        ),
                    cellularToWifiReturns =
                        countSuccessfulActions(
                            QualificationReceiptNames.handoff(
                                HandoffKind.CELLULAR_TO_WIFI,
                                version
                            )
                        ),
                    runtimeResourcePasses =
                        countSuccessfulActions(
                            QualificationReceiptNames.resourceGate(
                                RuntimeResourceVerdict.PASS,
                                version
                            )
                        ),
                    runtimeResourceBlocks =
                        countActions(
                            QualificationReceiptNames.resourceGate(
                                RuntimeResourceVerdict.BLOCKED,
                                version
                            )
                        ),
                    latestRuntimeResourcePassMillis =
                        latestActionTimestamp(
                            QualificationReceiptNames.resourceGate(
                                RuntimeResourceVerdict.PASS,
                                version
                            )
                        ),
                    latestRuntimeResourceBlockMillis =
                        latestActionTimestamp(
                            QualificationReceiptNames.resourceGate(
                                RuntimeResourceVerdict.BLOCKED,
                                version
                            )
                        )
                )
            )
        }
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
            totalInterruptionMillis = interruptionDurations.first,
            longestInterruptionMillis = interruptionDurations.second,
            lowQualityEpisodes = countActions("LOW_QUALITY_EPISODE_"),
            totalLowQualityMillis = lowQualityDurations.first,
            longestLowQualityMillis = lowQualityDurations.second,
            passiveCauseCounts = actionCountsByPrefix("PASSIVE_CAUSE_"),
            coreSelfTestPasses = countSuccessfulActions("SELF_TEST_CORE"),
            observerSelfTestPasses = countSuccessfulActions("SELF_TEST_OBSERVER_CALLBACK"),
            runtimeBudgetSessions = countActions("RUNTIME_BUDGET_SESSION"),
            recoveryActionDurationTotalMillis = recoveryDurations.first,
            recoveryActionDurationMaxMillis = recoveryDurations.second,
            recoveryActionDurationSamples = recoveryDurations.third,
            recent1hInterruptionCount = interruptions1h.first,
            recent1hInterruptionMillis = interruptions1h.second,
            recent1hLongestInterruptionMillis = interruptions1h.third,
            recent24hInterruptionCount = interruptions24h.first,
            recent24hInterruptionMillis = interruptions24h.second,
            recent24hLongestInterruptionMillis = interruptions24h.third,
            recent24hCauseCounts = causes24h,
            recent24hLowQualityEpisodeCount = lowQuality24h.first,
            recent24hLowQualityMillis = lowQuality24h.second,
            recent24hLowQualityLongestMillis = lowQuality24h.third,
            userIncidentMarkers = countActions("USER_INCIDENT_MARKER"),
            latestUserIncidentMarkerMillis = latestIncidentMarker,
            incidentWindowActions = incidentWindowActions,
            manualWifiDiagnosisCounts = actionCountsByPrefix("MANUAL_WIFI_DIAG_"),
            runtimeResourcePasses = countActions("RUNTIME_RESOURCE_GATE_PASS"),
            runtimeResourceInconclusive = countActions("RUNTIME_RESOURCE_GATE_INCONCLUSIVE"),
            runtimeResourceBlocked = countActions("RUNTIME_RESOURCE_GATE_BLOCKED"),
            fieldCandidateQualifiedReceipts = runningVersionCode?.let {
                countSuccessfulActions(QualificationReceiptNames.fieldQualified(it))
            } ?: 0,
            fieldCandidateBlockedReceipts = runningVersionCode?.let {
                countActions(QualificationReceiptNames.fieldBlocked(it))
            } ?: 0,
            cellularToWifiReturns = runningVersionCode?.let {
                countSuccessfulActions(
                    QualificationReceiptNames.handoff(HandoffKind.CELLULAR_TO_WIFI, it)
                )
            } ?: 0,
            currentFieldQualificationVerdict = fieldAssessment?.verdict?.name ?: "NOT_APPLICABLE",
            currentFieldQualificationMissing = fieldAssessment?.missing ?: emptySet()
        )
    }
}
