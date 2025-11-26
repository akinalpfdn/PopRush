package com.akinalpfdn.poprush

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for PopRush.
 * Initializes Hilt dependency injection and sets up logging.
 */
@HiltAndroidApp
class PopRushApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging - will work in debug builds
        // In production builds, you can disable logging or use crash reporting
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            Timber.plant(Timber.DebugTree())
        }

        // Try to log the startup, but don't crash if logging fails
        try {
            Timber.d("PopRush Application started")
        } catch (e: Exception) {
            // Logging failed, continue without it
        }
    }
}