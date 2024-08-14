package com.marcosanz.gallerydialog.utils

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.Surface
import android.view.WindowManager
import com.marcosanz.gallerydialog.extension.getDisplayRotation

internal class OrientationManager(
    private val activity: Activity
) {

    /**
     * Gets the current [activity] orientation /
     * Sets the [activity] orientation to the specified value
     */
    var displayOrientation: Int
        get() =
            activity.requestedOrientation
        set(value) {
            activity.requestedOrientation = value
        }

    private val deviceOrientation: Int
        get() = activity.resources.configuration.orientation

    /**
     * Sets the [activity] orientation to [ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE]
     */
    fun setOrientationLandscape() {
        displayOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

    }

    /**
     * Sets the [activity] orientation to [ActivityInfo.SCREEN_ORIENTATION_PORTRAIT]
     */
    fun setOrientationPortrait() {
        displayOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    /**
     * Toggles the [activity] orientation between [ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE] and [ActivityInfo.SCREEN_ORIENTATION_PORTRAIT]
     */
    fun toggleOrientation() {
        if (deviceOrientation == Configuration.ORIENTATION_PORTRAIT)
            setOrientationLandscape()
        else
            setOrientationPortrait()

    }
}