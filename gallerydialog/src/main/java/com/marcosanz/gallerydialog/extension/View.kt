package com.marcosanz.gallerydialog.extension

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.display.DisplayManager
import android.net.Uri
import android.provider.MediaStore
import android.view.Display
import android.view.Surface
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import coil3.asDrawable
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.marcosanz.gallerydialog.entity.Image
import com.marcosanz.gallerydialog.utils.Constant
import com.panoramagl.ios.enumerations.UIDeviceOrientation
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

internal fun ImageView.loadFromImage(
    image: Image? = null,
    defaultDrawable: Drawable? = null,
    onResourceReady: (Drawable?) -> Unit
) {

    val request = ImageRequest.Builder(context).data(
        when (image) {
            is Image.Drawable -> image.drawable
            is Image.URI -> image.uri
            is Image.URL -> image.url
            else -> null
        }
    )
        .crossfade(true)
        .error(defaultDrawable)
        .target(
            onSuccess = { result ->
                onResourceReady(result.asDrawable(resources))
            },
            onError = { result ->
                onResourceReady(result?.asDrawable(resources))
            }
        ).build()

    context.imageLoader.enqueue(request)
}


internal fun Image?.getBitmapWithCoil(
    context: Context,
    errorDrawable: Drawable? = null,
    onBitmapLoaded: (Bitmap) -> Unit,
    onBitmapError: () -> Unit
) {
    if (this == null) {
        onBitmapError()
        return
    }

    try {
        val request = ImageRequest.Builder(context).data(
            when (this) {
                is Image.Drawable -> this.drawable
                is Image.URI -> this.uri
                is Image.URL -> this.url
            }
        )
            .error(errorDrawable)
            .target(
                onSuccess = { result ->
                    val bitmap = rescaleBitmap(result.toBitmap())
                    onBitmapLoaded(bitmap)
                },
                onError = { result ->
                    val bitmap = (result as? BitmapDrawable)?.bitmap?.let {
                        rescaleBitmap(it)
                    }

                    if (bitmap != null)
                        onBitmapLoaded(bitmap)
                    else
                        onBitmapError()
                }
            )
            .build()

        context.imageLoader.enqueue(request)
    } catch (ex: Exception) {
        ex.printStackTrace()
        onBitmapError()
    }
}


internal fun Image?.getBitmapWithGlide(
    context: Context,
    errorDrawable: Drawable? = null,
    onBitmapLoaded: (Bitmap) -> Unit, onBitmapError: () -> Unit
) {
    if (this == null) {
        onBitmapError()
        return
    }

    try {
        val request = Glide.with(context).asBitmap()
        when (this) {
            is Image.Drawable -> request.load(this.drawable)
            is Image.URI -> request.load(this.uri)
            is Image.URL -> request.load(this.url)
        }
            .error(errorDrawable)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onBitmapLoaded(rescaleBitmap(resource))
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)

                    if (errorDrawable != null)
                        onBitmapLoaded(rescaleBitmap(errorDrawable.toBitmap()))
                    else
                        onBitmapError()
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    } catch (ex: Exception) {
        ex.printStackTrace()
        onBitmapError()
    }
}

internal fun rescaleBitmap(
    ogBitmap: Bitmap,
    maxWidth: Int = Constant.MAX_WIDTH,
    maxHeight: Int = Constant.MAX_HEIGHT
): Bitmap {
    if (ogBitmap.width <= maxWidth && ogBitmap.height <= maxHeight) {
        return ogBitmap // Rescaling unnecesary
    }

    val ratio = minOf(maxWidth.toFloat() / ogBitmap.width, maxHeight.toFloat() / ogBitmap.height)
    val newWidth = (ogBitmap.width * ratio).toInt()
    val newHeight = (ogBitmap.height * ratio).toInt()

    return ogBitmap.scale(newWidth, newHeight)
}

internal fun Context.loadDrawable(
    image: Image?,
    @DrawableRes errorDrawable: Int?,
    callback: (Bitmap?) -> Unit
) {
    try {
        var builder = ImageRequest.Builder(this).data(
            when (image) {
                is Image.Drawable -> image.drawable
                is Image.URI -> image.uri
                is Image.URL -> image.url
                else -> null
            }
        )
        errorDrawable?.let {
            builder = builder.error(it)
        }

        val request = builder.target(
            onSuccess = { result ->
                callback(result.toBitmap())  // Drawable is ready, sending it to callback
            },
            onError = { result ->
                callback(result?.toBitmap())  // If it fails, send the error drawable
            }
        ).build()


        imageLoader.enqueue(request)
    } catch (ex: Exception) {
        ex.printStackTrace()
        errorDrawable?.let {
            callback(AppCompatResources.getDrawable(this, it)?.toBitmap())
        }
    }
}

internal fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable)
        return this.bitmap


    val bitmap = createBitmap(intrinsicWidth, intrinsicHeight)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)

    return bitmap
}

internal fun Activity?.getUIDeviceOrientation(): UIDeviceOrientation? {
    val displayManager =
        (this?.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager) ?: return null
    val rotation = displayManager.getDisplay(Display.DEFAULT_DISPLAY).rotation
    return when (rotation) {
        Surface.ROTATION_0 -> UIDeviceOrientation.UIDeviceOrientationPortrait
        Surface.ROTATION_90 -> UIDeviceOrientation.UIDeviceOrientationLandscapeLeft
        Surface.ROTATION_180 -> UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown
        Surface.ROTATION_270 -> UIDeviceOrientation.UIDeviceOrientationLandscapeRight
        else -> null
    }
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