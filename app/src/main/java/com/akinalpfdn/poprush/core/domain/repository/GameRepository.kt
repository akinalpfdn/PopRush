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
     * Retrieves the high score for a specific game mode.
     *
     * @param modKey The mode key (e.g., "classic", "speed")
     * @return The highest score achieved for that mode, or 0 if no games played
     */
    suspend fun getHighScore(modKey: String): Int

    /**
     * Updates the high score for a specific game mode if the new score is higher.
     *
     * @param modKey The mode key (e.g., "classic", "speed")
     * @param newScore The new score to potentially set as high score
     * @return True if high score was updated, false otherwise
     */
    suspend fun updateHighScore(modKey: String, newScore: Int): Boolean

    /**
     * Gets a stream of high score updates for a specific game mode.
     *
     * @param modKey The mode key (e.g., "classic", "speed")
     * @return Flow that emits high score changes
     */
    fun getHighScoreFlow(modKey: String): Flow<Int>

    /**
     * Gets all high scores for all game modes as a reactive map.
     *
     * @return Flow emitting map of modKey to highScore
     */
    fun getAllHighScores(): Flow<Map<String, Int>>

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