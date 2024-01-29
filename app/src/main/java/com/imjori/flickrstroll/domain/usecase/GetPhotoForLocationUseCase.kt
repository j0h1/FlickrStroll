package com.imjori.flickrstroll.domain.usecase

import android.util.Log
import com.imjori.flickrstroll.data.search.FlickrPhotoSearchRepository
import com.imjori.flickrstroll.data.search.FlickrSearchPhotoMetadata
import com.imjori.flickrstroll.data.search.FlickrSearchResult
import com.imjori.flickrstroll.domain.model.Photo
import javax.inject.Inject

class GetPhotoForLocationUseCase @Inject constructor(
    private val flickrPhotoSearchRepository: FlickrPhotoSearchRepository,
) {

    suspend operator fun invoke(
        currentPhotos: List<Photo>,
        lat: Double,
        lon: Double,
        radius: Double = RADIUS.first(),
    ): GetPhotoForLocationResult {
        return when (val searchResult =
            flickrPhotoSearchRepository.getNearbyPhotos(lat, lon, radius)) {
            is FlickrSearchResult.PhotosFound -> {
                Log.d(TAG, "${searchResult.photos.size} photos found")
                GetPhotoForLocationResult.PhotoFound(
                    photoMetadata = searchResult.photos.pickPhoto(currentPhotos)
                )
            }

            FlickrSearchResult.NoPhotosFound -> {
                if (radius != RADIUS.last()) {
                    val newRadius = radius.pickNextRadius()
                    Log.d(TAG, "No photo found. Fetch again with radius=$newRadius")
                    invoke(currentPhotos, lat, lon, newRadius)
                } else {
                    Log.d(TAG, "No photos found")
                    GetPhotoForLocationResult.NoPhotoFound
                }
            }

            FlickrSearchResult.Exception -> GetPhotoForLocationResult.NoPhotoFound
        }
    }

    /**
     * This function picks a photo based on the following logic:
     *  1. Pick the first (most relevant) photo, if it's not already in the feed
     *  2. If it's already in the feed, find the next most relevant photo that is not already in the feed
     *  3. If all photos are already in the feed, pick a random one
     */
    private fun List<FlickrSearchPhotoMetadata>.pickPhoto(
        currentPhotos: List<Photo>
    ): FlickrSearchPhotoMetadata {
        val currentPhotoUrls = currentPhotos.map { it.url }
        return firstOrNull { it.getUrl() !in currentPhotoUrls } ?: random()
    }

    private fun Double.pickNextRadius(): Double {
        return RADIUS[RADIUS.indexOf(this) + 1]
    }

    sealed interface GetPhotoForLocationResult {
        data class PhotoFound(val photoMetadata: FlickrSearchPhotoMetadata) :
            GetPhotoForLocationResult

        data object NoPhotoFound : GetPhotoForLocationResult
    }

    companion object {
        private const val TAG = "GetPhotoForLocationUseCase"
        private val RADIUS = listOf(0.5, 1.0, 2.0, 5.0)
    }
}