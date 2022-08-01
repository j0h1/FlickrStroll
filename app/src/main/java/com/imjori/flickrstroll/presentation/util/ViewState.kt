package com.imjori.flickrstroll.presentation.util

import android.os.Parcelable
import com.imjori.flickrstroll.domain.model.Photo
import kotlinx.parcelize.Parcelize

@Parcelize
data class ViewState(
    val isWalkStarted: Boolean = false,
    val photos: List<Photo> = emptyList()
) : Parcelable
