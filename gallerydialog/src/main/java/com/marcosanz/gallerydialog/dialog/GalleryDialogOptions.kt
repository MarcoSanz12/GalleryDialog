package com.marcosanz.gallerydialog.dialog

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class GalleryDialogOptions(

    // FileProvider authorities
    /**
     * The authority of a FileProvider, necessary to share a File
     * You can define it like this:
     *
     * Create in your main module res/xml/paths.xml
     * ```
     * <paths>
     *     <cache-path
     *         name="shared_images"
     *         path="images/"/>
     * </paths>
     *```
     * Then declare in your main module AndroidManifest.xml
     * ```
     * <provider
     *   android:name="androidx.core.content.FileProvider"
     *   android:authorities="com.marcosanz.gallerydialog" // Here use your module location
     *   android:exported="false"
     *   android:grantUriPermissions="true">
     *      <meta-data
     *          android:name="android.support.FILE_PROVIDER_PATHS"
     *          android:resource="@xml/paths" />
     * </provider>
     *
     *  ```
     */
    val fileProviderAuthorities: String? = null,

    // MESSAGES

    /** Text shown in the Intent sharing chooser */
    val messageSharing: String? = null,

    /** Text shown when image download begins */
    val messageDownloading: String? = null,

    /** Text shown when image is downloaded successfully */
    val messageSuccessfulDownload: String? = null,

    /**
     * Text shown when image download fails for any reason
     */
    val messageErrorDownload: String? = null,

    /**
     * Image resource shown if the image loading fails
     */
    @DrawableRes
    val errorDrawable: Int? = null,

    /**
     * If true, the dialog will allow orientation change and a rotation button will be shown
     */
    val rotation: Boolean = true
) : Parcelable