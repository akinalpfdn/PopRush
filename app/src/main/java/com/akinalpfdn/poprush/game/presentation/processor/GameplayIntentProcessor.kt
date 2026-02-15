package com.akinalpfdn.poprush.game.presentation.processor

import com.akinalpfdn.poprush.core.domain.model.GameplayIntent
import com.akinalpfdn.poprush.core.domain.model.MusicTrack
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import com.akinalpfdn.poprush.game.presentation.CoopHandler
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.PausableGameMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Processes gameplay-related intents: start, end, pause, restart, bubble press.
 */
class GameplayIntentProcessor(
    private val gameRepository: GameRepository,
    private val audioRepository: AudioRepository,
    private val coopHandler: CoopHandler,
    private val scope: CoroutineScope,
    private val gameStateFlow: MutableStateFlow<com.akinalpfdn.poprush.core.domain.model.GameState>,
    private val getActiveStrategy: () -> GameModeStrategy?
) {

    fun process(intent: GameplayIntent) {
        when (intent) {
            is GameplayIntent.StartGame -> handleStartGame()
            is GameplayIntent.EndGame -> handleEndGame()
            is GameplayIntent.TogglePause -> handleTogglePause()
            is GameplayIntent.RestartGame -> handleRestartGame()
            is GameplayIntent.ResetGame -> handleResetGame()
            is GameplayIntent.LoadGameData -> handleLoadGameData()
            is GameplayIntent.SaveGameData -> { /* Data saved automatically via repositories */ }
            is GameplayIntent.PressBubble -> handleBubblePress(intent.bubbleId)
            is GameplayIntent.UpdateHighScore -> handleUpdateHighScore(intent.newHighScore)
            is GameplayIntent.UpdateTimer -> { /* Handled by strategies */ }
            is GameplayIntent.GenerateNewLevel -> { /* Handled by strategies */ }
            is GameplayIntent.ActivateRandomBubble -> { /* Handled by strategies */ }
            is GameplayIntent.UpdateSpeedModeInterval -> { /* Handled by strategies */ }
            is GameplayIntent.StartSpeedModeTimer -> { /* Handled by strategies */ }
            is GameplayIntent.ResetSpeedModeState -> { /* Handled by strategies */ }
        }
    }

    private fun handleStartGame() {
        scope.launch {
            try {
                if (audioRepository.isAudioSupported() && gameStateFlow.value.musicEnabled) {
                    audioRepository.playMusic(MusicTrack(id = "gameplay", title = "Gameplay Music"))
                }
                getActiveStrategy()?.startGame()
                Timber.d("Game started with strategy: ${getActiveStrategy()?.modeId}")
            } catch (e: Exception) {
                Timber.e(e, "Error starting game")
            }
        }
    }

    private fun handleEndGame() {
        scope.launch {
            try {
                audioRepository.stopMusic()
                getActiveStrategy()?.endGame()
            } catch (e: Exception) {
                Timber.e(e, "Error ending game")
            }
        }
    }

    private fun handleTogglePause() {
        gameStateFlow.update { currentState ->
            val newPausedState = !currentState.isPaused
            scope.launch {
                val pausable = getActiveStrategy() as? PausableGameMode
                if (newPausedState) {
                    pausable?.pauseGame()
                    audioRepository.pauseMusic()
                } else {
                    pausable?.resumeGame()
                    audioRepository.resumeMusic()
                }
            }
            currentState.copy(isPaused = newPausedState)
        }
    }

    private fun handleRestartGame() {
        scope.launch {
            handleStartGame()
        }
    }

    private fun handleResetGame() {
        scope.launch {
            getActiveStrategy()?.resetGame()
        }
    }

    private fun handleLoadGameData() {
        scope.launch {
            try {
                val highScore = gameRepository.getHighScore()
                gameStateFlow.update { it.copy(highScore = highScore) }
                Timber.d("Game data loaded. High score: $highScore")
            } catch (e: Exception) {
                Timber.e(e, "Error loading game data")
            }
        }
    }

    private fun handleBubblePress(bubbleId: Int) {
        if (!gameStateFlow.value.isPlaying || gameStateFlow.value.isPaused) return

        scope.launch {
            try {
                if (gameStateFlow.value.isCoopMode) {
                    coopHandler.handleCoopClaimBubble(bubbleId)
                } else {
                    getActiveStrategy()?.handleBubblePress(bubbleId)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling bubble press for bubble $bubbleId")
            }
        }
    }

    private fun handleUpdateHighScore(newHighScore: Int) {
        scope.launch { gameRepository.updateHighScore(newHighScore) }
    }
}
