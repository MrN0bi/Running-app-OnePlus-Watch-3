package com.watchrunning.app.calculation

import com.watchrunning.app.model.LocationSample
import com.watchrunning.app.model.PaceEstimate
import com.watchrunning.app.model.PaceUnavailableReason
import java.util.ArrayDeque
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class PaceEstimator(
    windowSeconds: Int = 5,
    private val maximumHorizontalErrorMetres: Double = 25.0,
    private val maximumRunningSpeedMetresPerSecond: Double = 12.0,
) {
    var windowSeconds: Int = windowSeconds
        set(value) {
            require(value in setOf(3, 5, 10))
            field = value
            reset()
        }

    private val samples = ArrayDeque<LocationSample>()
    private val recentSpeeds = ArrayDeque<Pair<Long, Double>>()
    private var lastEstimateAt = Long.MIN_VALUE
    private var lastValidEstimate: PaceEstimate? = null
    private var continuitySegment = 0
    var rejectedSamples: Long = 0
        private set

    init {
        require(windowSeconds in setOf(3, 5, 10))
    }

    data class Acceptance(val accepted: Boolean, val startsNewSegment: Boolean, val segment: Int)

    fun add(sample: LocationSample, active: Boolean = true): Acceptance {
        if (!active || !isCoordinateValid(sample) || sample.horizontalErrorMeters > maximumHorizontalErrorMetres) {
            rejectedSamples++
            return Acceptance(false, false, continuitySegment)
        }

        val previous = samples.lastOrNull()
        if (previous != null) {
            val deltaMillis = sample.monotonicMillis - previous.monotonicMillis
            if (deltaMillis <= 0) {
                rejectedSamples++
                return Acceptance(false, false, continuitySegment)
            }
            if (deltaMillis > 10_000) {
                breakContinuity()
            } else {
                val distance = greatCircleDistanceMetres(previous, sample)
                val seconds = deltaMillis / 1_000.0
                val allowed = maximumRunningSpeedMetresPerSecond * seconds +
                    previous.horizontalErrorMeters + sample.horizontalErrorMeters
                if (distance / seconds > maximumRunningSpeedMetresPerSecond || distance > allowed) {
                    rejectedSamples++
                    return Acceptance(false, false, continuitySegment)
                }
            }
        }

        val startsNew = samples.isEmpty()
        samples.addLast(sample)
        trim(sample.monotonicMillis)
        return Acceptance(true, startsNew, continuitySegment)
    }

    fun estimate(nowMonotonicMillis: Long): PaceEstimate {
        val newest = samples.lastOrNull()
            ?: return unavailable(nowMonotonicMillis, PaceUnavailableReason.INSUFFICIENT_SAMPLES)
        val age = nowMonotonicMillis - newest.monotonicMillis
        if (age > 10_000) return unavailable(nowMonotonicMillis, PaceUnavailableReason.STALE, stale = true)
        if (age > 5_000) {
            return lastValidEstimate?.copy(calculatedAtMillis = nowMonotonicMillis, stale = true)
                ?: unavailable(nowMonotonicMillis, PaceUnavailableReason.STALE, stale = true)
        }

        trim(nowMonotonicMillis)
        val selected = samples.filter { it.monotonicMillis >= nowMonotonicMillis - windowSeconds * 1_000L }
        if (selected.size < 4) return unavailable(nowMonotonicMillis, PaceUnavailableReason.INSUFFICIENT_SAMPLES)

        val coverage = selected.last().monotonicMillis - selected.first().monotonicMillis
        if (coverage < windowSeconds * 700L || age > 3_000) {
            return unavailable(nowMonotonicMillis, PaceUnavailableReason.POOR_COVERAGE, stale = age > 3_000)
        }

        val points = toLocalPoints(selected)
        var fit = fitVelocity(points)
            ?: return unavailable(nowMonotonicMillis, PaceUnavailableReason.INVALID)

        val residuals = points.map { point ->
            val dx = point.east - (fit.interceptEast + fit.velocityEast * point.timeSeconds)
            val dy = point.north - (fit.interceptNorth + fit.velocityNorth * point.timeSeconds)
            sqrt(dx * dx + dy * dy)
        }
        val medianResidual = median(residuals)
        val mad = median(residuals.map { abs(it - medianResidual) })
        val threshold = max(10.0, 2.5 * mad)
        val filtered = points.zip(residuals).filter { (_, residual) -> residual <= threshold }.map { it.first }
        if (filtered.size >= 4) fit = fitVelocity(filtered) ?: fit

        var speed = sqrt(fit.velocityEast.pow(2) + fit.velocityNorth.pow(2))
        if (!speed.isFinite() || speed > maximumRunningSpeedMetresPerSecond) {
            return unavailable(nowMonotonicMillis, PaceUnavailableReason.INVALID)
        }

        if (lastEstimateAt == Long.MIN_VALUE || nowMonotonicMillis - lastEstimateAt >= 900) {
            recentSpeeds.addLast(nowMonotonicMillis to speed)
            lastEstimateAt = nowMonotonicMillis
        }
        while (recentSpeeds.size > 3) recentSpeeds.removeFirst()
        speed = median(recentSpeeds.map { it.second })

        if (speed < 0.56) return unavailable(nowMonotonicMillis, PaceUnavailableReason.STATIONARY, coverage)
        val secondsPerKm = (1_000.0 / speed).coerceAtLeast(120.0)
        return PaceEstimate(secondsPerKm, speed, coverage, nowMonotonicMillis).also {
            lastValidEstimate = it
        }
    }

    fun breakContinuity() {
        samples.clear()
        recentSpeeds.clear()
        lastEstimateAt = Long.MIN_VALUE
        lastValidEstimate = null
        continuitySegment++
    }

    fun reset() {
        breakContinuity()
        rejectedSamples = 0
    }

    private fun trim(now: Long) {
        val cutoff = now - (windowSeconds + 3) * 1_000L
        while (samples.isNotEmpty() && samples.first().monotonicMillis < cutoff) samples.removeFirst()
    }

    private fun unavailable(
        now: Long,
        reason: PaceUnavailableReason,
        coverage: Long = 0,
        stale: Boolean = false,
    ) = PaceEstimate(null, null, coverage, now, stale, reason)

    private fun isCoordinateValid(sample: LocationSample): Boolean =
        sample.latitude.isFinite() && sample.latitude in -90.0..90.0 &&
            sample.longitude.isFinite() && sample.longitude in -180.0..180.0 &&
            sample.horizontalErrorMeters.isFinite() && sample.horizontalErrorMeters >= 0.0

    private data class LocalPoint(val timeSeconds: Double, val east: Double, val north: Double, val weight: Double)
    private data class VelocityFit(
        val velocityEast: Double,
        val velocityNorth: Double,
        val interceptEast: Double,
        val interceptNorth: Double,
    )

    private fun toLocalPoints(source: List<LocationSample>): List<LocalPoint> {
        val origin = source.first()
        val earthRadius = 6_371_008.8
        val originLatRadians = origin.latitude * PI / 180.0
        return source.map { point ->
            val east = (point.longitude - origin.longitude) * PI / 180.0 * earthRadius * cos(originLatRadians)
            val north = (point.latitude - origin.latitude) * PI / 180.0 * earthRadius
            val error = point.horizontalErrorMeters.coerceIn(3.0, maximumHorizontalErrorMetres)
            val weight = (1.0 / (error * error)).coerceIn(1.0 / 1_225.0, 1.0 / 9.0)
            LocalPoint((point.monotonicMillis - origin.monotonicMillis) / 1_000.0, east, north, weight)
        }
    }

    private fun fitVelocity(points: List<LocalPoint>): VelocityFit? {
        val weightSum = points.sumOf { it.weight }
        if (weightSum <= 0.0) return null
        val meanTime = points.sumOf { it.timeSeconds * it.weight } / weightSum
        val meanEast = points.sumOf { it.east * it.weight } / weightSum
        val meanNorth = points.sumOf { it.north * it.weight } / weightSum
        val denominator = points.sumOf { it.weight * (it.timeSeconds - meanTime).pow(2) }
        if (denominator <= 1e-9) return null
        val velocityEast = points.sumOf { it.weight * (it.timeSeconds - meanTime) * (it.east - meanEast) } / denominator
        val velocityNorth = points.sumOf { it.weight * (it.timeSeconds - meanTime) * (it.north - meanNorth) } / denominator
        return VelocityFit(
            velocityEast,
            velocityNorth,
            meanEast - velocityEast * meanTime,
            meanNorth - velocityNorth * meanTime,
        )
    }

    private fun median(values: List<Double>): Double {
        if (values.isEmpty()) return Double.NaN
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) (sorted[middle - 1] + sorted[middle]) / 2.0 else sorted[middle]
    }

    companion object {
        fun greatCircleDistanceMetres(a: LocationSample, b: LocationSample): Double {
            val radius = 6_371_008.8
            val lat1 = a.latitude * PI / 180.0
            val lat2 = b.latitude * PI / 180.0
            val dLat = (b.latitude - a.latitude) * PI / 180.0
            val dLon = (b.longitude - a.longitude) * PI / 180.0
            val h = kotlin.math.sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * kotlin.math.sin(dLon / 2).pow(2)
            return 2 * radius * kotlin.math.asin(sqrt(h.coerceIn(0.0, 1.0)))
        }
    }
}
