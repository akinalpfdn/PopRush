package com.akinalpfdn.poprush.core.data.repository

import com.akinalpfdn.poprush.core.data.local.GamePreferences
import com.akinalpfdn.poprush.core.domain.model.GameResult
import com.akinalpfdn.poprush.core.domain.model.GameStatistics
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GameRepository using DataStore for persistence.
 * Handles high scores and game statistics.
 */
@Singleton
class GameRepositoryImpl @Inject constructor(
    private val gamePreferences: GamePreferences
) : GameRepository {

    override suspend fun saveGameResult(result: GameResult) {
        // In a full implementation, this would save detailed game statistics
        // For now, we'll just handle high score tracking
        gamePreferences.incrementGamesPlayed()
    }

    override suspend fun getHighScore(): Int {
        return try {
            gamePreferences.getHighScore().first()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun updateHighScore(newScore: Int): Boolean {
        return try {
            val currentHighScore = getHighScore()
            if (newScore > currentHighScore) {
                gamePreferences.saveHighScore(newScore)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun getHighScoreFlow(): Flow<Int> {
        return gamePreferences.getHighScore().map { it ?: 0 }
    }

    override suspend fun getRecentGameResults(limit: Int): List<GameResult> {
        // In a full implementation, this would query a database for recent results
        // For now, return empty list as we're not persisting detailed results
        return emptyList()
    }

    override suspend fun getGameStatistics(): GameStatistics {
        return try {
            val totalGames = gamePreferences.getTotalGamesPlayed().first()
            val highScore = gamePreferences.getHighScore().first()

            GameStatistics(
                totalGamesPlayed = totalGames,
                totalScore = 0, // Would sum all scores from database
                averageScore = 0f, // Would calculate from database
                highestScore = highScore,
                totalBubblesPressed = 0, // Would sum from database
                averageAccuracy = 0f, // Would calculate from database
                bestPerformanceRating = "NEEDS_PRACTICE", // Would determine from database
                totalTimePlayed = kotlin.time.Duration.ZERO, // Would sum from database
                averageGameDuration = kotlin.time.Duration.ZERO // Would calculate from database
            )
        } catch (e: Exception) {
            GameStatistics(0, 0, 0f, 0, 0, 0f, "NEEDS_PRACTICE", kotlin.time.Duration.ZERO, kotlin.time.Duration.ZERO)
        }
    }

    override suspend fun clearAllGameData() {
        try {
            gamePreferences.saveHighScore(0)
            // Reset other game data as needed
        } catch (e: Exception) {
            // Handle error
        }
    }

    override suspend fun getTotalGamesPlayed(): Int {
        return try {
            gamePreferences.getTotalGamesPlayed().first()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getBestPerformanceRating(): String? {
        // In a full implementation, this would query database for best rating
        return null
    }
}