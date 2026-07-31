package com.watchrunning.app.calculation

import java.time.Duration
import java.util.Locale
import kotlin.math.roundToLong

object MetricFormatters {
    const val UNAVAILABLE_PACE = "—:—"

    fun pace(secondsPerKilometre: Double?): String {
        if (secondsPerKilometre == null || !secondsPerKilometre.isFinite() || secondsPerKilometre <= 0.0) {
            return UNAVAILABLE_PACE
        }
        val seconds = secondsPerKilometre.roundToLong().coerceAtLeast(0)
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)
    }

    fun distance(metres: Double): String {
        val safeMetres = metres.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
        return String.format(Locale.ROOT, "%.2f", safeMetres / 1_000.0)
    }

    fun distanceKilometres(metres: Double): String = "${distance(metres)} km"

    fun duration(duration: Duration): String {
        val totalSeconds = duration.seconds.coerceAtLeast(0)
        val hours = totalSeconds / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
        }
    }
}
