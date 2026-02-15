package com.akinalpfdn.poprush.core.domain.util

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abstraction for time source. Allows deterministic testing of time-dependent logic.
 */
interface Clock {
    fun currentTimeMillis(): Long
}

/**
 * Production implementation using the system clock.
 */
@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
