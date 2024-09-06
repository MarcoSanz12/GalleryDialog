package com.marcosanz.gallerydialog.entity

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class Image(
    open val alt: String? = null,
    open val url: String? = null,
    open val uri: Uri? = null,
    @DrawableRes open val drawable: Int? = null
) : Parcelable {

    internal data class URL(
        override val alt: String? = null,
        override val url: String? = null
    ) : Image()

    internal data class Drawable(
        override val alt: String? = null,
        @DrawableRes override val drawable: Int? = null
    ) : Image()

    internal data class URI(
        override val alt: String? = null,
        override val uri: Uri? = null
    ) : Image()

}