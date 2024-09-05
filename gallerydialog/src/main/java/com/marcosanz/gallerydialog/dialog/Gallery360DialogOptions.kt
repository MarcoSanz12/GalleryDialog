package com.marcosanz.gallerydialog.dialog

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class Gallery360DialogOptions(

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
    val sensorialRotation: Boolean = true,

    /**
     * Image resource shown if the image loading fails
     */
    @DrawableRes
    val errorDrawable: Int? = null
) : Parcelable