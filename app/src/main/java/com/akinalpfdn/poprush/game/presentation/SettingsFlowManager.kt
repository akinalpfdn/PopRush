package com.akinalpfdn.poprush.game.presentation

import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Combines multiple settings flows into a single reactive stream.
 */
class SettingsFlowManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    fun observeSettings(): Flow<SettingsBundle> = combine(
        listOf(
            settingsRepository.getBubbleShapeFlow(),
            settingsRepository.getSoundEnabledFlow(),
            settingsRepository.getMusicEnabledFlow(),
            settingsRepository.getSoundVolumeFlow(),
            settingsRepository.getMusicVolumeFlow(),
            settingsRepository.getZoomLevelFlow(),
            settingsRepository.getHapticFeedbackFlow(),
            settingsRepository.getGameDifficultyFlow()
        )
    ) { values ->
        SettingsBundle(
            bubbleShape = values[0] as BubbleShape,
            soundEnabled = values[1] as Boolean,
            musicEnabled = values[2] as Boolean,
            soundVolume = values[3] as Float,
            musicVolume = values[4] as Float,
            zoomLevel = values[5] as Float,
            hapticFeedback = values[6] as Boolean,
            difficulty = values[7] as GameDifficulty
        )
    }
}

data class SettingsBundle(
    val bubbleShape: BubbleShape,
    val soundEnabled: Boolean,
    val musicEnabled: Boolean,
    val soundVolume: Float,
    val musicVolume: Float,
    val zoomLevel: Float,
    val hapticFeedback: Boolean,
    val difficulty: GameDifficulty
)
