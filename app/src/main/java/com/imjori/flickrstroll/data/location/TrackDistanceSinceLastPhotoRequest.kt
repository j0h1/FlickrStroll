package com.imjori.flickrstroll.data.location

import android.location.Location
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackDistanceSinceLastPhotoRequest @Inject constructor() {

    private val _requestPhoto = MutableSharedFlow<Location>()
    val requestPhoto = _requestPhoto.asSharedFlow()

    private var distanceTravelled = 0f
    private var recentLocation: Location? = null

    suspend operator fun invoke(currentLocation: Location?) {
        if (currentLocation == null) return

        if (recentLocation == null) {
            recentLocation = currentLocation
        }

        distanceTravelled += recentLocation!!.distanceTo(currentLocation)

        if (distanceTravelled >= DISTANCE_THRESHOLD_IN_METERS) {
            distanceTravelled = 0f
            _requestPhoto.emit(currentLocation)
        }
    }

    fun reset() {
        recentLocation = null
        distanceTravelled = 0f
    }

    companion object {
        private const val DISTANCE_THRESHOLD_IN_METERS = 100.0f
    }
}
