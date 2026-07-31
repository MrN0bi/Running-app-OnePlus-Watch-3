package com.watchrunning.app.exercise

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/** Fused location updates which Wear OS may source from the watch, Wi-Fi, or paired phone. */
class FusedLocationGateway(
    context: Context,
    private val onLocation: (Location) -> Unit,
    private val onFailure: (Throwable) -> Unit,
) {
    private val applicationContext = context.applicationContext
    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(applicationContext)
    private var started = false

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach(onLocation)
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (started || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MILLIS)
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MILLIS)
            .setWaitForAccurateLocation(false)
            .build()
        started = true
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { error ->
                started = false
                Log.w(TAG, "Unable to start fused location", error)
                onFailure(error)
            }
    }

    fun stop() {
        if (!started) return
        started = false
        client.removeLocationUpdates(callback)
    }

    private companion object {
        const val TAG = "WatchRunning"
        const val UPDATE_INTERVAL_MILLIS = 1_000L
        const val MIN_UPDATE_INTERVAL_MILLIS = 500L
    }
}
