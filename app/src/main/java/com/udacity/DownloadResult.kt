package com.udacity

import android.os.Parcel

import android.os.Parcelable


data class DownloadResult(val filename: String, val result: String): Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readString().toString()) {

    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(filename)
        dest.writeString(result)
    }


    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DownloadResult> = object : Parcelable.Creator<DownloadResult> {

            override fun createFromParcel(parcel: Parcel): DownloadResult {
                return DownloadResult(parcel)
            }

            override fun newArray(size: Int): Array<DownloadResult?> {
                return arrayOfNulls(size)
            }
        }
    }
}
