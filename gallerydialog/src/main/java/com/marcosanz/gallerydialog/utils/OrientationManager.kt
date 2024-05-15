package com.marcosanz.gallerydialog.utils

import android.app.Activity
import android.content.pm.ActivityInfo

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

    /**
     * Sets the [activity] orientation to [ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE]
     */
    fun setOrientationLandscape() {
        displayOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    /**
     * Sets the [activity] orientation to [ActivityInfo.SCREEN_ORIENTATION_PORTRAIT]
     */
    fun setOrientationPortrait() {
        displayOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * Toggles the [activity] orientation between [ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE] and [ActivityInfo.SCREEN_ORIENTATION_PORTRAIT]
     */
    fun toggleOrientation() {
        if (displayOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setOrientationLandscape()
        else
            setOrientationPortrait()

    }
}