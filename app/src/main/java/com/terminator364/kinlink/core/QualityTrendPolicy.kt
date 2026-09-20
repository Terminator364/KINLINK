package com.terminator364.kinlink.core

enum class QualityTrend {
    INSUFFICIENT,
    IMPROVING,
    STABLE,
    DEGRADING
}

object QualityTrendPolicy {
    fun classify(qualityNamesOldestFirst: List<String>): QualityTrend {
        val ranks = qualityNamesOldestFirst.mapNotNull { rank(it) }
        if (ranks.size < 3) return QualityTrend.INSUFFICIENT
        val split = ranks.size / 2
        val first = ranks.take(split).average()
        val second = ranks.drop(split).average()
        val delta = second - first
        return when {
            delta >= 0.75 -> QualityTrend.IMPROVING
            delta <= -0.75 -> QualityTrend.DEGRADING
            else -> QualityTrend.STABLE
        }
    }

    private fun rank(name: String): Int? = when (name) {
        "CONSTRAINED" -> 1
        "LIMITED" -> 2
        "COMFORTABLE" -> 3
        else -> null
    }
}
