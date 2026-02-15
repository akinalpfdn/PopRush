package com.akinalpfdn.poprush.core.domain.util

/**
 * Test implementation of Clock that allows manual time control.
 */
class TestClock(private var currentTime: Long = 0L) : Clock {

    override fun currentTimeMillis(): Long = currentTime

    fun advanceBy(millis: Long) {
        currentTime += millis
    }

    fun setTime(millis: Long) {
        currentTime = millis
    }
}
