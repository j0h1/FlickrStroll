package com.imjori.flickrstroll.data.search

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okio.IOException
import javax.inject.Inject

class FlickrPhotoSearchRepository @Inject constructor(
    private val flickrPhotoSearchApi: FlickrPhotoSearchApi
) {

    fun getNearbyPhotos(lat: Double, lon: Double): Flow<FlickrSearchResult> {
        return flow {
            try {
                val photosForCurrentLocation =
                    flickrPhotoSearchApi.getImagesNearCurrentLocation(lat, lon)
                        .photos
                        .photo

                if (photosForCurrentLocation.isEmpty()) {
                    emit(FlickrSearchResult.NoPhotoFound)
                } else {
                    emit(FlickrSearchResult.PhotoFound(photosForCurrentLocation))
                }
            } catch (exception: IOException) {
                Log.d(TAG, "Exception during photo fetching")
                emit(FlickrSearchResult.NoPhotoFound)
            }
        }.flowOn(Dispatchers.IO)
    }

    companion object {
        private const val TAG = "FlickrPhotoSearchRepository"
    }
}
