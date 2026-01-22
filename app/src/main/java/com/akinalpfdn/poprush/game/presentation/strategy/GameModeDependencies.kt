package com.akinalpfdn.poprush.game.presentation.strategy

import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.game.domain.usecase.GenerateLevelUseCase
import com.akinalpfdn.poprush.game.domain.usecase.HandleBubblePressUseCase
import com.akinalpfdn.poprush.game.domain.usecase.InitializeGameUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeUseCase
import com.akinalpfdn.poprush.game.domain.usecase.TimerUseCase
import com.akinalpfdn.poprush.game.presentation.CoopHandler

/**
 * Dependencies container for game mode strategies.
 * All shared dependencies are provided here to avoid passing them individually.
 */
data class GameModeDependencies(
    val gameRepository: GameRepository,
    val settingsRepository: SettingsRepository,
    val audioRepository: AudioRepository,
    val initializeGameUseCase: InitializeGameUseCase,
    val generateLevelUseCase: GenerateLevelUseCase,
    val handleBubblePressUseCase: HandleBubblePressUseCase,
    val timerUseCase: TimerUseCase,
    val speedModeUseCase: SpeedModeUseCase,
    val speedModeTimerUseCase: SpeedModeTimerUseCase,
    val coopUseCase: CoopUseCase,
    val coopHandler: CoopHandler
)
