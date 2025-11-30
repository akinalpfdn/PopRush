package com.akinalpfdn.poprush.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import com.akinalpfdn.poprush.core.domain.model.GameIntent
import com.akinalpfdn.poprush.core.domain.model.GameMode
import com.akinalpfdn.poprush.core.domain.model.GameMod
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.core.domain.model.StartScreenFlow
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository

import com.akinalpfdn.poprush.game.presentation.handler.GameLogicHandler
import com.akinalpfdn.poprush.game.presentation.handler.SpeedModeHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel implementing MVI (Model-View-Intent) architecture for the PopRush game.
 * Manages game state, processes intents, and coordinates between different handlers.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository,
    private val audioRepository: AudioRepository,
    val coopHandler: CoopHandler,
    private val speedModeHandler: SpeedModeHandler,
    private val gameLogicHandler: GameLogicHandler
) : ViewModel() {

    // Private mutable state flow
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Expose discoveredEndpoints for GameScreen
    val discoveredEndpoints = coopHandler.discoveredEndpoints

    // Combined settings flow for reactive updates
    private val settingsFlow: Flow<SettingsBundle> = combine(
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

    init {
        // Initialize handlers
        coopHandler.init(viewModelScope, _gameState)
        speedModeHandler.init(viewModelScope, _gameState)
        gameLogicHandler.init(viewModelScope, _gameState)

        // Initialize audio system
        viewModelScope.launch {
            audioRepository.initialize()
        }

        // Load initial game data
        processIntent(GameIntent.LoadGameData)

        // Observe settings changes and update game state
        settingsFlow
            .onEach { settings ->
                updateGameStateFromSettings(settings)
            }
            .launchIn(viewModelScope)

        Timber.d("GameViewModel initialized")
    }

    /**
     * Processes a game intent and updates the game state accordingly.
     */
    fun processIntent(intent: GameIntent) {
        Timber.d("Processing intent: $intent")

        when (intent) {
            is GameIntent.StartGame -> handleStartGame()
            is GameIntent.BackToMenu -> handleBackToMenu()
            is GameIntent.EndGame -> handleEndGame()
            is GameIntent.TogglePause -> handleTogglePause()
            is GameIntent.RestartGame -> handleRestartGame()
            is GameIntent.PressBubble -> handleBubblePress(intent.bubbleId)
            is GameIntent.SelectShape -> handleSelectShape(intent.shape)
            is GameIntent.UpdateZoom -> handleUpdateZoom(intent.zoomLevel)
            is GameIntent.ZoomIn -> handleZoomIn()
            is GameIntent.ZoomOut -> handleZoomOut()
            is GameIntent.ToggleSettings -> handleToggleSettings()
            is GameIntent.ShowBackConfirmation -> handleShowBackConfirmation()
            is GameIntent.HideBackConfirmation -> handleHideBackConfirmation()
            is GameIntent.UpdateHighScore -> gameLogicHandler.handleUpdateHighScore(intent.newHighScore)
            is GameIntent.ToggleSound -> handleToggleSound()
            is GameIntent.ToggleMusic -> handleToggleMusic()
            is GameIntent.UpdateSoundVolume -> handleUpdateSoundVolume(intent.volume)
            is GameIntent.UpdateMusicVolume -> handleUpdateMusicVolume(intent.volume)
            is GameIntent.ChangeDifficulty -> handleChangeDifficulty(intent.difficulty)
            is GameIntent.UpdateSelectedDuration -> handleUpdateSelectedDuration(intent.duration)
            is GameIntent.UpdateTimer -> { /* Handled by GameLogicHandler observation */ }
            is GameIntent.GenerateNewLevel -> gameLogicHandler.handleGenerateNewLevel()
            is GameIntent.ResetGame -> gameLogicHandler.handleResetGame()
            is GameIntent.LoadGameData -> gameLogicHandler.handleLoadGameData()
            is GameIntent.SaveGameData -> gameLogicHandler.handleSaveGameData()
            
            // Coop Mode Intents - Delegated to CoopHandler
            is GameIntent.StartCoopAdvertising -> coopHandler.handleStartCoopAdvertising(intent.playerName, intent.selectedColor)
            is GameIntent.StartCoopDiscovery -> coopHandler.handleStartCoopDiscovery(intent.playerName, intent.selectedColor)
            is GameIntent.StopCoopConnection -> coopHandler.handleStopCoopConnection()
            is GameIntent.CoopClaimBubble -> coopHandler.handleCoopClaimBubble(intent.bubbleId)
            is GameIntent.CoopSyncBubbles -> coopHandler.handleCoopSyncBubbles(intent.bubbles)
            is GameIntent.CoopSyncScores -> coopHandler.handleCoopSyncScores(intent.localScore, intent.opponentScore)
            is GameIntent.CoopGameFinished -> coopHandler.handleCoopGameFinished(intent.winnerId)
            is GameIntent.ShowCoopConnectionDialog -> coopHandler.handleShowCoopConnectionDialog()
            is GameIntent.HideCoopConnectionDialog -> coopHandler.handleHideCoopConnectionDialog()
            is GameIntent.ShowCoopError -> coopHandler.handleShowCoopError(intent.errorMessage)
            is GameIntent.ClearCoopError -> coopHandler.handleClearCoopError()
            is GameIntent.UpdateCoopPlayerName -> coopHandler.handleUpdateCoopPlayerName(intent.playerName)
            is GameIntent.UpdateCoopPlayerColor -> coopHandler.handleUpdateCoopPlayerColor(intent.playerColor)
            is GameIntent.StartCoopConnection -> coopHandler.handleStartCoopConnection()
            is GameIntent.StartHosting -> coopHandler.handleStartHosting()
            is GameIntent.StopHosting -> coopHandler.handleStopHosting()
            is GameIntent.StartDiscovery -> coopHandler.handleStartDiscovery()
            is GameIntent.StopDiscovery -> coopHandler.handleStopDiscovery()
            is GameIntent.ConnectToEndpoint -> coopHandler.handleConnectToEndpoint(intent.endpointId)
            is GameIntent.DisconnectCoop -> coopHandler.handleDisconnectCoop()
            is GameIntent.StartCoopGame -> coopHandler.handleStartCoopGame()
            is GameIntent.StartCoopMatch -> coopHandler.handleStartCoopMatch()
            is GameIntent.CloseCoopConnection -> coopHandler.handleCloseCoopConnection()

            is GameIntent.AudioIntent -> handleAudioIntent(intent)
            is GameIntent.SelectGameMode -> handleSelectGameMode(intent.mode)
            is GameIntent.SelectGameMod -> handleSelectGameMod(intent.mod)
            
            // UI Navigation Intents
            is GameIntent.NavigateToModPicker -> handleNavigateToModPicker()
            is GameIntent.NavigateToGameSetup -> handleNavigateToGameSetup()
            is GameIntent.NavigateBack -> handleNavigateBack()
            
            // Speed Mode Intents - Delegated to SpeedModeHandler
            is GameIntent.ActivateRandomBubble -> speedModeHandler.handleActivateRandomBubble(intent.bubbleId)
            is GameIntent.UpdateSpeedModeInterval -> speedModeHandler.handleUpdateSpeedModeInterval()
            is GameIntent.StartSpeedModeTimer -> speedModeHandler.handleStartSpeedModeTimer()
            is GameIntent.ResetSpeedModeState -> speedModeHandler.handleResetSpeedModeState()
        }
    }

    // Game Management
    private fun handleStartGame() {
        viewModelScope.launch {
            try {
                // Stop ALL timers
                gameLogicHandler.stopTimer()
                speedModeHandler.stopTimer()
                speedModeHandler.handleResetSpeedModeState()

                // Clear bubbles
                _gameState.update { it.copy(bubbles = emptyList()) }

                when (_gameState.value.selectedMod) {
                    GameMod.CLASSIC -> gameLogicHandler.startClassicGame()
                    GameMod.SPEED -> speedModeHandler.handleStartSpeedModeTimer()
                }

                // Start background music if enabled
                if (audioRepository.isAudioSupported() && _gameState.value.musicEnabled) {
                    audioRepository.playMusic(com.akinalpfdn.poprush.core.domain.model.MusicTrack(id = "gameplay", title = "Gameplay Music"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error starting game")
            }
        }
    }

    private fun handleBackToMenu() {
        viewModelScope.launch {
            try {
                // Stop ALL timers
                gameLogicHandler.stopTimer()
                speedModeHandler.stopTimer()

                // Stop music
                audioRepository.stopMusic()

                // Reset handlers
                speedModeHandler.handleResetSpeedModeState()
                gameLogicHandler.handleResetGame() // Resets state to initial

                // Ensure navigation state is correct (ResetGame sets some defaults, but let's be sure)
                _gameState.update { currentState ->
                    currentState.copy(
                        currentScreen = StartScreenFlow.MODE_SELECTION, // Or whatever default
                        selectedMod = GameMod.CLASSIC,
                        gameMode = GameMode.SINGLE
                    )
                }

                Timber.d("Returned to main menu")
            } catch (e: Exception) {
                Timber.e(e, "Error returning to menu")
            }
        }
    }

    private fun handleEndGame() {
        viewModelScope.launch {
            // Stop ALL timers
            gameLogicHandler.stopTimer()
            speedModeHandler.stopTimer()
            
            audioRepository.stopMusic()

            // Delegate saving result based on mode (or handle both)
            // Since we stopped timers, we can check which mode was active or just call handlers
            // But handlers need to know if they should save result.
            // GameLogicHandler.handleEndGame saves result.
            // SpeedModeHandler.handleSpeedModeGameOver saves result.
            
            if (_gameState.value.selectedMod == GameMod.SPEED) {
                speedModeHandler.handleSpeedModeGameOver()
            } else {
                gameLogicHandler.handleEndGame()
            }
        }
    }

    private fun handleTogglePause() {
        _gameState.update { currentState ->
            val newPausedState = !currentState.isPaused
            viewModelScope.launch {
                if (newPausedState) {
                    gameLogicHandler.pauseTimer()
                    speedModeHandler.pauseTimer()
                    audioRepository.pauseMusic()
                } else {
                    gameLogicHandler.resumeTimer()
                    speedModeHandler.resumeTimer()
                    audioRepository.resumeMusic()
                }
            }
            currentState.copy(isPaused = newPausedState)
        }
    }

    private fun handleRestartGame() {
        viewModelScope.launch {
            gameLogicHandler.handleSaveGameData()
            handleStartGame()
        }
    }

    private fun handleBubblePress(bubbleId: Int) {
        if (!_gameState.value.isPlaying || _gameState.value.isPaused) return

        when (_gameState.value.selectedMod) {
            GameMod.CLASSIC -> gameLogicHandler.handleBubblePress(bubbleId)
            GameMod.SPEED -> speedModeHandler.handleBubblePress(bubbleId)
        }
    }

    private fun handleSelectShape(shape: BubbleShape) {
        viewModelScope.launch { settingsRepository.saveBubbleShape(shape) }
    }

    private fun handleUpdateZoom(zoomLevel: Float) {
        viewModelScope.launch { settingsRepository.setZoomLevel(zoomLevel) }
    }

    private fun handleZoomIn() {
        val currentZoom = _gameState.value.zoomLevel
        val newZoom = (currentZoom + 0.1f).coerceAtMost(GameState.MAX_ZOOM_LEVEL)
        handleUpdateZoom(newZoom)
    }

    private fun handleZoomOut() {
        val currentZoom = _gameState.value.zoomLevel
        val newZoom = (currentZoom - 0.1f).coerceAtLeast(GameState.MIN_ZOOM_LEVEL)
        handleUpdateZoom(newZoom)
    }

    private fun handleToggleSettings() {
        _gameState.update { it.copy(showSettings = !it.showSettings) }
        viewModelScope.launch { audioRepository.playSound(SoundType.BUTTON_PRESS) }
    }

    private fun handleToggleSound() {
        viewModelScope.launch { settingsRepository.toggleSoundEnabled() }
    }

    private fun handleToggleMusic() {
        viewModelScope.launch { settingsRepository.toggleMusicEnabled() }
    }

    private fun handleUpdateSoundVolume(volume: Float) {
        viewModelScope.launch {
            settingsRepository.setSoundVolume(volume)
            audioRepository.setMasterVolume(volume)
        }
    }

    private fun handleUpdateMusicVolume(volume: Float) {
        viewModelScope.launch { settingsRepository.setMusicVolume(volume) }
    }

    private fun handleChangeDifficulty(difficulty: GameDifficulty) {
        viewModelScope.launch { settingsRepository.setGameDifficulty(difficulty) }
    }

    private fun handleUpdateSelectedDuration(duration: kotlin.time.Duration) {
        _gameState.update { it.copy(selectedDuration = duration) }
    }

    private fun handleSelectGameMode(mode: GameMode) {
        if (mode == GameMode.COOP) {
            _gameState.update {
                it.copy(
                    gameMode = mode,
                    currentScreen = StartScreenFlow.COOP_CONNECTION,
                    isCoopMode = true
                )
            }
        } else {
            _gameState.update {
                it.copy(
                    gameMode = mode,
                    currentScreen = StartScreenFlow.MOD_PICKER,
                    isCoopMode = false
                )
            }
        }
    }

    private fun handleSelectGameMod(mod: GameMod) {
        _gameState.update {
            it.copy(
                selectedMod = mod,
                currentScreen = StartScreenFlow.GAME_SETUP
            )
        }
    }

    private fun handleNavigateToModPicker() {
        _gameState.update { it.copy(currentScreen = StartScreenFlow.MOD_PICKER) }
    }

    private fun handleNavigateToGameSetup() {
        _gameState.update { it.copy(currentScreen = StartScreenFlow.GAME_SETUP) }
    }

    private fun handleNavigateBack() {
        val currentScreen = _gameState.value.currentScreen
        val newScreen = when (currentScreen) {
            StartScreenFlow.GAME_SETUP -> StartScreenFlow.MOD_PICKER
            StartScreenFlow.MOD_PICKER -> StartScreenFlow.MODE_SELECTION
            StartScreenFlow.COOP_CONNECTION -> StartScreenFlow.MODE_SELECTION
            StartScreenFlow.MODE_SELECTION -> StartScreenFlow.MODE_SELECTION
        }

        _gameState.update { it.copy(currentScreen = newScreen) }
        viewModelScope.launch { audioRepository.playSound(SoundType.BUTTON_PRESS) }
    }

    private fun handleShowBackConfirmation() {
        viewModelScope.launch {
            gameLogicHandler.pauseTimer()
            speedModeHandler.pauseTimer()
            _gameState.update { it.copy(showBackConfirmation = true, isPaused = true) }
        }
    }

    private fun handleHideBackConfirmation() {
        viewModelScope.launch {
            val currentState = _gameState.value
            if (currentState.isPlaying && !currentState.isGameOver) {
                gameLogicHandler.resumeTimer()
                speedModeHandler.resumeTimer()
            }
            _gameState.update {
                it.copy(
                    showBackConfirmation = false,
                    isPaused = if (it.isPlaying && !it.isGameOver) false else it.isPaused
                )
            }
        }
    }

    private fun handleAudioIntent(intent: GameIntent.AudioIntent) {
        viewModelScope.launch {
            when (intent) {
                is GameIntent.AudioIntent.PlaySound -> audioRepository.playSound(intent.soundType)
                is GameIntent.AudioIntent.PlayMusic -> audioRepository.playMusic(intent.musicTrack)
                is GameIntent.AudioIntent.StopAllAudio -> {
                    audioRepository.stopAllSounds()
                    audioRepository.stopMusic()
                }
                is GameIntent.AudioIntent.PauseAudio -> audioRepository.pauseMusic()
                is GameIntent.AudioIntent.ResumeAudio -> audioRepository.resumeMusic()
            }
        }
    }

    private fun updateGameStateFromSettings(settings: SettingsBundle) {
        _gameState.update { currentState ->
            currentState.copy(
                selectedShape = settings.bubbleShape,
                soundEnabled = settings.soundEnabled,
                musicEnabled = settings.musicEnabled,
                soundVolume = settings.soundVolume,
                musicVolume = settings.musicVolume,
                zoomLevel = settings.zoomLevel
            )
        }
    }

    private data class SettingsBundle(
        val bubbleShape: BubbleShape,
        val soundEnabled: Boolean,
        val musicEnabled: Boolean,
        val soundVolume: Float,
        val musicVolume: Float,
        val zoomLevel: Float,
        val hapticFeedback: Boolean,
        val difficulty: GameDifficulty
    )
}