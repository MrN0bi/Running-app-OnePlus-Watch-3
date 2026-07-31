package com.watchrunning.app.calculation

import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class MetricFormattersTest {
    @Test fun paceRoundsAndCarriesMinute() {
        assertEquals("5:00", MetricFormatters.pace(299.6))
    }

    @Test fun paceRejectsInvalidValues() {
        assertEquals("—:—", MetricFormatters.pace(null))
        assertEquals("—:—", MetricFormatters.pace(0.0))
        assertEquals("—:—", MetricFormatters.pace(Double.NaN))
    }

    @Test fun durationFormatsLongRuns() {
        assertEquals("02:03:04", MetricFormatters.duration(Duration.ofSeconds(7_384)))
    }
}
