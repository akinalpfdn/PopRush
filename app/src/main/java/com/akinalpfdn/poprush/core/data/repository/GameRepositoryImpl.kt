package com.akinalpfdn.poprush.core.data.repository

import com.akinalpfdn.poprush.core.data.local.GamePreferences
import com.akinalpfdn.poprush.core.domain.model.GameResult
import com.akinalpfdn.poprush.core.domain.model.GameStatistics
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GameRepository using DataStore for persistence.
 * Handles per-mode high scores and game statistics.
 */
@Singleton
class GameRepositoryImpl @Inject constructor(
    private val gamePreferences: GamePreferences
) : GameRepository {

    override suspend fun saveGameResult(result: GameResult) {
        gamePreferences.incrementGamesPlayed()
    }

    override suspend fun getHighScore(modKey: String): Int {
        return try {
            gamePreferences.getHighScore(modKey).first()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun updateHighScore(modKey: String, newScore: Int): Boolean {
        return try {
            val currentHighScore = getHighScore(modKey)
            if (newScore > currentHighScore) {
                gamePreferences.saveHighScore(modKey, newScore)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun getHighScoreFlow(modKey: String): Flow<Int> {
        return gamePreferences.getHighScore(modKey)
    }

    override fun getAllHighScores(): Flow<Map<String, Int>> {
        return combine(
            gamePreferences.getHighScore("classic"),
            gamePreferences.getHighScore("speed")
        ) { classic, speed ->
            mapOf("classic" to classic, "speed" to speed)
        }
    }

    override suspend fun getRecentGameResults(limit: Int): List<GameResult> {
        return emptyList()
    }

    override suspend fun getGameStatistics(): GameStatistics {
        return try {
            val totalGames = gamePreferences.getTotalGamesPlayed().first()
            val classicHighScore = gamePreferences.getHighScore("classic").first()

            GameStatistics(
                totalGamesPlayed = totalGames,
                totalScore = 0,
                averageScore = 0f,
                highestScore = classicHighScore,
                totalBubblesPressed = 0,
                averageAccuracy = 0f,
                bestPerformanceRating = "NEEDS_PRACTICE",
                totalTimePlayed = kotlin.time.Duration.ZERO,
                averageGameDuration = kotlin.time.Duration.ZERO
            )
        } catch (e: Exception) {
            GameStatistics(0, 0, 0f, 0, 0, 0f, "NEEDS_PRACTICE", kotlin.time.Duration.ZERO, kotlin.time.Duration.ZERO)
        }
    }

    override suspend fun clearAllGameData() {
        try {
            gamePreferences.saveHighScore("classic", 0)
            gamePreferences.saveHighScore("speed", 0)
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
        return null
    }
}
