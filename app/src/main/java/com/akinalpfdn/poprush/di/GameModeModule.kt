package com.akinalpfdn.poprush.di

import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import com.akinalpfdn.poprush.game.domain.usecase.GenerateLevelUseCase
import com.akinalpfdn.poprush.game.domain.usecase.HandleBubblePressUseCase
import com.akinalpfdn.poprush.game.domain.usecase.InitializeGameUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeTimerUseCase
import com.akinalpfdn.poprush.game.domain.usecase.SpeedModeUseCase
import com.akinalpfdn.poprush.game.domain.usecase.TimerUseCase
import com.akinalpfdn.poprush.game.presentation.CoopHandler
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeDependencies
import com.akinalpfdn.poprush.game.presentation.strategy.GameModeStrategyFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for game mode strategies.
 * Provides the factory and dependencies needed for strategy-based game modes.
 */
@Module
@InstallIn(SingletonComponent::class)
object GameModeModule {

    /**
     * Provides the GameModeDependencies container with all shared dependencies.
     */
    @Provides
    @Singleton
    fun provideGameModeDependencies(
        gameRepository: GameRepository,
        settingsRepository: SettingsRepository,
        audioRepository: AudioRepository,
        initializeGameUseCase: InitializeGameUseCase,
        generateLevelUseCase: GenerateLevelUseCase,
        handleBubblePressUseCase: HandleBubblePressUseCase,
        timerUseCase: TimerUseCase,
        speedModeUseCase: SpeedModeUseCase,
        speedModeTimerUseCase: SpeedModeTimerUseCase,
        coopUseCase: CoopUseCase,
        coopHandler: CoopHandler
    ): GameModeDependencies {
        return GameModeDependencies(
            gameRepository = gameRepository,
            settingsRepository = settingsRepository,
            audioRepository = audioRepository,
            initializeGameUseCase = initializeGameUseCase,
            generateLevelUseCase = generateLevelUseCase,
            handleBubblePressUseCase = handleBubblePressUseCase,
            timerUseCase = timerUseCase,
            speedModeUseCase = speedModeUseCase,
            speedModeTimerUseCase = speedModeTimerUseCase,
            coopUseCase = coopUseCase,
            coopHandler = coopHandler
        )
    }

    /**
     * Provides the GameModeStrategyFactory for creating game mode strategies.
     */
    @Provides
    @Singleton
    fun provideGameModeStrategyFactory(
        dependencies: GameModeDependencies
    ): GameModeStrategyFactory {
        return GameModeStrategyFactory(dependencies)
    }
}
