package com.bangkit.auris.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataDictionary(
    var title: String,
    var image: Int
) : Parcelable