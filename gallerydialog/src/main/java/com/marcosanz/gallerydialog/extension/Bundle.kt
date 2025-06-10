package com.marcosanz.gallerydialog.extension

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat
import java.io.Serializable

internal inline fun <reified T : Parcelable> Bundle.getParcelableArrayListCompat(
    key: String,
    clazz: Class<out T>
): java.util.ArrayList<T>? = BundleCompat.getParcelableArrayList(this, key, clazz)

internal inline fun <reified T : Parcelable> Bundle.getParcelableCompat(
    key: String,
    clazz: Class<out T>
): T? = BundleCompat.getParcelable(this, key, clazz)

internal inline fun <reified T : Serializable> Bundle.getSerializableCompat(
    key: String,
    clazz: Class<out T>
): T? = BundleCompat.getSerializable(this, key, clazz)

