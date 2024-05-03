package com.marcosanz.gallerydialog.extension

import android.os.Build


internal fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

internal fun CharSequence?.isNotNullOrEmpty(): Boolean = !isNullOrEmpty()

internal inline fun <T> sdk29AndUp(onSdk29: () -> T): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        onSdk29()
    else
        null