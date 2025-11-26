package com.akinalpfdn.poprush.di

import com.akinalpfdn.poprush.game.domain.usecase.GenerateLevelUseCase
import com.akinalpfdn.poprush.game.domain.usecase.HandleBubblePressUseCase
import com.akinalpfdn.poprush.game.domain.usecase.InitializeGameUseCase
import com.akinalpfdn.poprush.game.domain.usecase.TimerUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for game use cases.
 * Provides business logic implementations for game operations.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    /**
     * Provides use case for initializing the game.
     */
    @Provides
    @Singleton
    fun provideInitializeGameUseCase(): InitializeGameUseCase {
        return InitializeGameUseCase()
    }

    /**
     * Provides use case for generating new levels.
     */
    @Provides
    @Singleton
    fun provideGenerateLevelUseCase(): GenerateLevelUseCase {
        return GenerateLevelUseCase()
    }

    /**
     * Provides use case for handling bubble press events.
     */
    @Provides
    @Singleton
    fun provideHandleBubblePressUseCase(): HandleBubblePressUseCase {
        return HandleBubblePressUseCase()
    }

    /**
     * Provides use case for managing game timer.
     */
    @Provides
    @Singleton
    fun provideTimerUseCase(): TimerUseCase {
        return TimerUseCase()
    }
}