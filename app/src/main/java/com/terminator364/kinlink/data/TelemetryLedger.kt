package com.terminator364.kinlink.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.terminator364.kinlink.core.NetworkTruth
import java.util.UUID

class TelemetryLedger(context: Context) : SQLiteOpenHelper(context, "kinlink_telemetry.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE network_events (
              event_id TEXT PRIMARY KEY,
              ts_wall_ms INTEGER NOT NULL,
              transport TEXT NOT NULL,
              internet_state TEXT NOT NULL,
              context_type TEXT NOT NULL,
              metered INTEGER NOT NULL,
              interface_name TEXT,
              gateway TEXT,
              failure_domain TEXT NOT NULL,
              confidence REAL NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_network_events_ts ON network_events(ts_wall_ms)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    /** Stores only local metadata. No SSID, BSSID, payload or SIM identifier is recorded. */
    fun append(truth: NetworkTruth) {
        writableDatabase.execSQL(
            """
            INSERT INTO network_events(
              event_id, ts_wall_ms, transport, internet_state, context_type, metered,
              interface_name, gateway, failure_domain, confidence
            ) VALUES(?,?,?,?,?,?,?,?,?,?)
            """.trimIndent(),
            arrayOf(
                UUID.randomUUID().toString(), truth.observedAtMillis, truth.transport.name,
                truth.internetState.name, truth.context.name, if (truth.metered) 1 else 0,
                truth.interfaceName, truth.gateway, truth.failureDomain.name, truth.confidence
            )
        )
        prune()
    }

    private fun prune() {
        writableDatabase.execSQL(
            "DELETE FROM network_events WHERE event_id IN (SELECT event_id FROM network_events ORDER BY ts_wall_ms DESC LIMIT -1 OFFSET 5000)"
        )
    }

    fun recentCount(): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM network_events", null
    ).use { cursor -> cursor.moveToFirst(); cursor.getInt(0) }

    fun diagnosticSummary(currentTruth: NetworkTruth): DiagnosticSummary {
        val counts = linkedMapOf<String, Int>()
        readableDatabase.rawQuery(
            "SELECT transport || '_' || internet_state, COUNT(*) FROM network_events GROUP BY transport, internet_state",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                counts[cursor.getString(0)] = cursor.getInt(1)
            }
        }
        return DiagnosticSummary(
            generatedAtMillis = System.currentTimeMillis(),
            totalEvents = recentCount(),
            currentTruth = currentTruth,
            stateCounts = counts
        )
    }
}
