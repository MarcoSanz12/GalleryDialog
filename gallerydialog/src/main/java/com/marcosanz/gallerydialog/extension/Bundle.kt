package com.marcosanz.gallerydialog.extension

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

internal inline fun <reified T : Parcelable> Bundle?.getParcelableArrayListCompat(
    key: String,
    clazz: Class<out T>
): java.util.ArrayList<T>? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this?.getParcelableArrayList(key, clazz)
    else
        this?.getParcelableArrayList(key)

internal inline fun <reified T: Parcelable> Bundle?.getParcelableCompat(
    key: String,
    clazz: Class<out T>
): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this?.getParcelable(key, clazz)
    else
        this?.getParcelable(key)

internal inline fun <reified T : Serializable> Bundle.getSerializableCompat(key:String, clazz: Class<out T>): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getSerializable(key,clazz)
    } else {
        @Suppress("DEPRECATION")
        getSerializable(key) as? T
    }
}

