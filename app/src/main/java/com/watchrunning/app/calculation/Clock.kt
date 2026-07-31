package com.watchrunning.app.calculation

import android.os.SystemClock
import java.time.Duration

fun interface MonotonicClock {
    fun nowMillis(): Long
}

object SystemMonotonicClock : MonotonicClock {
    override fun nowMillis(): Long = SystemClock.elapsedRealtime()
}

class ActiveDurationTracker(private val clock: MonotonicClock) {
    private var checkpointActiveMillis = 0L
    private var checkpointMonotonicMillis = 0L
    private var active = false

    fun update(activeDurationMillis: Long, checkpointMillis: Long, isActive: Boolean) {
        checkpointActiveMillis = activeDurationMillis.coerceAtLeast(0)
        checkpointMonotonicMillis = checkpointMillis
        active = isActive
    }

    fun current(): Duration {
        val added = if (active) (clock.nowMillis() - checkpointMonotonicMillis).coerceAtLeast(0) else 0L
        return Duration.ofMillis(checkpointActiveMillis + added)
    }
}
