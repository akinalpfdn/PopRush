package com.akinalpfdn.poprush.game.presentation.strategy

import com.akinalpfdn.poprush.core.domain.model.GameResult
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Base class for game mode strategies providing common initialization and helper methods.
 */
abstract class BaseGameModeStrategy(
    protected val dependencies: GameModeDependencies
) : GameModeStrategy {

    protected lateinit var scope: CoroutineScope
    protected lateinit var stateFlow: MutableStateFlow<GameState>

    protected val isInitialized: Boolean
        get() = ::scope.isInitialized

    override suspend fun initialize(scope: CoroutineScope, stateFlow: MutableStateFlow<GameState>) {
        this.scope = scope
        this.stateFlow = stateFlow
        Timber.d("${modeName} initialized")
    }

    protected fun resetStateToDefaults() {
        stateFlow.update { currentState ->
            currentState.copy(
                isPlaying = false,
                isGameOver = false,
                isPaused = false,
                score = 0,
                currentLevel = 1,
                bubbles = emptyList(),
                timeRemaining = GameState.GAME_DURATION,
                showSpeedBonus = false,
                speedBonusPoints = 0
            )
        }
    }

    protected suspend fun saveAndAnnounceResult(gameResult: GameResult) {
        dependencies.gameRepository.saveGameResult(gameResult)

        val modKey = stateFlow.value.selectedMod.modKey
        if (gameResult.isHighScore) {
            dependencies.gameRepository.updateHighScore(modKey, gameResult.finalScore)
            stateFlow.update { state ->
                state.copy(highScores = state.highScores + (modKey to gameResult.finalScore))
            }
            dependencies.audioRepository.playSound(SoundType.HIGH_SCORE)
        } else {
            dependencies.audioRepository.playSound(SoundType.GAME_OVER)
        }
    }

    protected fun markGameOver() {
        stateFlow.update {
            it.copy(
                isPlaying = false,
                isGameOver = true,
                isPaused = false
            )
        }
    }
}
