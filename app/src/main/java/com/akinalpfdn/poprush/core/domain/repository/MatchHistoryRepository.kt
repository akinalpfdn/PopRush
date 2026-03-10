package com.akinalpfdn.poprush.core.domain.repository

import com.akinalpfdn.poprush.core.data.local.entity.MatchResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for coop match history and statistics.
 * All queries are pair-based (player A vs player B).
 */
interface MatchHistoryRepository {

    suspend fun saveMatchResult(matchResult: MatchResultEntity)

    fun getMatchHistoryForPair(playerA: String, playerB: String): Flow<List<MatchResultEntity>>

    suspend fun getWinCount(playerId: String, opponentId: String, coopMod: String): Int

    suspend fun getTotalMatchCount(playerA: String, playerB: String, coopMod: String): Int

    suspend fun getRecentMatches(playerA: String, playerB: String, limit: Int = 50): List<MatchResultEntity>
}
