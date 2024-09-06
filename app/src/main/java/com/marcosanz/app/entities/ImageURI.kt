package com.marcosanz.app.entities

import android.net.Uri

data class ImageURI(
    val thumbnail: Uri? = null,
    val alt: String? = null,
    val uri: Uri? = null
)