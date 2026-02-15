package com.akinalpfdn.poprush.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface AudioSettingsRepository {

    suspend fun toggleSoundEnabled()

    suspend fun isSoundEnabled(): Boolean

    fun getSoundEnabledFlow(): Flow<Boolean>

    suspend fun toggleMusicEnabled()

    suspend fun isMusicEnabled(): Boolean

    fun getMusicEnabledFlow(): Flow<Boolean>

    suspend fun setSoundVolume(volume: Float)

    suspend fun getSoundVolume(): Float

    fun getSoundVolumeFlow(): Flow<Float>

    suspend fun setMusicVolume(volume: Float)

    suspend fun getMusicVolume(): Float

    fun getMusicVolumeFlow(): Flow<Float>
}
