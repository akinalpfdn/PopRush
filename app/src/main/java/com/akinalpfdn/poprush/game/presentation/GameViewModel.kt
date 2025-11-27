package com.akinalpfdn.poprush.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
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
import com.akinalpfdn.poprush.game.domain.usecase.GenerateLevelUseCase
import com.akinalpfdn.poprush.game.domain.usecase.HandleBubblePressUseCase
import com.akinalpfdn.poprush.game.domain.usecase.InitializeGameUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerEvent
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerUseCase
import com.akinalpfdn.poprush.game.domain.usecase.TimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlin.time.Duration
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel implementing MVI (Model-View-Intent) architecture for the PopRush game.
 * Manages game state, processes intents, and coordinates between different use cases.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository,
    private val audioRepository: AudioRepository,
    private val initializeGameUseCase: InitializeGameUseCase,
    private val generateLevelUseCase: GenerateLevelUseCase,
    private val handleBubblePressUseCase: HandleBubblePressUseCase,
    private val timerUseCase: TimerUseCase,
    private val speedModeUseCase: SpeedModeUseCase,
    private val speedModeTimerUseCase: SpeedModeTimerUseCase
) : ViewModel() {

    // Private mutable state flow
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

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
        processIntent(GameIntent.LoadGameData)

        // Observe settings changes and update game state
        settingsFlow
            .onEach { settings ->
                updateGameStateFromSettings(settings)
            }
            .launchIn(viewModelScope)

        // Observe high score changes
        gameRepository.getHighScoreFlow()
            .onEach { highScore ->
                _gameState.update { currentState ->
                    currentState.copy(highScore = highScore)
                }
            }
            .launchIn(viewModelScope)

        // Observe timer updates
        timerUseCase.getTimerFlow()
            .onEach { timeRemaining ->
                _gameState.update { currentState ->
                    currentState.copy(timeRemaining = timeRemaining)
                }
                // Check for game over
                if (timeRemaining.inWholeSeconds <= 0 && _gameState.value.isPlaying) {
                    processIntent(GameIntent.EndGame)
                }
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
            is GameIntent.UpdateHighScore -> handleUpdateHighScore(intent.newHighScore)
            is GameIntent.ToggleSound -> handleToggleSound()
            is GameIntent.ToggleMusic -> handleToggleMusic()
            is GameIntent.UpdateSoundVolume -> handleUpdateSoundVolume(intent.volume)
            is GameIntent.UpdateMusicVolume -> handleUpdateMusicVolume(intent.volume)
            is GameIntent.ChangeDifficulty -> handleChangeDifficulty(intent.difficulty)
            is GameIntent.UpdateSelectedDuration -> handleUpdateSelectedDuration(intent.duration)
            is GameIntent.UpdateTimer -> handleUpdateTimer(intent.timeRemaining)
            is GameIntent.GenerateNewLevel -> handleGenerateNewLevel()
            is GameIntent.ResetGame -> handleResetGame()
            is GameIntent.LoadGameData -> handleLoadGameData()
            is GameIntent.SaveGameData -> handleSaveGameData()
            is GameIntent.AudioIntent -> handleAudioIntent(intent)
            // Game Mode Selection Intents
            is GameIntent.SelectGameMode -> handleSelectGameMode(intent.mode)
            is GameIntent.SelectGameMod -> handleSelectGameMod(intent.mod)
            // UI Navigation Intents
            is GameIntent.NavigateToModPicker -> handleNavigateToModPicker()
            is GameIntent.NavigateToGameSetup -> handleNavigateToGameSetup()
            is GameIntent.ShowCoopComingSoon -> handleShowCoopComingSoon()
            is GameIntent.HideComingSoonMessage -> handleHideComingSoonMessage()
            is GameIntent.NavigateBack -> handleNavigateBack()
            // Speed Mode Intents
            is GameIntent.ActivateRandomBubble -> handleActivateRandomBubble(intent.bubbleId)
            is GameIntent.UpdateSpeedModeInterval -> handleUpdateSpeedModeInterval()
            is GameIntent.StartSpeedModeTimer -> handleStartSpeedModeTimer()
            is GameIntent.ResetSpeedModeState -> handleResetSpeedModeState()
        }
    }

    // Game Management Intents
    private fun handleStartGame() {
        viewModelScope.launch {
            try {
                val currentState = _gameState.value

                when (currentState.selectedMod) {
                    GameMod.CLASSIC -> {
                        // Classic mode: existing logic
                        val newBubbles = initializeGameUseCase.execute()

                        // Generate the first level immediately
                        val difficulty = getCurrentSettings().difficulty
                        val activeBubbles = generateLevelUseCase.execute(
                            currentBubbles = newBubbles,
                            difficulty = difficulty,
                            currentLevel = 1
                        )

                        val selectedDuration = currentState.selectedDuration

                        _gameState.update {
                            it.copy(
                                isPlaying = true,
                                isGameOver = false,
                                isPaused = false,
                                score = 0,
                                timeRemaining = selectedDuration,
                                currentLevel = 1,
                                bubbles = activeBubbles
                            )
                        }

                        // Start the timer with selected duration
                        timerUseCase.startTimer(selectedDuration)

                        Timber.d("Classic mode game started with ${activeBubbles.count { it.isActive }} active bubbles")
                    }

                    GameMod.SPEED -> {
                        // Speed mode: new logic
                        _gameState.update {
                            it.copy(
                                isPlaying = true,
                                isGameOver = false,
                                isPaused = false,
                                score = 0,
                                timeRemaining = Duration.ZERO, // No time limit in speed mode
                                currentLevel = 1
                            )
                        }

                        // Start speed mode timer
                        processIntent(GameIntent.StartSpeedModeTimer)

                        Timber.d("Speed mode game started")
                    }
                }

                // Start background music if enabled
                if (audioRepository.isAudioSupported() && _gameState.value.musicEnabled) {
                    audioRepository.playMusic(com.akinalpfdn.poprush.core.domain.model.MusicTrack(id = "gameplay", title = "Gameplay Music"))
                }

                // Play start sound
                audioRepository.playSound(SoundType.BUTTON_PRESS)
            } catch (e: Exception) {
                Timber.e(e, "Error starting game")
            }
        }
    }

    /**
     * Handles back to menu navigation.
     * Resets game state to return to the start screen.
     */
    private fun handleBackToMenu() {
        viewModelScope.launch {
            try {
                // Stop any running timer
                timerUseCase.stopTimer()

                // Stop background music
                audioRepository.stopMusic()

                // Reset game state to initial state (similar to ResetGame)
                _gameState.update { currentState ->
                    currentState.copy(
                        isPlaying = false,
                        isGameOver = false,
                        isPaused = false,
                        score = 0,
                        currentLevel = 1,
                        timeRemaining = GameState.GAME_DURATION,
                        showSettings = false,
                        showBackConfirmation = false
                    )
                }

                Timber.d("Returned to main menu")
            } catch (e: Exception) {
                Timber.e(e, "Error returning to menu")
            }
        }
    }

    /**
     * Shows the back confirmation dialog and pauses the game.
     */
    private fun handleShowBackConfirmation() {
        viewModelScope.launch {
            // Pause the timer when showing confirmation
            timerUseCase.pauseTimer()

            _gameState.update { it.copy(
                showBackConfirmation = true,
                isPaused = true
            ) }
        }
    }

    /**
     * Hides the back confirmation dialog and resumes the game if it was playing.
     */
    private fun handleHideBackConfirmation() {
        viewModelScope.launch {
            val currentState = _gameState.value

            // Only resume if the game was playing (not game over)
            if (currentState.isPlaying && !currentState.isGameOver) {
                timerUseCase.resumeTimer()
            }

            _gameState.update {
                it.copy(
                    showBackConfirmation = false,
                    // Only unpause if we're still playing and not going back to menu
                    isPaused = if (it.isPlaying && !it.isGameOver) false else it.isPaused
                )
            }
        }
    }

    private fun handleEndGame() {
        viewModelScope.launch {
            try {
                val currentState = _gameState.value

                // Stop the timers
                timerUseCase.stopTimer()
                speedModeTimerUseCase.stopTimer()

                // Reset speed mode state if needed
                if (currentState.selectedMod == GameMod.SPEED) {
                    speedModeUseCase.resetSpeedMode()
                }

                // Stop music
                audioRepository.stopMusic()

                // Create game result for statistics
                val gameResult = createGameResult(currentState)

                // Save game result
                gameRepository.saveGameResult(gameResult)

                // Update high score if needed
                if (currentState.score > currentState.highScore) {
                    gameRepository.updateHighScore(currentState.score)
                    audioRepository.playSound(SoundType.HIGH_SCORE)
                } else {
                    audioRepository.playSound(SoundType.GAME_OVER)
                }

                _gameState.update { it.copy(
                    isPlaying = false,
                    isGameOver = true,
                    isPaused = false
                )}

                Timber.d("Game ended with score: ${currentState.score}")
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
                    timerUseCase.pauseTimer()
                    if (currentState.selectedMod == GameMod.SPEED) {
                        speedModeTimerUseCase.pauseTimer()
                    }
                    audioRepository.pauseMusic()
                } else {
                    timerUseCase.resumeTimer()
                    if (currentState.selectedMod == GameMod.SPEED) {
                        speedModeTimerUseCase.resumeTimer()
                    }
                    audioRepository.resumeMusic()
                }
            }
            currentState.copy(isPaused = newPausedState)
        }
    }

    private fun handleRestartGame() {
        viewModelScope.launch {
            // Save current game data first
            handleSaveGameData()
            // Start fresh game
            handleStartGame()
        }
    }

    private fun handleResetGame() {
        viewModelScope.launch {
            try {
                // Stop timers
                timerUseCase.stopTimer()
                speedModeTimerUseCase.stopTimer()

                // Reset speed mode state
                speedModeUseCase.resetSpeedMode()

                // Reset to initial state
                _gameState.update { currentState ->
                    currentState.copy(
                        isPlaying = false,
                        isGameOver = false,
                        isPaused = false,
                        score = 0,
                        currentLevel = 1,
                        bubbles = emptyList(),
                        timeRemaining = GameState.GAME_DURATION,
                        speedModeState = speedModeUseCase.speedModeState.value
                    )
                }

                Timber.d("Game reset to initial state")
            } catch (e: Exception) {
                Timber.e(e, "Error resetting game")
            }
        }
    }

    // Bubble Interaction Intents
    private fun handleBubblePress(bubbleId: Int) {
        viewModelScope.launch {
            try {
                val currentState = _gameState.value
                if (!currentState.isPlaying || currentState.isPaused) return@launch

                when (currentState.selectedMod) {
                    GameMod.CLASSIC -> {
                        // Classic mode: existing logic
                        val result = handleBubblePressUseCase.execute(
                            bubbles = currentState.bubbles,
                            bubbleId = bubbleId
                        )

                        if (result.success) {
                            // Update bubbles
                            _gameState.update { it.copy(bubbles = result.updatedBubbles) }

                            // Play sound with haptic feedback
                            val settings = getCurrentSettings()
                            audioRepository.playSoundWithHaptic(
                                SoundType.BUBBLE_PRESS,
                                settings.hapticFeedback
                            )

                            // Check if level is complete
                            if (result.isLevelComplete) {
                                _gameState.update { it.copy(score = it.score + 1) }
                                audioRepository.playSound(SoundType.LEVEL_COMPLETE)

                                // Generate new level after a short delay
                                kotlinx.coroutines.delay(200)
                                handleGenerateNewLevel()
                            }
                        }

                        Timber.d("Classic mode bubble $bubbleId pressed: success=${result.success}")
                    }

                    GameMod.SPEED -> {
                        // Speed mode: deactivate bubble logic
                        val bubble = currentState.bubbles.find { it.id == bubbleId }

                        if (bubble != null && bubble.isSpeedModeActive) {
                            // Deactivate the bubble (make it transparent again)
                            val updatedBubbles = speedModeUseCase.deactivateBubble(bubbleId, currentState.bubbles)
                            val settings = getCurrentSettings()

                            _gameState.update {
                                it.copy(
                                    bubbles = updatedBubbles,
                                    score = currentState.score + 1 // Increment score for speed mode
                                )
                            }

                            // Play sound with haptic feedback
                            audioRepository.playSoundWithHaptic(
                                SoundType.BUBBLE_PRESS,
                                settings.hapticFeedback
                            )

                            Timber.d("Speed mode bubble $bubbleId deactivated")
                        } else {
                            Timber.d("Speed mode bubble $bubbleId not active for deactivation")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling bubble press for bubble $bubbleId")
            }
        }
    }

    private fun handleGenerateNewLevel() {
        viewModelScope.launch {
            try {
                val currentState = _gameState.value
                val difficulty = getCurrentSettings().difficulty

                val newBubbles = generateLevelUseCase.execute(
                    currentBubbles = currentState.bubbles,
                    difficulty = difficulty,
                    currentLevel = currentState.currentLevel
                )

                _gameState.update { it.copy(
                    bubbles = newBubbles,
                    currentLevel = it.currentLevel + 1
                )}

                Timber.d("Generated new level ${currentState.currentLevel + 1}")
            } catch (e: Exception) {
                Timber.e(e, "Error generating new level")
            }
        }
    }

    // Settings Intents
    private fun handleSelectShape(shape: BubbleShape) {
        viewModelScope.launch {
            settingsRepository.saveBubbleShape(shape)
        }
    }

    private fun handleUpdateZoom(zoomLevel: Float) {
        viewModelScope.launch {
            settingsRepository.setZoomLevel(zoomLevel)
        }
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
        viewModelScope.launch {
            audioRepository.playSound(SoundType.BUTTON_PRESS)
        }
    }

    private fun handleToggleSound() {
        viewModelScope.launch {
            settingsRepository.toggleSoundEnabled()
        }
    }

    private fun handleToggleMusic() {
        viewModelScope.launch {
            settingsRepository.toggleMusicEnabled()
        }
    }

    private fun handleUpdateSoundVolume(volume: Float) {
        viewModelScope.launch {
            settingsRepository.setSoundVolume(volume)
            audioRepository.setMasterVolume(volume)
        }
    }

    private fun handleUpdateMusicVolume(volume: Float) {
        viewModelScope.launch {
            settingsRepository.setMusicVolume(volume)
        }
    }

    private fun handleChangeDifficulty(difficulty: GameDifficulty) {
        viewModelScope.launch {
            settingsRepository.setGameDifficulty(difficulty)
        }
    }

    private fun handleUpdateTimer(timeRemaining: kotlin.time.Duration) {
        _gameState.update { it.copy(timeRemaining = timeRemaining) }
    }

    /**
     * Updates the selected game duration.
     */
    private fun handleUpdateSelectedDuration(duration: kotlin.time.Duration) {
        _gameState.update { it.copy(selectedDuration = duration) }
    }

    // Game Mode Selection Handlers
    private fun handleSelectGameMode(mode: GameMode) {
        if (mode == GameMode.COOP) {
            // Show coming soon toast and don't change the mode
            _gameState.update { it.copy(showComingSoonToast = true) }
            // Auto-hide toast after 3 seconds
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _gameState.update { it.copy(showComingSoonToast = false) }
            }
        } else {
            // Navigate to mod picker for single player
            _gameState.update {
                it.copy(
                    gameMode = mode,
                    currentScreen = StartScreenFlow.MOD_PICKER,
                    showComingSoonToast = false
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

    // UI Navigation Handlers
    private fun handleNavigateToModPicker() {
        _gameState.update { it.copy(currentScreen = StartScreenFlow.MOD_PICKER) }
    }

    private fun handleNavigateToGameSetup() {
        _gameState.update { it.copy(currentScreen = StartScreenFlow.GAME_SETUP) }
    }

    private fun handleShowCoopComingSoon() {
        _gameState.update { it.copy(showComingSoonToast = true) }
        // Auto-hide after 3 seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _gameState.update { it.copy(showComingSoonToast = false) }
        }
    }

    private fun handleHideComingSoonMessage() {
        _gameState.update { it.copy(showComingSoonToast = false) }
    }

    private fun handleNavigateBack() {
        val currentScreen = _gameState.value.currentScreen
        val newScreen = when (currentScreen) {
            StartScreenFlow.GAME_SETUP -> StartScreenFlow.MOD_PICKER
            StartScreenFlow.MOD_PICKER -> StartScreenFlow.MODE_SELECTION
            StartScreenFlow.MODE_SELECTION -> StartScreenFlow.MODE_SELECTION // Already at first screen, stay here
        }

        _gameState.update { it.copy(currentScreen = newScreen) }

        // Play back navigation sound
        viewModelScope.launch {
            audioRepository.playSound(SoundType.BUTTON_PRESS)
        }
    }

    // Speed Mode Handlers
    private fun handleActivateRandomBubble(bubbleId: Int) {
        viewModelScope.launch {
            try {
                val currentState = _gameState.value
                val updatedBubbles = speedModeUseCase.activateBubble(bubbleId, currentState.bubbles)

                _gameState.update { it.copy(bubbles = updatedBubbles) }

                // Check if speed mode is game over
                if (speedModeUseCase.isGameOver(updatedBubbles)) {
                    handleSpeedModeGameOver()
                }

                Timber.d("Activated bubble $bubbleId in speed mode")
            } catch (e: Exception) {
                Timber.e(e, "Error activating bubble in speed mode")
            }
        }
    }

    private fun handleUpdateSpeedModeInterval() {
        viewModelScope.launch {
            try {
                // This would typically be called by a timer tick
                // For now, just log the current interval
                val currentInterval = speedModeUseCase.speedModeState.value.currentInterval
                Timber.d("Current speed mode interval: ${currentInterval}s")
            } catch (e: Exception) {
                Timber.e(e, "Error updating speed mode interval")
            }
        }
    }

    private fun handleStartSpeedModeTimer() {
        viewModelScope.launch {
            try {
                // Show loading state
                _gameState.update { it.copy(isLoadingSpeedMode = true) }

                // Small delay to show the loading indicator
                kotlinx.coroutines.delay(500)

                // Initialize speed mode
                speedModeUseCase.initializeSpeedMode()

                // Create initial bubbles for speed mode (all transparent/inactive)
                val initialBubbles = initializeGameUseCase.execute().map { bubble ->
                    bubble.copy(
                        transparency = 0.0f, // All transparent initially
                        isSpeedModeActive = false,
                        isActive = false,
                        isPressed = false
                    )
                }

                _gameState.update {
                    it.copy(
                        bubbles = initialBubbles,
                        isLoadingSpeedMode = false
                    )
                }

                // Start the speed mode timer
                speedModeTimerUseCase.startTimer()

                // Set up timer event listener as a separate job
                launch {
                    speedModeTimerUseCase.timerEvents.collect { event ->
                        when (event) {
                            is SpeedModeTimerEvent.ActivateBubble -> {
                                if (event.bubbleId == -1) {
                                    // Random selection needed
                                    val currentBubbles = _gameState.value.bubbles
                                    viewModelScope.launch {
                                        speedModeTimerUseCase.triggerBubbleActivation(currentBubbles)
                                    }
                                } else {
                                    // Specific bubble activation
                                    processIntent(GameIntent.ActivateRandomBubble(event.bubbleId))
                                }
                            }
                            is SpeedModeTimerEvent.GameOver -> {
                                handleSpeedModeGameOver()
                            }
                            is SpeedModeTimerEvent.Tick -> {
                                // Update speed mode state based on elapsed time
                                val deltaTime = speedModeTimerUseCase.getElapsedTime()
                                speedModeUseCase.updateSpeedMode(deltaTime)

                                // Update game state with new speed mode state
                                _gameState.update { currentState ->
                                    currentState.copy(speedModeState = speedModeUseCase.speedModeState.value)
                                }
                            }
                            else -> { /* Handle other events as needed */ }
                        }
                    }
                }

                Timber.d("Speed mode timer started")
            } catch (e: Exception) {
                Timber.e(e, "Error starting speed mode timer")
            }
        }
    }

    private fun handleResetSpeedModeState() {
        viewModelScope.launch {
            try {
                // Stop speed mode timer
                speedModeTimerUseCase.stopTimer()

                // Reset speed mode use case
                speedModeUseCase.resetSpeedMode()

                // Update game state
                _gameState.update { currentState ->
                    currentState.copy(
                        speedModeState = speedModeUseCase.speedModeState.value,
                        bubbles = currentState.bubbles.map { bubble ->
                            bubble.copy(
                                transparency = 1.0f,
                                isSpeedModeActive = false,
                                isActive = false,
                                isPressed = false
                            )
                        }
                    )
                }

                Timber.d("Speed mode state reset")
            } catch (e: Exception) {
                Timber.e(e, "Error resetting speed mode state")
            }
        }
    }

    private fun handleSpeedModeGameOver() {
        viewModelScope.launch {
            try {
                // Stop speed mode timer
                speedModeTimerUseCase.stopTimer()

                // Mark game as over
                _gameState.update { currentState ->
                    currentState.copy(
                        isGameOver = true,
                        isPlaying = false,
                        speedModeState = speedModeUseCase.speedModeState.value.copy(isGameOver = true)
                    )
                }

                Timber.d("Speed mode game over")
            } catch (e: Exception) {
                Timber.e(e, "Error handling speed mode game over")
            }
        }
    }

    private fun handleUpdateHighScore(newHighScore: Int) {
        viewModelScope.launch {
            gameRepository.updateHighScore(newHighScore)
        }
    }

    // Data Management Intents
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
        // This would be called when the app is backgrounded or destroyed
        // Most data is already saved automatically via repositories
    }

    // Audio Intents
    private fun handleAudioIntent(intent: GameIntent.AudioIntent) {
        viewModelScope.launch {
            when (intent) {
                is GameIntent.AudioIntent.PlaySound -> {
                    audioRepository.playSound(intent.soundType)
                }
                is GameIntent.AudioIntent.PlayMusic -> {
                    audioRepository.playMusic(intent.musicTrack)
                }
                is GameIntent.AudioIntent.StopAllAudio -> {
                    audioRepository.stopAllSounds()
                    audioRepository.stopMusic()
                }
                is GameIntent.AudioIntent.PauseAudio -> {
                    audioRepository.pauseMusic()
                }
                is GameIntent.AudioIntent.ResumeAudio -> {
                    audioRepository.resumeMusic()
                }
            }
        }
    }

    // Helper Methods
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

    private fun getCurrentSettings(): SettingsBundle {
        // This would ideally come from a flow, but for simplicity we get current values
        return SettingsBundle(
            bubbleShape = _gameState.value.selectedShape,
            soundEnabled = _gameState.value.soundEnabled,
            musicEnabled = _gameState.value.musicEnabled,
            soundVolume = _gameState.value.soundVolume,
            musicVolume = _gameState.value.musicVolume,
            zoomLevel = _gameState.value.zoomLevel,
            hapticFeedback = true, // Would get from settings
            difficulty = GameDifficulty.NORMAL // Start with normal difficulty like React version
        )
    }

    private fun createGameResult(gameState: GameState) = com.akinalpfdn.poprush.core.domain.model.GameResult(
        finalScore = gameState.score,
        levelsCompleted = gameState.currentLevel - 1,
        totalBubblesPressed = gameState.pressedBubbleCount,
        accuracyPercentage = if (gameState.pressedBubbleCount > 0) 100f else 0f, // Simplified
        averageTimePerLevel = if (gameState.currentLevel > 1) {
            (GameState.GAME_DURATION - gameState.timeRemaining) / (gameState.currentLevel - 1)
        } else GameState.GAME_DURATION,
        gameDuration = GameState.GAME_DURATION - gameState.timeRemaining,
        isHighScore = gameState.score > gameState.highScore
    )

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            timerUseCase.stopTimer()
            audioRepository.release()
        }
        Timber.d("GameViewModel cleared")
    }
}

/**
 * Data class bundling current settings for easier handling.
 */
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