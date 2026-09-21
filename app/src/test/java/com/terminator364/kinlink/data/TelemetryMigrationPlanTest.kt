package com.terminator364.kinlink.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryMigrationPlanTest {
    @Test fun everyHistoricalVersionHasContiguousPathToCurrent() {
        for (version in 1 until TelemetryMigrationPlan.CURRENT_VERSION) {
            assertTrue("missing path from v$version", TelemetryMigrationPlan.isContiguousFrom(version))
        }
    }

    @Test fun v2ToV5ContainsAllRequiredSchemaAdditions() {
        val sql = TelemetryMigrationPlan.path(2).flatMap { it.sqlStatements }.joinToString("\n")
        assertTrue(sql.contains("quality_tier"))
        assertTrue(sql.contains("duration_ms"))
        assertTrue(sql.contains("ipv4_address"))
        assertTrue(sql.contains("ipv6_address"))
        assertTrue(sql.contains("ipv4_default_route"))
        assertTrue(sql.contains("ipv6_default_route"))
    }

    @Test fun v1ToV2DoesNotPrematurelyAddV4DurationColumn() {
        val sql = TelemetryMigrationPlan.path(1, 2).flatMap { it.sqlStatements }.joinToString("\n")
        assertFalse(sql.contains("duration_ms"))
    }

    @Test fun durationColumnAppearsOnlyAtV3ToV4() {
        val sql = TelemetryMigrationPlan.path(3, 4).flatMap { it.sqlStatements }.joinToString("\n")
        assertTrue(sql.contains("duration_ms"))
    }

    @Test fun currentVersionNeedsNoMigration() {
        assertEquals(emptyList<TelemetryMigrationStep>(), TelemetryMigrationPlan.path(5))
        assertTrue(TelemetryMigrationPlan.isContiguousFrom(5))
    }

    @Test fun downgradeAndUnknownVersionsAreRejected() {
        assertFalse(TelemetryMigrationPlan.isContiguousFrom(5, 4))
        assertFalse(TelemetryMigrationPlan.isContiguousFrom(0, 5))
        assertFalse(TelemetryMigrationPlan.isContiguousFrom(1, 6))
    }
}
