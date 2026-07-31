package com.watchrunning.app.calculation

import org.junit.Assert.assertEquals
import org.junit.Test

class ActiveDurationTrackerTest {
    @Test fun activeTimeAdvancesFromMonotonicCheckpoint() {
        var now = 10_000L
        val tracker = ActiveDurationTracker { now }
        tracker.update(activeDurationMillis = 4_000, checkpointMillis = 9_000, isActive = true)

        assertEquals(5_000L, tracker.current().toMillis())
        now = 12_500
        assertEquals(7_500L, tracker.current().toMillis())
    }

    @Test fun pausedTimeRemainsAtCheckpoint() {
        var now = 20_000L
        val tracker = ActiveDurationTracker { now }
        tracker.update(activeDurationMillis = 12_000, checkpointMillis = 19_000, isActive = false)
        now = 60_000

        assertEquals(12_000L, tracker.current().toMillis())
    }
}
