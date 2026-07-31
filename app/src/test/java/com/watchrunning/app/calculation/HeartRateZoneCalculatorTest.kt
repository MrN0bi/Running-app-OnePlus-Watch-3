package com.watchrunning.app.calculation

import com.watchrunning.app.model.HeartRateSample
import com.watchrunning.app.model.HeartRateZone
import org.junit.Assert.assertEquals
import org.junit.Test

class HeartRateZoneCalculatorTest {
    private val config = HeartRateZoneCalculator.fromMaximumHeartRate(200)

    @Test fun boundariesAreInclusiveAtZoneStart() {
        assertEquals(HeartRateZone.BELOW, HeartRateZoneCalculator.zone(99.0, config))
        assertEquals(HeartRateZone.ZONE_1, HeartRateZoneCalculator.zone(100.0, config))
        assertEquals(HeartRateZone.ZONE_2, HeartRateZoneCalculator.zone(120.0, config))
        assertEquals(HeartRateZone.ZONE_3, HeartRateZoneCalculator.zone(140.0, config))
        assertEquals(HeartRateZone.ZONE_4, HeartRateZoneCalculator.zone(160.0, config))
        assertEquals(HeartRateZone.ZONE_5, HeartRateZoneCalculator.zone(180.0, config))
        assertEquals(HeartRateZone.ZONE_5, HeartRateZoneCalculator.zone(220.0, config))
    }

    @Test fun zoneTimeUsesEarlierSampleAndClassifiesLongGaps() {
        val accumulator = ZoneTimeAccumulator(config)
        accumulator.add(HeartRateSample(150.0, 1_000, true), active = true)
        accumulator.add(HeartRateSample(150.0, 6_000, true), active = true)
        accumulator.add(HeartRateSample(150.0, 21_000, true), active = true)
        assertEquals(5_000, accumulator.zoneMillis()[2])
        assertEquals(10_000, accumulator.unclassifiedMillis())
    }

    @Test fun pauseBreaksHeartRateContinuity() {
        val accumulator = ZoneTimeAccumulator(config)
        accumulator.add(HeartRateSample(150.0, 1_000, true), true)
        accumulator.breakContinuity()
        accumulator.add(HeartRateSample(150.0, 8_000, true), true)
        assertEquals(0, accumulator.zoneMillis().sum())
    }

    @Test fun restoredTotalsContinueWithoutBeingReinterpreted() {
        val accumulator = ZoneTimeAccumulator(
            configuration = config,
            initialZoneMillis = listOf(1_000, 2_000, 3_000, 4_000, 5_000),
            initialUnclassifiedMillis = 6_000,
        )
        accumulator.add(HeartRateSample(150.0, 10_000, true), true)
        accumulator.add(HeartRateSample(150.0, 12_000, true), true)

        assertEquals(listOf(1_000L, 2_000L, 5_000L, 4_000L, 5_000L), accumulator.zoneMillis())
        assertEquals(6_000L, accumulator.unclassifiedMillis())
    }
}
