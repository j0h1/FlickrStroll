package com.imjori.flickrstroll.data.search

data class FlickrSearchPhotoMetadata(
    val id: String,
    val owner: String,
    val secret: String,
    val server: String,
    val farm: String,
    val title: String
) {
    /**
     * Returns a valid image URL based on the received metadata
     * Format: https://farm{farm_id}.staticflickr.com/{server_id}/{photo_id}_{secret}.jpg
     */
    fun getUrl(): String = "${IMAGE_URL_PREFIX}${farm}${IMAGE_URL_DOMAIN}/${server}/${id}_${secret}${PREFERRED_FILE_ENDING}"

    companion object {
        private const val IMAGE_URL_PREFIX = "https://farm"
        private const val IMAGE_URL_DOMAIN = ".staticflickr.com"
        private const val PREFERRED_FILE_ENDING = ".jpg"
    }
}
