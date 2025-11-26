package com.akinalpfdn.poprush.core.domain.repository

import com.akinalpfdn.poprush.core.domain.model.GameResult
import com.akinalpfdn.poprush.core.domain.model.GameStatistics
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing game data persistence and retrieval.
 * Handles high scores, game statistics, and historical game data.
 */
interface GameRepository {

    /**
     * Saves a completed game result to persistent storage.
     *
     * @param result The game result to save
     */
    suspend fun saveGameResult(result: GameResult)

    /**
     * Retrieves the current high score.
     *
     * @return The highest score achieved, or 0 if no games played
     */
    suspend fun getHighScore(): Int

    /**
     * Updates the high score if the new score is higher.
     *
     * @param newScore The new score to potentially set as high score
     * @return True if high score was updated, false otherwise
     */
    suspend fun updateHighScore(newScore: Int): Boolean

    /**
     * Gets a stream of high score updates.
     *
     * @return Flow that emits high score changes
     */
    fun getHighScoreFlow(): Flow<Int>

    /**
     * Retrieves the last N game results for statistics.
     *
     * @param limit Maximum number of results to retrieve
     * @return List of recent game results
     */
    suspend fun getRecentGameResults(limit: Int = 10): List<GameResult>

    /**
     * Gets all-time game statistics.
     *
     * @return GameStatistics containing aggregated data
     */
    suspend fun getGameStatistics(): GameStatistics

    /**
     * Clears all saved game data (for testing or reset purposes).
     */
    suspend fun clearAllGameData()

    /**
     * Gets the total number of games played.
     *
     * @return Total games count
     */
    suspend fun getTotalGamesPlayed(): Int

    /**
     * Gets the best performance rating achieved.
     *
     * @return The best PerformanceRating, or null if no games played
     */
    suspend fun getBestPerformanceRating(): String?
}