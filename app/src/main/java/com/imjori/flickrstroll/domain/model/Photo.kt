package com.imjori.flickrstroll.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Photo(
    val url: String,
    val contentDescription: String? = null,
    val index: Int
) : Parcelable
