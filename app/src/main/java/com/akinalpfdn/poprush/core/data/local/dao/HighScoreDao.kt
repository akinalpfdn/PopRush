package com.akinalpfdn.poprush.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akinalpfdn.poprush.core.data.local.entity.HighScoreEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for high score operations.
 * Provides methods to insert, update, delete, and query high scores.
 */
@Dao
interface HighScoreDao {

    /**
     * Inserts a new high score. Replaces existing if conflict occurs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighScore(highScore: HighScoreEntity)

    /**
     * Updates an existing high score.
     */
    @Update
    suspend fun updateHighScore(highScore: HighScoreEntity)

    /**
     * Deletes a specific high score.
     */
    @Delete
    suspend fun deleteHighScore(highScore: HighScoreEntity)

    /**
     * Gets the current high score.
     */
    @Query("SELECT score FROM high_scores ORDER BY score DESC LIMIT 1")
    suspend fun getCurrentHighScore(): Int?

    /**
     * Gets a Flow of the current high score for reactive updates.
     */
    @Query("SELECT score FROM high_scores ORDER BY score DESC LIMIT 1")
    fun getCurrentHighScoreFlow(): Flow<Int?>

    /**
     * Gets all high scores ordered by score (highest first).
     */
    @Query("SELECT * FROM high_scores ORDER BY score DESC")
    fun getAllHighScores(): Flow<List<HighScoreEntity>>

    /**
     * Gets the top N high scores.
     */
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT :limit")
    suspend fun getTopHighScores(limit: Int): List<HighScoreEntity>

    /**
     * Gets high scores from a specific date range.
     */
    @Query("SELECT * FROM high_scores WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY score DESC")
    suspend fun getHighScoresInRange(startDate: Long, endDate: Long): List<HighScoreEntity>

    /**
     * Gets the total number of high scores.
     */
    @Query("SELECT COUNT(*) FROM high_scores")
    suspend fun getHighScoreCount(): Int

    /**
     * Gets the average score across all games.
     */
    @Query("SELECT AVG(score) FROM high_scores")
    suspend fun getAverageScore(): Float?

    /**
     * Deletes all high scores.
     */
    @Query("DELETE FROM high_scores")
    suspend fun deleteAllHighScores()

    /**
     * Deletes high scores older than a specific timestamp.
     */
    @Query("DELETE FROM high_scores WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldHighScores(cutoffTimestamp: Long)

    /**
     * Gets the most recent high score.
     */
    @Query("SELECT * FROM high_scores ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecentHighScore(): HighScoreEntity?
}