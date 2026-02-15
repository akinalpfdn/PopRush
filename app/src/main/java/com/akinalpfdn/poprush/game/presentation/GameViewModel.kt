package com.akinalpfdn.poprush.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.domain.model.GameDifficulty
import com.akinalpfdn.poprush.core.domain.model.AudioIntent
import com.akinalpfdn.poprush.core.domain.model.CoopIntent
import com.akinalpfdn.poprush.core.domain.model.GameIntent
import com.akinalpfdn.poprush.core.domain.model.GameMode
import com.akinalpfdn.poprush.core.domain.model.GameMod
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.GameplayIntent
import com.akinalpfdn.poprush.core.domain.model.NavigationIntent
import com.akinalpfdn.poprush.core.domain.model.SettingsIntent
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.core.domain.model.StartScreenFlow
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategyFactory
import com.akinalpfdn.poprush.game.presentation.strategy.impl.CoopModeStrategy
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
 * Uses the Strategy pattern to delegate game-specific logic to GameModeStrategy implementations.
 *
 * Adding a new game mode now requires:
 * 1. Create a new GameModeStrategy implementation
 * 2. Add it to GameModeStrategyFactory.createStrategy()
 * 3. Add configuration to GameModeRegistry
 *
 * No changes to this ViewModel are needed for new modes!
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: com.akinalpfdn.poprush.core.domain.repository.GameRepository,
    private val settingsRepository: SettingsRepository,
    private val audioRepository: AudioRepository,
    private val strategyFactory: GameModeStrategyFactory,
    val coopHandler: CoopHandler
) : ViewModel() {

    // Private mutable state flow
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Current active game mode strategy
    private var activeStrategy: GameModeStrategy? = null
    private var coopStrategy: CoopModeStrategy? = null

    // Expose discoveredEndpoints from CoopHandler for GameScreen
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
        // Initialize audio system
        viewModelScope.launch {
            audioRepository.initialize()
        }

        // Load initial game data
        processIntent(GameplayIntent.LoadGameData)

        // Initialize strategies once (like the original handlers)
        initializeStrategies()

        // Initialize coop handler (still used for connection management)
        coopHandler.init(viewModelScope, _gameState)

        // Observe settings changes and update game state
        settingsFlow
            .onEach { settings ->
                updateGameStateFromSettings(settings)
            }
            .launchIn(viewModelScope)

        Timber.d("GameViewModel initialized with strategy pattern")
    }

    /**
     * Initialize all strategies once with the scope and state flow.
     * This matches the original handler behavior where handlers were initialized once.
     */
    private fun initializeStrategies() {
        val classicStrategy = strategyFactory.getStrategy(GameMod.CLASSIC)
        val speedStrategy = strategyFactory.getStrategy(GameMod.SPEED)

        viewModelScope.launch {
            classicStrategy.initialize(viewModelScope, _gameState)
            speedStrategy.initialize(viewModelScope, _gameState)
        }

        Timber.d("All strategies initialized")
    }

    /**
     * Processes a game intent and updates the game state accordingly.
     * Intents are either handled globally or delegated to the active strategy.
     */
    fun processIntent(intent: GameIntent) {
        Timber.d("Processing intent: $intent")

        when (intent) {
            // Gameplay intents
            is GameplayIntent -> handleGameplayIntent(intent)

            // Settings intents
            is SettingsIntent -> handleSettingsIntent(intent)

            // Navigation intents
            is NavigationIntent -> handleNavigationIntent(intent)

            // Audio intents
            is AudioIntent -> handleAudioIntent(intent)

            // Coop intents - delegate to CoopHandler
            is CoopIntent -> handleCoopIntent(intent)
        }
    }

    // ============ Intent Dispatchers ============

    private fun handleGameplayIntent(intent: GameplayIntent) {
        when (intent) {
            is GameplayIntent.StartGame -> handleStartGame()
            is GameplayIntent.EndGame -> handleEndGame()
            is GameplayIntent.TogglePause -> handleTogglePause()
            is GameplayIntent.RestartGame -> handleRestartGame()
            is GameplayIntent.ResetGame -> handleResetGame()
            is GameplayIntent.LoadGameData -> handleLoadGameData()
            is GameplayIntent.SaveGameData -> handleSaveGameData()
            is GameplayIntent.PressBubble -> handleBubblePress(intent.bubbleId)
            is GameplayIntent.UpdateHighScore -> handleUpdateHighScore(intent.newHighScore)
            // Legacy intents - handled by strategies directly
            is GameplayIntent.UpdateTimer -> { /* Handled by strategies */ }
            is GameplayIntent.GenerateNewLevel -> { /* Handled by strategies */ }
            is GameplayIntent.ActivateRandomBubble -> { /* Handled by strategies */ }
            is GameplayIntent.UpdateSpeedModeInterval -> { /* Handled by strategies */ }
            is GameplayIntent.StartSpeedModeTimer -> { /* Handled by strategies */ }
            is GameplayIntent.ResetSpeedModeState -> { /* Handled by strategies */ }
        }
    }

    private fun handleSettingsIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SelectShape -> handleSelectShape(intent.shape)
            is SettingsIntent.UpdateZoom -> handleUpdateZoom(intent.zoomLevel)
            is SettingsIntent.ZoomIn -> handleZoomIn()
            is SettingsIntent.ZoomOut -> handleZoomOut()
            is SettingsIntent.ToggleSettings -> handleToggleSettings()
            is SettingsIntent.ToggleSound -> handleToggleSound()
            is SettingsIntent.ToggleMusic -> handleToggleMusic()
            is SettingsIntent.UpdateSoundVolume -> handleUpdateSoundVolume(intent.volume)
            is SettingsIntent.UpdateMusicVolume -> handleUpdateMusicVolume(intent.volume)
            is SettingsIntent.ChangeDifficulty -> handleChangeDifficulty(intent.difficulty)
            is SettingsIntent.UpdateSelectedDuration -> handleUpdateSelectedDuration(intent.duration)
        }
    }

    private fun handleNavigationIntent(intent: NavigationIntent) {
        when (intent) {
            is NavigationIntent.NavigateToModPicker -> handleNavigateToModPicker()
            is NavigationIntent.NavigateToGameSetup -> handleNavigateToGameSetup()
            is NavigationIntent.NavigateBack -> handleNavigateBack()
            is NavigationIntent.BackToMenu -> handleBackToMenu()
            is NavigationIntent.ShowBackConfirmation -> handleShowBackConfirmation()
            is NavigationIntent.HideBackConfirmation -> handleHideBackConfirmation()
            is NavigationIntent.SelectGameMode -> handleSelectGameMode(intent.mode)
            is NavigationIntent.SelectGameMod -> handleSelectGameMod(intent.mod)
        }
    }

    private fun handleCoopIntent(intent: CoopIntent) {
        when (intent) {
            is CoopIntent.StartCoopAdvertising -> coopHandler.handleStartCoopAdvertising(intent.playerName, intent.selectedColor)
            is CoopIntent.StartCoopDiscovery -> coopHandler.handleStartCoopDiscovery(intent.playerName, intent.selectedColor)
            is CoopIntent.StopCoopConnection -> coopHandler.handleStopCoopConnection()
            is CoopIntent.CoopClaimBubble -> coopHandler.handleCoopClaimBubble(intent.bubbleId)
            is CoopIntent.CoopSyncBubbles -> coopHandler.handleCoopSyncBubbles(intent.bubbles)
            is CoopIntent.CoopSyncScores -> coopHandler.handleCoopSyncScores(intent.localScore, intent.opponentScore)
            is CoopIntent.CoopGameFinished -> coopHandler.handleCoopGameFinished(intent.winnerId)
            is CoopIntent.ShowCoopConnectionDialog -> coopHandler.handleShowCoopConnectionDialog()
            is CoopIntent.HideCoopConnectionDialog -> coopHandler.handleHideCoopConnectionDialog()
            is CoopIntent.ShowCoopError -> coopHandler.handleShowCoopError(intent.errorMessage)
            is CoopIntent.ClearCoopError -> coopHandler.handleClearCoopError()
            is CoopIntent.UpdateCoopPlayerName -> coopHandler.handleUpdateCoopPlayerName(intent.playerName)
            is CoopIntent.UpdateCoopPlayerColor -> coopHandler.handleUpdateCoopPlayerColor(intent.playerColor)
            is CoopIntent.StartCoopConnection -> coopHandler.handleStartCoopConnection()
            is CoopIntent.StartHosting -> coopHandler.handleStartHosting()
            is CoopIntent.StopHosting -> coopHandler.handleStopHosting()
            is CoopIntent.StartDiscovery -> coopHandler.handleStartDiscovery()
            is CoopIntent.StopDiscovery -> coopHandler.handleStopDiscovery()
            is CoopIntent.ConnectToEndpoint -> coopHandler.handleConnectToEndpoint(intent.endpointId)
            is CoopIntent.DisconnectCoop -> coopHandler.handleDisconnectCoop()
            is CoopIntent.StartCoopGame -> coopHandler.handleStartCoopGame()
            is CoopIntent.StartCoopMatch -> coopHandler.handleStartCoopMatch()
            is CoopIntent.CloseCoopConnection -> coopHandler.handleCloseCoopConnection()
        }
    }

    // ============ Game Management ============

    private fun handleStartGame() {
        viewModelScope.launch {
            try {
                // Start background music if enabled
                if (audioRepository.isAudioSupported() && _gameState.value.musicEnabled) {
                    audioRepository.playMusic(com.akinalpfdn.poprush.core.domain.model.MusicTrack(id = "gameplay", title = "Gameplay Music"))
                }

                // Delegate to active strategy
                activeStrategy?.startGame()

                Timber.d("Game started with strategy: ${activeStrategy?.modeId}")
            } catch (e: Exception) {
                Timber.e(e, "Error starting game")
            }
        }
    }

    private fun handleBackToMenu() {
        viewModelScope.launch {
            try {
                // Stop music
                audioRepository.stopMusic()

                // Clean up active strategy
                activeStrategy?.cleanup()

                // Reset to initial state
                _gameState.update { currentState ->
                    currentState.copy(
                        currentScreen = StartScreenFlow.MODE_SELECTION,
                        selectedMod = GameMod.CLASSIC,
                        gameMode = GameMode.SINGLE,
                        isPlaying = false,
                        isGameOver = false,
                        isPaused = false,
                        score = 0,
                        currentLevel = 1,
                        bubbles = emptyList(),
                        timeRemaining = GameState.GAME_DURATION,
                        isCoopMode = false,
                        coopState = null
                    )
                }

                // Clear active strategy
                activeStrategy = null

                Timber.d("Returned to main menu")
            } catch (e: Exception) {
                Timber.e(e, "Error returning to menu")
            }
        }
    }

    private fun handleEndGame() {
        viewModelScope.launch {
            try {
                // Stop music
                audioRepository.stopMusic()

                // Delegate to active strategy
                activeStrategy?.endGame()
            } catch (e: Exception) {
                Timber.e(e, "Error ending game")
            }
        }
    }

    private fun handleTogglePause() {
        _gameState.update { currentState ->
            val newPausedState = !currentState.isPaused
            viewModelScope.launch {
                if (newPausedState) {
                    activeStrategy?.pauseGame()
                    audioRepository.pauseMusic()
                } else {
                    activeStrategy?.resumeGame()
                    audioRepository.resumeMusic()
                }
            }
            currentState.copy(isPaused = newPausedState)
        }
    }

    private fun handleRestartGame() {
        viewModelScope.launch {
            handleSaveGameData()
            handleStartGame()
        }
    }

    private fun handleResetGame() {
        viewModelScope.launch {
            activeStrategy?.resetGame()
        }
    }

    private fun handleLoadGameData() {
        viewModelScope.launch {
            try {
                val highScore = gameRepository.getHighScore()
                _gameState.update { it.copy(highScore = highScore) }
                Timber.d("Game data loaded. High score: $highScore")
            } catch (e: Exception) {
                Timber.e(e, "Error loading game data")
            }
        }
    }

    private fun handleSaveGameData() {
        // Most data is already saved automatically via repositories
    }

    private fun handleBubblePress(bubbleId: Int) {
        if (!_gameState.value.isPlaying || _gameState.value.isPaused) return

        viewModelScope.launch {
            try {
                // Coop mode uses CoopHandler directly
                if (_gameState.value.isCoopMode) {
                    coopHandler.handleCoopClaimBubble(bubbleId)
                } else {
                    // Delegate to active strategy
                    activeStrategy?.handleBubblePress(bubbleId)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling bubble press for bubble $bubbleId")
            }
        }
    }

    // ============ Settings ============

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

    private fun handleShowBackConfirmation() {
        viewModelScope.launch {
            activeStrategy?.pauseGame()
            _gameState.update { it.copy(showBackConfirmation = true, isPaused = true) }
        }
    }

    private fun handleHideBackConfirmation() {
        viewModelScope.launch {
            val currentState = _gameState.value
            if (currentState.isPlaying && !currentState.isGameOver) {
                activeStrategy?.resumeGame()
            }
            _gameState.update {
                it.copy(
                    showBackConfirmation = false,
                    isPaused = if (it.isPlaying && !it.isGameOver) false else it.isPaused
                )
            }
        }
    }

    private fun handleUpdateHighScore(newHighScore: Int) {
        viewModelScope.launch { gameRepository.updateHighScore(newHighScore) }
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

    // ============ Mode Selection (Strategy Switching) ============

    private fun handleSelectGameMode(mode: GameMode) {
        viewModelScope.launch {
            if (mode == GameMode.COOP) {
                // For coop mode, use the coop strategy
                if (coopStrategy == null) {
                    coopStrategy = CoopModeStrategy(strategyFactory.dependencies)
                    coopStrategy?.initialize(viewModelScope, _gameState)
                }
                activeStrategy = coopStrategy

                _gameState.update {
                    it.copy(
                        gameMode = mode,
                        currentScreen = StartScreenFlow.COOP_CONNECTION,
                        isCoopMode = true
                    )
                }
            } else {
                // Single player mode - strategy will be set when mod is selected
                _gameState.update {
                    it.copy(
                        gameMode = mode,
                        currentScreen = StartScreenFlow.MOD_PICKER,
                        isCoopMode = false
                    )
                }
            }
        }
    }

    private fun handleSelectGameMod(mod: GameMod) {
        viewModelScope.launch {
            // Just switch to the already-initialized strategy
            val strategy = strategyFactory.getStrategy(mod)
            activeStrategy = strategy

            _gameState.update {
                it.copy(
                    selectedMod = mod,
                    currentScreen = StartScreenFlow.GAME_SETUP
                )
            }

            Timber.d("Switched to strategy: ${strategy.modeId}")
        }
    }

    // Removed switchStrategy - no longer needed since strategies are pre-initialized

    // ============ Navigation ============

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

    // ============ Audio ============

    private fun handleAudioIntent(intent: AudioIntent) {
        viewModelScope.launch {
            when (intent) {
                is AudioIntent.PlaySound -> audioRepository.playSound(intent.soundType)
                is AudioIntent.PlayMusic -> audioRepository.playMusic(intent.musicTrack)
                is AudioIntent.StopAllAudio -> {
                    audioRepository.stopAllSounds()
                    audioRepository.stopMusic()
                }
                is AudioIntent.PauseAudio -> audioRepository.pauseMusic()
                is AudioIntent.ResumeAudio -> audioRepository.resumeMusic()
            }
        }
    }

    // ============ Helpers ============

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
