package com.marcosanz.app.entities

import androidx.annotation.DrawableRes

data class ImageDrawable(
    val alt: String? = null,
    @DrawableRes val resId: Int? = null
)

