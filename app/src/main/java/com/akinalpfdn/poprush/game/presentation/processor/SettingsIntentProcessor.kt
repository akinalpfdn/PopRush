package com.akinalpfdn.poprush.game.presentation.processor

import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.SettingsIntent
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Processes settings-related intents: shape, zoom, sound, music, difficulty, duration.
 */
class SettingsIntentProcessor(
    private val settingsRepository: SettingsRepository,
    private val audioRepository: AudioRepository,
    private val scope: CoroutineScope,
    private val gameStateFlow: MutableStateFlow<GameState>
) {

    fun process(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SelectShape -> scope.launch { settingsRepository.saveBubbleShape(intent.shape) }
            is SettingsIntent.UpdateZoom -> scope.launch { settingsRepository.setZoomLevel(intent.zoomLevel) }
            is SettingsIntent.ZoomIn -> handleZoomIn()
            is SettingsIntent.ZoomOut -> handleZoomOut()
            is SettingsIntent.ToggleSettings -> handleToggleSettings()
            is SettingsIntent.ToggleSound -> scope.launch { settingsRepository.toggleSoundEnabled() }
            is SettingsIntent.ToggleMusic -> scope.launch { settingsRepository.toggleMusicEnabled() }
            is SettingsIntent.UpdateSoundVolume -> handleUpdateSoundVolume(intent.volume)
            is SettingsIntent.UpdateMusicVolume -> scope.launch { settingsRepository.setMusicVolume(intent.volume) }
            is SettingsIntent.ChangeDifficulty -> scope.launch { settingsRepository.setGameDifficulty(intent.difficulty) }
            is SettingsIntent.UpdateSelectedDuration -> gameStateFlow.update { it.copy(selectedDuration = intent.duration) }
        }
    }

    private fun handleZoomIn() {
        val currentZoom = gameStateFlow.value.zoomLevel
        val newZoom = (currentZoom + 0.1f).coerceAtMost(GameState.MAX_ZOOM_LEVEL)
        scope.launch { settingsRepository.setZoomLevel(newZoom) }
    }

    private fun handleZoomOut() {
        val currentZoom = gameStateFlow.value.zoomLevel
        val newZoom = (currentZoom - 0.1f).coerceAtLeast(GameState.MIN_ZOOM_LEVEL)
        scope.launch { settingsRepository.setZoomLevel(newZoom) }
    }

    private fun handleToggleSettings() {
        gameStateFlow.update { it.copy(showSettings = !it.showSettings) }
        scope.launch { audioRepository.playSound(SoundType.BUTTON_PRESS) }
    }

    private fun handleUpdateSoundVolume(volume: Float) {
        scope.launch {
            settingsRepository.setSoundVolume(volume)
            audioRepository.setMasterVolume(volume)
        }
    }
}
