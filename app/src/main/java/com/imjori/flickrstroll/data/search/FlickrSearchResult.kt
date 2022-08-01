package com.imjori.flickrstroll.data.search

sealed class FlickrSearchResult {
    data class PhotoFound(val photos: List<FlickrSearchPhotoMetadata>) : FlickrSearchResult()
    object NoPhotoFound : FlickrSearchResult()
}
