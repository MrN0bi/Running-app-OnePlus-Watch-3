package com.watchrunning.app.calculation

import com.watchrunning.app.model.HeartRateSample
import com.watchrunning.app.model.HeartRateZone
import com.watchrunning.app.model.ZoneConfiguration
import kotlin.math.roundToInt

object HeartRateZoneCalculator {
    fun fromAge(age: Int): ZoneConfiguration {
        require(age in 13..100)
        return fromMaximumHeartRate(220 - age)
    }

    fun fromMaximumHeartRate(maximumHeartRate: Int): ZoneConfiguration {
        require(maximumHeartRate in 100..240)
        val bounds = listOf(0.50, 0.60, 0.70, 0.80, 0.90)
            .map { (maximumHeartRate * it).roundToInt() }
        return ZoneConfiguration(maximumHeartRate, bounds)
    }

    fun zone(beatsPerMinute: Double, configuration: ZoneConfiguration): HeartRateZone {
        if (!beatsPerMinute.isFinite() || beatsPerMinute < configuration.lowerBoundsBpm[0]) {
            return HeartRateZone.BELOW
        }
        return when {
            beatsPerMinute < configuration.lowerBoundsBpm[1] -> HeartRateZone.ZONE_1
            beatsPerMinute < configuration.lowerBoundsBpm[2] -> HeartRateZone.ZONE_2
            beatsPerMinute < configuration.lowerBoundsBpm[3] -> HeartRateZone.ZONE_3
            beatsPerMinute < configuration.lowerBoundsBpm[4] -> HeartRateZone.ZONE_4
            else -> HeartRateZone.ZONE_5
        }
    }

    /** Fraction along the coloured arc, excluding visual segment gaps. */
    fun indicatorFraction(beatsPerMinute: Double, configuration: ZoneConfiguration): Float {
        val ratio = beatsPerMinute / configuration.maximumHeartRate.toDouble()
        return ((ratio - 0.50) / 0.50).coerceIn(0.0, 1.0).toFloat()
    }
}

class ZoneTimeAccumulator(
    private val configuration: ZoneConfiguration,
    private val maximumAttributedGapMillis: Long = 10_000,
    initialZoneMillis: List<Long> = List(5) { 0L },
    initialUnclassifiedMillis: Long = 0,
) {
    private val zoneTotals = LongArray(5) { index -> initialZoneMillis.getOrElse(index) { 0L }.coerceAtLeast(0) }
    private var unclassified = initialUnclassifiedMillis.coerceAtLeast(0)
    private var previous: HeartRateSample? = null

    fun add(sample: HeartRateSample, active: Boolean) {
        val old = previous
        previous = sample.takeIf { active }
        if (!active || old == null) return

        val interval = sample.monotonicMillis - old.monotonicMillis
        if (interval <= 0) return
        if (!old.accurate || interval > maximumAttributedGapMillis) {
            unclassified += interval.coerceAtMost(maximumAttributedGapMillis)
            return
        }
        val zone = HeartRateZoneCalculator.zone(old.beatsPerMinute, configuration)
        if (zone == HeartRateZone.BELOW) {
            unclassified += interval
        } else {
            zoneTotals[zone.number - 1] += interval
        }
    }

    fun breakContinuity() {
        previous = null
    }

    fun zoneMillis(): List<Long> = zoneTotals.toList()

    fun unclassifiedMillis(): Long = unclassified
}
