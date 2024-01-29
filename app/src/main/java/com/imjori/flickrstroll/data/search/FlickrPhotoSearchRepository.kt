package com.imjori.flickrstroll.data.search

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import javax.inject.Inject

class FlickrPhotoSearchRepository @Inject constructor(
    private val flickrPhotoSearchApi: FlickrPhotoSearchApi
) {

    suspend fun getNearbyPhotos(lat: Double, lon: Double, radius: Double): FlickrSearchResult =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Get nearby photos for: lat=$lat, lon=$lon, radius=$radius")
            try {
                val photosForCurrentLocation =
                    flickrPhotoSearchApi.getImagesNearCurrentLocation(lat, lon, radius)
                        .photos
                        .photo

                if (photosForCurrentLocation.isEmpty()) {
                    return@withContext FlickrSearchResult.NoPhotosFound
                } else {
                    return@withContext FlickrSearchResult.PhotosFound(photosForCurrentLocation)
                }
            } catch (exception: IOException) {
                Log.d(TAG, "Exception during photo fetching: $exception")
                return@withContext FlickrSearchResult.Exception
            }
        }

    companion object {
        private const val TAG = "FlickrPhotoSearchRepository"
    }
}
