package com.akinalpfdn.poprush.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akinalpfdn.poprush.core.data.local.entity.MatchResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for match result operations.
 * Supports pair-based stats and match history queries.
 */
@Dao
interface MatchResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchResult(matchResult: MatchResultEntity)

    /**
     * Get all match results for a specific player pair (order-independent).
     * Ordered by most recent first.
     */
    @Query("""
        SELECT * FROM match_results
        WHERE (localPlayerId = :playerA AND opponentPlayerId = :playerB)
           OR (localPlayerId = :playerB AND opponentPlayerId = :playerA)
        ORDER BY timestamp DESC
    """)
    fun getMatchHistoryForPair(playerA: String, playerB: String): Flow<List<MatchResultEntity>>

    /**
     * Count wins for a specific player against a specific opponent in a specific mode.
     * Pair-independent: works regardless of who was host.
     */
    @Query("""
        SELECT COUNT(*) FROM match_results
        WHERE winnerId = :playerId
          AND coopMod = :coopMod
          AND ((localPlayerId = :playerId AND opponentPlayerId = :opponentId)
            OR (localPlayerId = :opponentId AND opponentPlayerId = :playerId))
    """)
    suspend fun getWinCount(playerId: String, opponentId: String, coopMod: String): Int

    /**
     * Count total matches for a player pair in a specific mode.
     */
    @Query("""
        SELECT COUNT(*) FROM match_results
        WHERE coopMod = :coopMod
          AND ((localPlayerId = :playerA AND opponentPlayerId = :playerB)
            OR (localPlayerId = :playerB AND opponentPlayerId = :playerA))
    """)
    suspend fun getTotalMatchCount(playerA: String, playerB: String, coopMod: String): Int

    /**
     * Get all match results for the current player pair, ordered by most recent.
     */
    @Query("""
        SELECT * FROM match_results
        WHERE (localPlayerId = :playerA AND opponentPlayerId = :playerB)
           OR (localPlayerId = :playerB AND opponentPlayerId = :playerA)
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getRecentMatches(playerA: String, playerB: String, limit: Int = 50): List<MatchResultEntity>
}
