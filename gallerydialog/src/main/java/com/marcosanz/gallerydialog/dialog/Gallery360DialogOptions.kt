package com.marcosanz.gallerydialog.dialog

import android.os.Parcel
import android.os.Parcelable

data class Gallery360DialogOptions(

    /**
     * Message shown if image loading fails
     */
    val errorMessage: String? = null,

    /**
     * If true, the dialog will allow orientation change and a rotation button will be shown
     */
    val rotation: Boolean = true,

    /**
     * Allows sensorial rotation via Gyroscope, Accelerometer & Compass.
     */
    val sensorialRotation : Boolean = true
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as Boolean
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(errorMessage)
        parcel.writeValue(rotation)
        parcel.writeValue(sensorialRotation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Gallery360DialogOptions> {
        override fun createFromParcel(parcel: Parcel): Gallery360DialogOptions {
            return Gallery360DialogOptions(parcel)
        }

        override fun newArray(size: Int): Array<Gallery360DialogOptions?> {
            return arrayOfNulls(size)
        }
    }
}