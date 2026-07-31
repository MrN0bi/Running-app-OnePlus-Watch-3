package com.watchrunning.app.calculation

import com.watchrunning.app.model.LocationSample
import com.watchrunning.app.model.PaceUnavailableReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaceEstimatorTest {
    @Test fun constantSpeedTrackProducesExpectedPace() {
        val estimator = PaceEstimator(5)
        val metresPerSecond = 1000.0 / 300.0
        for (second in 0..6) estimator.add(point(second, metresPerSecond * second))
        val estimate = estimator.estimate(6_000)
        assertNotNull(estimate.secondsPerKilometre)
        assertEquals(300.0, estimate.secondsPerKilometre!!, 8.0)
    }

    @Test fun stationaryJitterIsUnavailable() {
        val estimator = PaceEstimator(5)
        for (second in 0..6) estimator.add(point(second, if (second % 2 == 0) 0.4 else -0.4))
        assertEquals(PaceUnavailableReason.STATIONARY, estimator.estimate(6_000).unavailableReason)
    }

    @Test fun impossibleJumpIsRejected() {
        val estimator = PaceEstimator(5)
        assertTrue(estimator.add(point(0, 0.0)).accepted)
        assertFalse(estimator.add(point(1, 100.0)).accepted)
    }

    @Test fun duplicateAndOutOfOrderPointsAreRejected() {
        val estimator = PaceEstimator(5)
        estimator.add(point(2, 0.0))
        assertFalse(estimator.add(point(2, 1.0)).accepted)
        assertFalse(estimator.add(point(1, 1.0)).accepted)
    }

    @Test fun gpsLossBecomesStaleThenUnavailable() {
        val estimator = PaceEstimator(5)
        for (second in 0..6) estimator.add(point(second, second * 3.0))
        assertTrue(estimator.estimate(12_000).stale)
        assertEquals(PaceUnavailableReason.STALE, estimator.estimate(17_000).unavailableReason)
    }

    @Test fun acceptsTwentyFiveMetreAccuracyAndRejectsAnythingWorse() {
        val estimator = PaceEstimator(5)
        assertTrue(estimator.add(point(0, 0.0, horizontalErrorMetres = 25.0)).accepted)
        assertFalse(estimator.add(point(1, 3.0, horizontalErrorMetres = 25.1)).accepted)
    }

    private fun point(
        second: Int,
        northMetres: Double,
        horizontalErrorMetres: Double = 3.0,
    ) = LocationSample(
        latitude = northMetres / 111_195.0,
        longitude = 0.0,
        altitudeMeters = null,
        bearingDegrees = null,
        horizontalErrorMeters = horizontalErrorMetres,
        monotonicMillis = second * 1_000L,
    )
}
