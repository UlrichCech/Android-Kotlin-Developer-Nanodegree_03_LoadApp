package com.udacity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadResult(val filename: String, val result: String): Parcelable
