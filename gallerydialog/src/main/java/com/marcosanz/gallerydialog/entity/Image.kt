package com.marcosanz.gallerydialog.entity

import android.os.Parcel
import android.os.Parcelable

data class Image(
    val thumbnail: String? = null,
    val url: String? = null,
    val alt: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(thumbnail)
        parcel.writeString(url)
        parcel.writeString(alt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Image> {
        override fun createFromParcel(parcel: Parcel): Image {
            return Image(parcel)
        }

        override fun newArray(size: Int): Array<Image?> {
            return arrayOfNulls(size)
        }
    }
}