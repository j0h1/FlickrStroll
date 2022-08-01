package com.imjori.flickrstroll.presentation

import android.location.Location
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imjori.flickrstroll.data.location.TrackDistanceSinceLastPhotoRequest
import com.imjori.flickrstroll.data.search.FlickrPhotoSearchRepository
import com.imjori.flickrstroll.data.search.FlickrSearchPhotoMetadata
import com.imjori.flickrstroll.data.search.FlickrSearchResult
import com.imjori.flickrstroll.domain.model.Photo
import com.imjori.flickrstroll.presentation.util.Event
import com.imjori.flickrstroll.presentation.util.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlickrStrollViewModel @Inject constructor(
    private val flickrPhotoSearchRepository: FlickrPhotoSearchRepository,
    private val savedStateHandle: SavedStateHandle,
    trackDistanceSinceLastPhotoRequest: TrackDistanceSinceLastPhotoRequest
) : ViewModel() {

    private val _viewState = mutableStateOf(savedStateHandle[VIEW_STATE_KEY] ?: ViewState())
    val viewState: State<ViewState> = _viewState

    private val _event = MutableSharedFlow<Event>()
    val event: SharedFlow<Event> = _event

    init {
        trackDistanceSinceLastPhotoRequest.requestPhoto.onEach { location ->
            requestPhotoForLocation(location)
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            if (viewState.value.isWalkStarted) {
                _event.emit(Event.RestartLocationPollingService)
            }
        }
    }

    private fun requestPhotoForLocation(location: Location) {
        Log.d(TAG, "Requesting photo for current location")

        flickrPhotoSearchRepository.getNearbyPhotos(
            lat = location.latitude,
            lon = location.longitude
        )
            .onEach { searchResult ->
                when (searchResult) {
                    is FlickrSearchResult.PhotoFound -> {
                        handleNearbyPhotosFound(searchResult.photos)
                    }
                    FlickrSearchResult.NoPhotoFound -> {
                        Log.d(TAG, "No photo found for current location")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleWalk() {
        val isWalkStartedToggled = !_viewState.value.isWalkStarted

        _viewState.value = _viewState.value.copy(
            isWalkStarted = isWalkStartedToggled,
            photos = if (isWalkStartedToggled) emptyList() else _viewState.value.photos
        )

        viewModelScope.launch {
            _event.emit(
                if (isWalkStartedToggled) Event.WalkStarted else Event.WalkStopped
            )
        }

        savedStateHandle[VIEW_STATE_KEY] = _viewState.value
    }

    private fun handleNearbyPhotosFound(photos: List<FlickrSearchPhotoMetadata>) {
        val newPhotoMetadata = photos.firstOrNull { photoMetadata ->
            !photoAlreadyInStream(photoMetadata)
        }

        if (newPhotoMetadata == null) {
            Log.d(TAG, "No new photo that is not already in stream")
            return
        }

        val photo = Photo(
            url = newPhotoMetadata.getUrl(),
            contentDescription = newPhotoMetadata.title,
            index = _viewState.value.photos.size
        )

        val updatedPhotos = _viewState.value.photos.toMutableList()
        updatedPhotos.add(photo)
        updatedPhotos.sortByDescending { it.index }

        Log.d(TAG, "Adding photo to stream: ${photo.url}")

        _viewState.value = _viewState.value.copy(photos = updatedPhotos)

        savedStateHandle[VIEW_STATE_KEY] = _viewState.value
    }


    private fun photoAlreadyInStream(metadata: FlickrSearchPhotoMetadata): Boolean {
        return _viewState.value.photos.map { it.url }.contains(metadata.getUrl())
    }

    companion object {
        private const val TAG = "FlickrStrollViewModel"
        private const val VIEW_STATE_KEY = "viewState"
    }
}
