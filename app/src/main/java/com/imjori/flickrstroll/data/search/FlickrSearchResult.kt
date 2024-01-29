package com.imjori.flickrstroll.data.search

sealed class FlickrSearchResult {
    data class PhotosFound(val photos: List<FlickrSearchPhotoMetadata>) : FlickrSearchResult()
    data object NoPhotosFound : FlickrSearchResult()
    data object Exception : FlickrSearchResult()
}
