package com.akinalpfdn.poprush.core.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents the final result of a completed game session.
 * This is used for high score tracking and game analytics.
 *
 * @param finalScore The final score achieved
 * @param levelsCompleted Number of levels successfully completed
 * @param totalBubblesPressed Total number of bubbles pressed during the game
 * @param accuracyPercentage Accuracy of bubble pressing (correct / total attempts)
 * @param averageTimePerLevel Average time spent per level
 * @param gameDuration Total duration of the game session
 * @param difficulty The difficulty level played at
 * @param isHighScore Whether this result achieved a new high score
 * @param timestamp When the game was completed
 */
data class GameResult(
    val finalScore: Int,
    val levelsCompleted: Int,
    val totalBubblesPressed: Int,
    val accuracyPercentage: Float,
    val averageTimePerLevel: Duration,
    val gameDuration: Duration,
    val difficulty: GameDifficulty = GameDifficulty.NORMAL,
    val isHighScore: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calculates the performance rating based on score and accuracy.
     */
    val performanceRating: PerformanceRating
        get() = when {
            accuracyPercentage >= 95f && finalScore >= 50 -> PerformanceRating.PERFECT
            accuracyPercentage >= 90f && finalScore >= 40 -> PerformanceRating.EXCELLENT
            accuracyPercentage >= 80f && finalScore >= 30 -> PerformanceRating.GREAT
            accuracyPercentage >= 70f && finalScore >= 20 -> PerformanceRating.GOOD
            accuracyPercentage >= 60f && finalScore >= 10 -> PerformanceRating.FAIR
            else -> PerformanceRating.NEEDS_PRACTICE
        }

    /**
     * Calculates the bubbles per minute rate.
     */
    val bubblesPerMinute: Float
        get() = if (gameDuration.inWholeSeconds > 0) {
            (totalBubblesPressed.toFloat() / gameDuration.inWholeSeconds) * 60f
        } else 0f

    /**
     * Determines if the result qualifies for any achievements.
     */
    val potentialAchievements: List<AchievementType>
        get() = mutableListOf<AchievementType>().apply {
            if (isHighScore) add(AchievementType.NEW_HIGH_SCORE)
            if (accuracyPercentage == 100f && totalBubblesPressed > 0) add(AchievementType.PERFECT_ACCURACY)
            if (finalScore >= 100) add(AchievementType.CENTURY_SCORER)
            if (levelsCompleted >= 20) add(AchievementType.SPEED_DEMON)
            if (difficulty == GameDifficulty.EXPERT && finalScore >= 30) add(AchievementType.EXTREME_MASTER)
        }
}

/**
 * Performance rating enum for categorizing game results.
 */
enum class PerformanceRating(
    val displayName: String,
    val color: String
) {
    PERFECT("Perfect", "#FFD700"),
    EXCELLENT("Excellent", "#C0C0C0"),
    GREAT("Great", "#CD7F32"),
    GOOD("Good", "#32CD32"),
    FAIR("Fair", "#87CEEB"),
    NEEDS_PRACTICE("Needs Practice", "#FF6B6B")
}

/**
 * Achievement types for future gamification features.
 */
enum class AchievementType(
    val displayName: String,
    val description: String,
    val icon: String
) {
    NEW_HIGH_SCORE("New High Score", "Achieved a new personal best", "🏆"),
    PERFECT_ACCURACY("Perfect Accuracy", "100% accuracy in a game", "🎯"),
    CENTURY_SCORER("Century Scorer", "Scored 100+ points in a single game", "💯"),
    SPEED_DEMON("Speed Demon", "Completed 20+ levels in one game", "⚡"),
    EXTREME_MASTER("Extreme Master", "Scored 30+ points on extreme difficulty", "🔥")
}