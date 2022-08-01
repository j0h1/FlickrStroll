package com.imjori.flickrstroll.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.create().apply {
        interval = MAX_LOCATION_UPDATE_INTERVAL_IN_MS
        fastestInterval = MIN_LOCATION_UPDATE_INTERVAL_IN_MS
        priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
    }

    @SuppressLint("MissingPermission")
    private val _locationUpdates = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    Log.d(TAG, "New location: lat=${it.latitude}, lon=${it.longitude}")
                }
                trySend(locationResult.lastLocation)
            }
        }

        Log.d(TAG, "Starting location updates")

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            close(e)
        }

        awaitClose {
            Log.d(TAG, "Stopping location updates")
            fusedLocationProviderClient.removeLocationUpdates(callback)
        }
    }.shareIn(
        scope = GlobalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    fun locationFlow(): Flow<Location?> = _locationUpdates

    companion object {
        private const val TAG = "LocationRepository"
        private const val MAX_LOCATION_UPDATE_INTERVAL_IN_MS = 10000L
        private const val MIN_LOCATION_UPDATE_INTERVAL_IN_MS = 5000L
    }
}
