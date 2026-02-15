package com.akinalpfdn.poprush.core.domain.repository

import com.akinalpfdn.poprush.core.domain.model.BubbleColor

interface PlayerProfileRepository {

    suspend fun savePlayerName(playerName: String)

    suspend fun getPlayerName(): String

    suspend fun savePlayerColor(playerColor: BubbleColor)

    suspend fun getPlayerColor(): BubbleColor

    suspend fun isFirstLaunch(): Boolean

    suspend fun markFirstLaunchCompleted()
}
