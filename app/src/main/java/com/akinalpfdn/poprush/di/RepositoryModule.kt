package com.akinalpfdn.poprush.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.akinalpfdn.poprush.audio.data.repository.AudioRepositoryImpl
import com.akinalpfdn.poprush.core.data.local.GamePreferences
import com.akinalpfdn.poprush.core.data.repository.GameRepositoryImpl
import com.akinalpfdn.poprush.core.data.repository.SettingsRepositoryImpl
import com.akinalpfdn.poprush.core.domain.repository.AudioRepository
import com.akinalpfdn.poprush.core.domain.repository.GameRepository
import com.akinalpfdn.poprush.core.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for repository implementations.
 * Provides concrete implementations of domain repository interfaces.
 */
// Create the DataStore extension property
private val Context.preferencesDataStore by preferencesDataStore(name = "poprush_preferences")

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides DataStore instance for preferences storage.
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.preferencesDataStore
    }

    /**
     * Provides GamePreferences for managing DataStore operations.
     */
    @Provides
    @Singleton
    fun provideGamePreferences(dataStore: DataStore<Preferences>): GamePreferences {
        return GamePreferences(dataStore)
    }

    /**
     * Provides GameRepository implementation.
     */
    @Provides
    @Singleton
    fun provideGameRepository(
        gamePreferences: GamePreferences
    ): GameRepository {
        return GameRepositoryImpl(gamePreferences)
    }

    /**
     * Provides SettingsRepository implementation.
     */
    @Provides
    @Singleton
    fun provideSettingsRepository(
        gamePreferences: GamePreferences
    ): SettingsRepository {
        return SettingsRepositoryImpl(gamePreferences)
    }

    /**
     * Provides AudioRepository implementation.
     */
    @Provides
    @Singleton
    fun provideAudioRepository(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository
    ): AudioRepository {
        return AudioRepositoryImpl(context, settingsRepository)
    }
}