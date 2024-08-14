package com.marcosanz.gallerydialog.extension

import android.content.Context
import android.os.Build
import android.view.WindowManager

internal fun Context?.getDisplayRotation(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this?.display?.rotation ?: 0
    } else {
        val windowManager = this?.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        windowManager?.defaultDisplay?.rotation ?: 0
    }
