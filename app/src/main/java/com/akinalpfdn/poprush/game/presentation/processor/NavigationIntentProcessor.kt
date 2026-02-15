package com.akinalpfdn.poprush.game.presentation.processor

import com.akinalpfdn.poprush.core.domain.model.GameMod
import com.akinalpfdn.poprush.core.domain.model.GameMode
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.NavigationIntent
import com.akinalpfdn.poprush.core.domain.model.SoundType
import com.akinalpfdn.poprush.core.domain.model.StartScreenFlow
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategyFactory
import com.akinalpfdn.poprush.game.presentation.strategy.PausableGameMode
import com.akinalpfdn.poprush.game.presentation.strategy.impl.CoopModeStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Processes navigation-related intents: screen transitions, game mode/mod selection, back actions.
 */
class NavigationIntentProcessor(
    private val audioRepository: AudioRepository,
    private val strategyFactory: GameModeStrategyFactory,
    private val scope: CoroutineScope,
    private val gameStateFlow: MutableStateFlow<GameState>,
    private val getActiveStrategy: () -> GameModeStrategy?,
    private val setActiveStrategy: (GameModeStrategy?) -> Unit,
    private val getCoopStrategy: () -> CoopModeStrategy?,
    private val setCoopStrategy: (CoopModeStrategy?) -> Unit
) {

    fun process(intent: NavigationIntent) {
        when (intent) {
            is NavigationIntent.NavigateToModPicker -> gameStateFlow.update { it.copy(currentScreen = StartScreenFlow.MOD_PICKER) }
            is NavigationIntent.NavigateToGameSetup -> gameStateFlow.update { it.copy(currentScreen = StartScreenFlow.GAME_SETUP) }
            is NavigationIntent.NavigateBack -> handleNavigateBack()
            is NavigationIntent.BackToMenu -> handleBackToMenu()
            is NavigationIntent.ShowBackConfirmation -> handleShowBackConfirmation()
            is NavigationIntent.HideBackConfirmation -> handleHideBackConfirmation()
            is NavigationIntent.SelectGameMode -> handleSelectGameMode(intent.mode)
            is NavigationIntent.SelectGameMod -> handleSelectGameMod(intent.mod)
        }
    }

    private fun handleNavigateBack() {
        val currentScreen = gameStateFlow.value.currentScreen
        val newScreen = when (currentScreen) {
            StartScreenFlow.GAME_SETUP -> StartScreenFlow.MOD_PICKER
            StartScreenFlow.MOD_PICKER -> StartScreenFlow.MODE_SELECTION
            StartScreenFlow.MODE_SELECTION -> StartScreenFlow.MODE_SELECTION
        }
        gameStateFlow.update { it.copy(currentScreen = newScreen) }
        scope.launch { audioRepository.playSound(SoundType.BUTTON_PRESS) }
    }

    private fun handleBackToMenu() {
        scope.launch {
            try {
                audioRepository.stopMusic()
                getActiveStrategy()?.cleanup()

                gameStateFlow.update { currentState ->
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

                setActiveStrategy(null)
                Timber.d("Returned to main menu")
            } catch (e: Exception) {
                Timber.e(e, "Error returning to menu")
            }
        }
    }

    private fun handleShowBackConfirmation() {
        scope.launch {
            (getActiveStrategy() as? PausableGameMode)?.pauseGame()
            gameStateFlow.update { it.copy(showBackConfirmation = true, isPaused = true) }
        }
    }

    private fun handleHideBackConfirmation() {
        scope.launch {
            val currentState = gameStateFlow.value
            if (currentState.isPlaying && !currentState.isGameOver) {
                (getActiveStrategy() as? PausableGameMode)?.resumeGame()
            }
            gameStateFlow.update {
                it.copy(
                    showBackConfirmation = false,
                    isPaused = if (it.isPlaying && !it.isGameOver) false else it.isPaused
                )
            }
        }
    }

    private fun handleSelectGameMode(mode: GameMode) {
        scope.launch {
            if (mode == GameMode.COOP) {
                var coopStrategy = getCoopStrategy()
                if (coopStrategy == null) {
                    coopStrategy = CoopModeStrategy(strategyFactory.dependencies)
                    coopStrategy.initialize(scope, gameStateFlow)
                    setCoopStrategy(coopStrategy)
                }
                setActiveStrategy(coopStrategy)

                gameStateFlow.update {
                    it.copy(
                        gameMode = mode,
                        isCoopMode = true,
                        showCoopConnectionDialog = true
                    )
                }
            } else {
                gameStateFlow.update {
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
        scope.launch {
            val strategy = strategyFactory.getStrategy(mod)
            setActiveStrategy(strategy)

            gameStateFlow.update {
                it.copy(
                    selectedMod = mod,
                    currentScreen = StartScreenFlow.GAME_SETUP
                )
            }

            Timber.d("Switched to strategy: ${strategy.modeId}")
        }
    }
}
