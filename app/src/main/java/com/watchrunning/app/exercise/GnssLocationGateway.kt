package com.watchrunning.app.exercise

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat

/** Direct watch-local GNSS updates, preferred over the hybrid fallback sources. */
class GnssLocationGateway(
    context: Context,
    private val onLocation: (Location) -> Unit,
    private val onUnavailable: () -> Unit,
) {
    private val applicationContext = context.applicationContext
    private val locationManager = applicationContext.getSystemService(LocationManager::class.java)
    private var started = false

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) = onLocation(location)
        override fun onProviderDisabled(provider: String) {
            if (provider == LocationManager.GPS_PROVIDER) onUnavailable()
        }
        override fun onProviderEnabled(provider: String) = Unit
        @Deprecated("Deprecated by Android")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun start() {
        if (started || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) return
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            onUnavailable()
            return
        }
        started = true
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            UPDATE_INTERVAL_MILLIS,
            0f,
            listener,
            Looper.getMainLooper(),
        )
    }

    fun stop() {
        if (!started) return
        started = false
        locationManager.removeUpdates(listener)
    }

    private companion object {
        const val UPDATE_INTERVAL_MILLIS = 1_000L
    }
}
