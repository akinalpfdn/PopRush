package com.akinalpfdn.poprush.di

import com.akinalpfdn.poprush.core.domain.util.Clock
import com.akinalpfdn.poprush.game.domain.usecase.GenerateLevelUseCase
import com.akinalpfdn.poprush.game.domain.usecase.HandleBubblePressUseCase
import com.akinalpfdn.poprush.game.domain.usecase.InitializeGameUseCase
import com.akinalpfdn.poprush.game.domain.usecase.TimerUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Dependency Injection module for game use cases.
 * Provides business logic implementations for game operations.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideInitializeGameUseCase(): InitializeGameUseCase {
        return InitializeGameUseCase()
    }

    @Provides
    @Singleton
    fun provideGenerateLevelUseCase(random: Random): GenerateLevelUseCase {
        return GenerateLevelUseCase(random)
    }

    @Provides
    @Singleton
    fun provideHandleBubblePressUseCase(): HandleBubblePressUseCase {
        return HandleBubblePressUseCase()
    }

    @Provides
    @Singleton
    fun provideTimerUseCase(clock: Clock): TimerUseCase {
        return TimerUseCase(clock)
    }
}
