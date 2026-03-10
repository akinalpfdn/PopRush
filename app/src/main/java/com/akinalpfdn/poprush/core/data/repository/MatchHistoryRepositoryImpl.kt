package com.akinalpfdn.poprush.core.data.repository

import com.akinalpfdn.poprush.core.data.local.dao.MatchResultDao
import com.akinalpfdn.poprush.core.data.local.entity.MatchResultEntity
import com.akinalpfdn.poprush.core.domain.repository.MatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of MatchHistoryRepository backed by Room database.
 */
class MatchHistoryRepositoryImpl @Inject constructor(
    private val matchResultDao: MatchResultDao
) : MatchHistoryRepository {

    override suspend fun saveMatchResult(matchResult: MatchResultEntity) {
        matchResultDao.insertMatchResult(matchResult)
    }

    override fun getMatchHistoryForPair(playerA: String, playerB: String): Flow<List<MatchResultEntity>> {
        return matchResultDao.getMatchHistoryForPair(playerA, playerB)
    }

    override suspend fun getWinCount(playerId: String, opponentId: String, coopMod: String): Int {
        return matchResultDao.getWinCount(playerId, opponentId, coopMod)
    }

    override suspend fun getTotalMatchCount(playerA: String, playerB: String, coopMod: String): Int {
        return matchResultDao.getTotalMatchCount(playerA, playerB, coopMod)
    }

    override suspend fun getRecentMatches(playerA: String, playerB: String, limit: Int): List<MatchResultEntity> {
        return matchResultDao.getRecentMatches(playerA, playerB, limit)
    }
}
