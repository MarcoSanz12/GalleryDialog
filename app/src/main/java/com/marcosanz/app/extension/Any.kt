package com.marcosanz.app.extension

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun Context.saveAndGetUri(@DrawableRes resId: Int, number: Int, authority: String): Uri? {
    val folder = File(this.cacheDir, "images")

    return try {
        folder.mkdirs()
        val fileName = "image_drawable"

        val file = File(folder, "${number}_${fileName}.png")
        val fos = FileOutputStream(file)

        val bitmap = (AppCompatResources.getDrawable(this, resId) as? BitmapDrawable)?.bitmap
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fos)

        fos.flush()
        fos.close()

        FileProvider.getUriForFile(this, authority, file)

    } catch (ex: Exception) {
        ex.printStackTrace()
        null
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
