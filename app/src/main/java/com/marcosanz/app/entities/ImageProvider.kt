package com.marcosanz.app.entities

import android.content.Context
import com.marcosanz.app.R
import com.marcosanz.app.extension.saveAndGetUri

object ImageProvider {

    fun getImagesUri(context: Context, authority: String, drawableRes: List<Int>): List<ImageURI> =
        drawableRes.map {
            val uri = context.saveAndGetUri(it, it, authority)
            ImageURI(
                thumbnail = uri,
                uri = uri
            )
        }

    val imagesUrl = listOf(
        ImageURL(
            thumbnail = "https://media.licdn.com/dms/image/C4E03AQHc8TgMSagC7Q/profile-displayphoto-shrink_800_800/0/1614784623062?e=2147483647&v=beta&t=0dARt006OVNRV5uozNO_aXoxOG7Wk-BxX5cDwrWNe9g",
            url = "https://media.licdn.com/dms/image/C4E03AQHc8TgMSagC7Q/profile-displayphoto-shrink_800_800/0/1614784623062?e=2147483647&v=beta&t=0dARt006OVNRV5uozNO_aXoxOG7Wk-BxX5cDwrWNe9g",
            alt = "Famous spanish youtuber Willyrex"
        ),
        ImageURL(
            thumbnail = "https://brobible.com/wp-content/uploads/2022/05/its-morbin-time.jpg",
            url = "https://brobible.com/wp-content/uploads/2022/05/its-morbin-time.jpg",
            alt = "Doctor Michael Morbius from the comic series Batman"
        )
    )

    val imagesDrawable = listOf(
        ImageDrawable(
            alt = "Cuellar Castle",
            resId = R.drawable.cuellar1
        ),
        ImageDrawable(
            alt = "Cuellar Castle II",
            resId = R.drawable.cuellar2
        )
    )

    val images360Url = listOf(
        ImageURL(
            thumbnail = "https://media.macphun.com/img/uploads/customer/blog/2432/169339558164ef2a7d7179d5.77274305.jpg?q=85&w=1680",
            url = "https://media.macphun.com/img/uploads/customer/blog/2432/169339558164ef2a7d7179d5.77274305.jpg?q=85&w=1680",
        ),
        ImageURL(
            thumbnail = "https://img.youtube.com/vi/VaZcXQ8Th7s/maxresdefault.jpg",
            url = "https://img.youtube.com/vi/VaZcXQ8Th7s/maxresdefault.jpg",
        )
    )

    val images360Drawable = listOf(
        ImageDrawable(
            alt = "Image 360",
            resId = R.drawable.image360_1
        ),
        ImageDrawable(
            resId = R.drawable.image360_2
        )
    )
}