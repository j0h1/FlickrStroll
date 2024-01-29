package com.imjori.flickrstroll.data.search

import com.imjori.flickrstroll.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrPhotoSearchApi {

    @GET("rest/")
    suspend fun getImagesNearCurrentLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Double = RADIUS,
        @Query("method") method: String = PHOTO_SEARCH_METHOD,
        @Query("api_key") apiKey: String = BuildConfig.FLICKR_API_KEY,
        @Query("format") format: String = FORMAT,
        @Query("media") media: String = MEDIA_TYPE_PHOTOS,
        @Query("content_types") contentTypes: Int = CONTENT_TYPES_PHOTOS_ONLY,
        @Query("radius_units") radiusUnits: String = RADIUS_UNITS,
        @Query("has_geo") hasGeo: Int = HAS_BEEN_GEOTAGGED,
        @Query("nojsoncallback") noJsonCallback: Int = NO_JSON_CALLBACK,
    ): FlickrSearchPhotoResponse

    companion object {
        private const val FORMAT = "json"
        private const val PHOTO_SEARCH_METHOD = "flickr.photos.search"

        // Query parameter "media" defines if we are interested in photos, videos or both
        private const val MEDIA_TYPE_PHOTOS = "photos"

        /* Query parameter "content_types" defines the actual content of the photo,
           e.g. screenshot, photographs or other */
        private const val CONTENT_TYPES_PHOTOS_ONLY = 0
        private const val RADIUS = 0.5
        private const val RADIUS_UNITS = "km"
        private const val HAS_BEEN_GEOTAGGED = 1

        // Prevents wrapping of response as such: jsonFlickrApi(<response>)
        private const val NO_JSON_CALLBACK = 1
    }
}
