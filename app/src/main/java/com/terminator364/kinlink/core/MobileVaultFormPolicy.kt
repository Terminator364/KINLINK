package com.terminator364.kinlink.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId

object MobileVaultFormPolicy {
    private val SIZE = Regex("""^\s*([0-9]+(?:[.,][0-9]+)?)\s*(MB|GB)?\s*$""", RegexOption.IGNORE_CASE)

    fun parseDecimalBytes(text: String): Long? {
        val match = SIZE.matchEntire(text) ?: return null
        val number = match.groupValues[1].replace(',', '.')
        val unit = match.groupValues[2].uppercase().ifBlank { "MB" }
        val multiplier = when (unit) {
            "MB" -> BigDecimal("1000000")
            "GB" -> BigDecimal("1000000000")
            else -> return null
        }
        return runCatching {
            BigDecimal(number)
                .multiply(multiplier)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        }.getOrNull()?.takeIf { it >= 0L }
    }

    fun formatEditable(bytes: Long?): String {
        val value = bytes ?: return ""
        if (value < 0L) return ""
        return if (value >= 1_000_000_000L && value % 100_000_000L == 0L) {
            BigDecimal(value)
                .divide(BigDecimal("1000000000"), 1, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + " GB"
        } else {
            BigDecimal(value)
                .divide(BigDecimal("1000000"), 0, RoundingMode.HALF_UP)
                .toPlainString() + " MB"
        }
    }

    fun parseInclusiveExpiryDate(
        text: String,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        return runCatching {
            LocalDate.parse(trimmed)
                .plusDays(1)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    fun formatInclusiveExpiryDate(
        expiryAtEpochMillis: Long?,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val value = expiryAtEpochMillis ?: return ""
        if (value <= 0L) return ""
        return runCatching {
            java.time.Instant.ofEpochMilli(value - 1L)
                .atZone(zoneId)
                .toLocalDate()
                .toString()
        }.getOrDefault("")
    }

    fun parseCycleStartDate(
        text: String,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        return runCatching {
            LocalDate.parse(trimmed)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    fun formatCycleStartDate(
        cycleStartAtEpochMillis: Long?,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val value = cycleStartAtEpochMillis ?: return ""
        if (value <= 0L) return ""
        return runCatching {
            java.time.Instant.ofEpochMilli(value)
                .atZone(zoneId)
                .toLocalDate()
                .toString()
        }.getOrDefault("")
    }
}
