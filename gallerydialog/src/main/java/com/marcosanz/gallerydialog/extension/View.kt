package com.marcosanz.gallerydialog.extension

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


internal fun View.visible(animate: Boolean = false, y: Float? = null) {
    isVisible = true
    if (animate && alpha < 1f)
        animate(duration = 0.2f, alpha = 1f, y = y)
}

internal fun View.invisible(animate: Boolean = false, y: Float? = null) {
    if (!animate)
        isVisible = false
    else if (!isInvisible)
        animate(duration = 0.2f, alpha = 0f, y = y) {
            isVisible = false
        }
}

internal fun ImageView.loadFromUrl(
    url: String?,
    defaultBitmap: Bitmap? = null,
    onResourceReady: (Drawable?) -> Unit
) {

    if (url.isNullOrEmpty()) {
        this.setImageBitmap(defaultBitmap)
        return
    }

    Glide.with(this).load(url).transition(
        DrawableTransitionOptions.withCrossFade(
            DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true)
        )
    )
        .error(defaultBitmap)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                onResourceReady(null)
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                onResourceReady(resource)
                return false
            }

        })
        .into(object : CustomTarget<Drawable?>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable?>?
            ) {

                setImageDrawable(resource)
            }


            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })
}

internal fun Context.loadDrawable(imageUrl: String?, callback: (Bitmap?) -> Unit) {
    imageUrl ?: return

    Glide.with(this)
        .load(imageUrl)
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                callback(resource.toBitmap())  // El drawable está listo, lo pasamos al callback
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // Lógica para limpiar si es necesario
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                callback(errorDrawable?.toBitmap())  // Si falla, pasamos el drawable de error
            }
        })
}

internal fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable)
        return this.bitmap


    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)

    return bitmap
}

internal fun Bitmap.shareImage(context: Context, authority: String, shareMessage: String?) {
    val folder = File(context.cacheDir, "images")

    val uri: Uri?
    try {
        folder.mkdirs()
        val fileName = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date(System.currentTimeMillis()))
        val file = File(folder, "${fileName}.png")
        val fos = FileOutputStream(file)

        compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        uri = FileProvider.getUriForFile(context, authority, file) ?: return

        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            setType("image/png")
        }
        context.startActivity(Intent.createChooser(intent, shareMessage))


    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

internal fun Bitmap.saveBitmapToPictures(context: Context): Boolean {

    val fileName = SimpleDateFormat(
        "yyyyMMdd_HHmmss",
        Locale.getDefault()
    ).format(Date(System.currentTimeMillis()))

    val imageCollection = sdk29AndUp {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.WIDTH, width)
        put(MediaStore.Images.Media.HEIGHT, height)
    }

    return try {
        context.contentResolver.insert(imageCollection, contentValues)?.also { uri ->
            context.contentResolver.openOutputStream(uri)?.use { os ->
                if (!compress(Bitmap.CompressFormat.PNG, 100, os))
                    throw IOException("Couldn't save bitmap")
            } ?: throw IOException("Couldn't save bitmap")
        } ?: throw IOException("Couldn't create MediaStore storage")
        true

    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}


/**
 * Executes basic animations over a [View]
 * Non-specified values will take the initial View's value as default
 * No param is mandatory
 * @param duration anim duration in milliseconds
 * @param y vertical displacement
 * @param alpha ending alpha (for fading effects)
 * @param endAction function called after the animation has ended
 */
internal fun <T : View> T.animate(
    duration: Float = 1F,
    y: Float? = null,
    alpha: Float? = null,
    endAction: ((T) -> Unit)? = null
) {
    val animacion = this.animate()
    duration.notNull { animacion.duration = (it * 1000).toLong() }
    y.notNull { animacion.translationY(it) }
    alpha.notNull { animacion.alpha(it) }
    endAction.notNull { animacion.withEndAction { it.invoke(this) } }
    animacion.interpolator = AccelerateDecelerateInterpolator()
    animacion.start()
}