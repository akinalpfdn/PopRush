package com.akinalpfdn.poprush.core.domain.model

/**
 * Represents comprehensive game statistics for tracking player performance.
 */
data class GameStatistics(
    val totalGamesPlayed: Int = 0,
    val totalScore: Int = 0,
    val averageScore: Float = 0f,
    val highestScore: Int = 0,
    val totalBubblesPressed: Int = 0,
    val averageAccuracy: Float = 0f,
    val bestPerformanceRating: String = "NEEDS_PRACTICE",
    val totalTimePlayed: kotlin.time.Duration = kotlin.time.Duration.ZERO,
    val averageGameDuration: kotlin.time.Duration = kotlin.time.Duration.ZERO
)