package com.akinalpfdn.poprush.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akinalpfdn.poprush.core.domain.model.AudioIntent
import com.akinalpfdn.poprush.core.domain.model.CoopIntent
import com.akinalpfdn.poprush.core.domain.model.GameIntent
import com.akinalpfdn.poprush.core.domain.model.GameMod
import com.akinalpfdn.poprush.core.domain.model.GameState
import com.akinalpfdn.poprush.core.domain.model.GameplayIntent
import com.akinalpfdn.poprush.core.domain.model.NavigationIntent
import com.akinalpfdn.poprush.core.domain.model.SettingsIntent
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import com.akinalpfdn.poprush.game.presentation.processor.GameplayIntentProcessor
import com.akinalpfdn.poprush.game.presentation.processor.NavigationIntentProcessor
import com.akinalpfdn.poprush.game.presentation.processor.SettingsIntentProcessor
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategy
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategyFactory
import com.akinalpfdn.poprush.game.presentation.strategy.impl.CoopModeStrategy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel implementing MVI architecture for the PopRush game.
 * Delegates intent processing to specialized processors and game logic to strategies.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: com.akinalpfdn.poprush.core.domain.repository.GameRepository,
    private val settingsRepository: SettingsRepository,
    private val audioRepository: AudioRepository,
    private val strategyFactory: GameModeStrategyFactory,
    val coopHandler: CoopHandler,
    private val settingsFlowManager: SettingsFlowManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var activeStrategy: GameModeStrategy? = null
    private var coopStrategy: CoopModeStrategy? = null

    val discoveredEndpoints = coopHandler.discoveredEndpoints

    // Intent processors
    private val gameplayProcessor = GameplayIntentProcessor(
        gameRepository = gameRepository,
        audioRepository = audioRepository,
        coopHandler = coopHandler,
        scope = viewModelScope,
        gameStateFlow = _gameState,
        getActiveStrategy = { activeStrategy }
    )

    private val settingsProcessor = SettingsIntentProcessor(
        settingsRepository = settingsRepository,
        audioRepository = audioRepository,
        scope = viewModelScope,
        gameStateFlow = _gameState
    )

    private val navigationProcessor = NavigationIntentProcessor(
        audioRepository = audioRepository,
        strategyFactory = strategyFactory,
        scope = viewModelScope,
        gameStateFlow = _gameState,
        getActiveStrategy = { activeStrategy },
        setActiveStrategy = { activeStrategy = it },
        getCoopStrategy = { coopStrategy },
        setCoopStrategy = { coopStrategy = it }
    )

    init {
        viewModelScope.launch { audioRepository.initialize() }
        processIntent(GameplayIntent.LoadGameData)
        initializeStrategies()
        coopHandler.init(viewModelScope, _gameState)

        settingsFlowManager.observeSettings()
            .onEach { settings ->
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
            .launchIn(viewModelScope)

        Timber.d("GameViewModel initialized with intent processors")
    }

    private fun initializeStrategies() {
        val classicStrategy = strategyFactory.getStrategy(GameMod.CLASSIC)
        val speedStrategy = strategyFactory.getStrategy(GameMod.SPEED)

        viewModelScope.launch {
            classicStrategy.initialize(viewModelScope, _gameState)
            speedStrategy.initialize(viewModelScope, _gameState)
        }
    }

    fun processIntent(intent: GameIntent) {
        Timber.d("Processing intent: $intent")
        when (intent) {
            is GameplayIntent -> gameplayProcessor.process(intent)
            is SettingsIntent -> settingsProcessor.process(intent)
            is NavigationIntent -> navigationProcessor.process(intent)
            is AudioIntent -> handleAudioIntent(intent)
            is CoopIntent -> handleCoopIntent(intent)
        }
    }

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
            is CoopIntent.PlayAgain -> coopHandler.handlePlayAgain()
            is CoopIntent.SelectCoopMod -> coopHandler.handleSelectCoopMod(intent.coopMod)
            is CoopIntent.ConfirmCoopMod -> coopHandler.handleConfirmCoopMod()
            is CoopIntent.ShowCoopStats -> _gameState.update { it.copy(showCoopStatsDialog = true) }
            is CoopIntent.HideCoopStats -> _gameState.update { it.copy(showCoopStatsDialog = false) }
        }
    }

}
