package com.akinalpfdn.poprush.core.domain.repository

import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import kotlinx.coroutines.flow.Flow

interface GameSettingsRepository {

    suspend fun setGameDifficulty(difficulty: GameDifficulty)

    suspend fun getGameDifficulty(): GameDifficulty

    fun getGameDifficultyFlow(): Flow<GameDifficulty>
}
