package com.watchrunning.app.calculation

import com.watchrunning.app.model.GpsSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationSourceSelectorTest {
    @Test fun firstAccurateSourceIsSelected() {
        val selector = LocationSourceSelector()
        val decision = selector.observe(GpsSource.FUSED, 12.0, 1_000)
        assertTrue(decision.useSample)
        assertEquals(GpsSource.FUSED, selector.selectedSource(1_000))
    }

    @Test fun betterSourceRequiresTwoConsecutiveFixes() {
        val selector = LocationSourceSelector()
        selector.observe(GpsSource.WATCH_GNSS, 20.0, 1_000)
        assertFalse(selector.observe(GpsSource.FUSED, 12.0, 2_000).useSample)
        val switched = selector.observe(GpsSource.FUSED, 11.0, 3_000)
        assertTrue(switched.useSample)
        assertTrue(switched.sourceChanged)
        assertEquals(GpsSource.FUSED, selector.selectedSource(3_000))
    }

    @Test fun smallAccuracyDifferenceDoesNotCauseSwitching() {
        val selector = LocationSourceSelector()
        selector.observe(GpsSource.WATCH_GNSS, 10.0, 1_000)
        repeat(3) { index ->
            assertFalse(selector.observe(GpsSource.HEALTH_SERVICES_PHONE, 9.0, 2_000L + index).useSample)
        }
        assertEquals(GpsSource.WATCH_GNSS, selector.selectedSource(3_000))
    }

    @Test fun freshCandidateImmediatelyReplacesStaleSource() {
        val selector = LocationSourceSelector()
        selector.observe(GpsSource.WATCH_GNSS, 5.0, 1_000)
        val switched = selector.observe(GpsSource.HEALTH_SERVICES_PHONE, 20.0, 6_001)
        assertTrue(switched.useSample)
        assertEquals(GpsSource.HEALTH_SERVICES_PHONE, selector.selectedSource(6_001))
    }

    @Test fun rejectedSwitchRestoresPreviousSource() {
        val selector = LocationSourceSelector()
        selector.observe(GpsSource.WATCH_GNSS, 20.0, 1_000)
        selector.observe(GpsSource.FUSED, 10.0, 2_000)
        val switched = selector.observe(GpsSource.FUSED, 9.0, 3_000)
        selector.reject(switched)
        assertEquals(GpsSource.WATCH_GNSS, selector.selectedSource(3_000))
        assertNull(selector.selectedSource(7_000))
    }

    @Test fun rejectedCurrentSourceFixDoesNotRefreshItsFreshness() {
        val selector = LocationSourceSelector()
        selector.observe(GpsSource.WATCH_GNSS, 8.0, 1_000)
        val rejected = selector.observe(GpsSource.WATCH_GNSS, 7.0, 7_000)
        selector.reject(rejected)
        assertNull(selector.selectedSource(7_000))
    }
}
