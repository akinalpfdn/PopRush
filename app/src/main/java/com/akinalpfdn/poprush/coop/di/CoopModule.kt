package com.akinalpfdn.poprush.coop.di

import com.akinalpfdn.poprush.coop.data.NearbyConnectionsManagerImpl
import com.akinalpfdn.poprush.coop.domain.model.NearbyConnectionsManager
import com.akinalpfdn.poprush.coop.domain.usecase.CoopUseCase
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for coop mode dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CoopModule {

    @Binds
    @Singleton
    abstract fun bindNearbyConnectionsManager(
        nearbyConnectionsManagerImpl: NearbyConnectionsManagerImpl
    ): NearbyConnectionsManager

    companion object {
        @Provides
        @Singleton
        fun provideGson(): Gson {
            return Gson()
        }
    }
}