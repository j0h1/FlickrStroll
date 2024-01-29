package com.imjori.flickrstroll.presentation

import android.location.Location
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imjori.flickrstroll.data.location.LocationRepository
import com.imjori.flickrstroll.data.search.FlickrSearchPhotoMetadata
import com.imjori.flickrstroll.domain.model.Photo
import com.imjori.flickrstroll.domain.usecase.GetPhotoForLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class FlickrStrollViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val locationRepository: LocationRepository,
    private val getPhotoForLocation: GetPhotoForLocationUseCase,
) : ViewModel() {

    private val _viewState = mutableStateOf(savedStateHandle[VIEW_STATE_KEY] ?: ViewState())
    val viewState: State<ViewState> = _viewState

    private val _event = MutableSharedFlow<Event>()
    val event: SharedFlow<Event> = _event

    private var getPhotosForLocationJob: Job? = null

    init {
        viewModelScope.launch {
            if (viewState.value.isWalkStarted) {
                _event.emit(Event.RestartLocationPollingService)
            }
        }
    }

    fun toggleWalk() {
        val isWalkStartedToggled = !_viewState.value.isWalkStarted

        initGetPhotosForLocationJob(shouldStart = isWalkStartedToggled)

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

    private fun initGetPhotosForLocationJob(shouldStart: Boolean) {
        getPhotosForLocationJob = if (shouldStart) {
            locationRepository.locationFlow
                .onEach { location ->
                    handleNewLocation(location)
                }.launchIn(viewModelScope)
        } else {
            getPhotosForLocationJob?.cancel()
            null
        }
    }

    private suspend fun handleNewLocation(location: Location) {
        val getPhotoForLocationResult = getPhotoForLocation(
            currentPhotos = _viewState.value.photos,
            lat = location.latitude,
            lon = location.longitude,
        )

        when (getPhotoForLocationResult) {
            is GetPhotoForLocationUseCase.GetPhotoForLocationResult.PhotoFound -> {
                handlePhotoFound(
                    photoMetadata = getPhotoForLocationResult.photoMetadata,
                )
            }

            GetPhotoForLocationUseCase.GetPhotoForLocationResult.NoPhotoFound -> {
                Log.d(TAG, "No photo found for current location")
            }
        }
    }

    private fun handlePhotoFound(photoMetadata: FlickrSearchPhotoMetadata) {
        val photo = Photo(
            url = photoMetadata.getUrl(),
            contentDescription = photoMetadata.title,
            index = _viewState.value.photos.size
        )

        val updatedPhotos = _viewState.value.photos.toMutableList()
        updatedPhotos.add(photo)
        updatedPhotos.sortByDescending { it.index }

        Log.d(TAG, "Adding photo to stream: ${photo.url}")

        _viewState.value = _viewState.value.copy(photos = updatedPhotos)

        savedStateHandle[VIEW_STATE_KEY] = _viewState.value
    }

    @Parcelize
    data class ViewState(
        val isWalkStarted: Boolean = false,
        val photos: List<Photo> = emptyList()
    ) : Parcelable

    sealed class Event {
        data object WalkStarted : Event()
        data object WalkStopped : Event()
        data object RestartLocationPollingService : Event()
    }

    companion object {
        private const val TAG = "FlickrStrollViewModel"
        private const val VIEW_STATE_KEY = "viewState"
    }
}
