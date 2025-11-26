package com.akinalpfdn.poprush.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a high score record in the database.
 * Stores high score information with metadata for statistics.
 */
@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * The score achieved in the game.
     */
    val score: Int,

    /**
     * Number of levels completed in this game.
     */
    val levelsCompleted: Int,

    /**
     * Total number of bubbles pressed.
     */
    val totalBubblesPressed: Int,

    /**
     * Accuracy percentage (correct presses / total presses).
     */
    val accuracyPercentage: Float,

    /**
     * Difficulty level the game was played at.
     */
    val difficulty: String,

    /**
     * Duration of the game in milliseconds.
     */
    val gameDurationMs: Long,

    /**
     * When the high score was achieved (timestamp).
     */
    val timestamp: Long = System.currentTimeMillis(),

    /**
     * Performance rating achieved.
     */
    val performanceRating: String
) {
    companion object {
        /**
         * Creates a HighScoreEntity from game result data.
         */
        fun fromGameResult(
            score: Int,
            levelsCompleted: Int,
            totalBubblesPressed: Int,
            accuracyPercentage: Float,
            difficulty: String,
            gameDurationMs: Long,
            performanceRating: String
        ): HighScoreEntity {
            return HighScoreEntity(
                score = score,
                levelsCompleted = levelsCompleted,
                totalBubblesPressed = totalBubblesPressed,
                accuracyPercentage = accuracyPercentage,
                difficulty = difficulty,
                gameDurationMs = gameDurationMs,
                performanceRating = performanceRating
            )
        }
    }
}