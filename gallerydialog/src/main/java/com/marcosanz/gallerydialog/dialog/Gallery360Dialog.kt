package com.marcosanz.gallerydialog.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.marcosanz.gallerydialog.R
import com.marcosanz.gallerydialog.databinding.DlgGallery360Binding
import com.marcosanz.gallerydialog.entity.Image
import com.marcosanz.gallerydialog.extension.getBitmapWithGlide
import com.marcosanz.gallerydialog.extension.getParcelableCompat
import com.marcosanz.gallerydialog.extension.getSerializableCompat
import com.marcosanz.gallerydialog.extension.getUIDeviceOrientation
import com.marcosanz.gallerydialog.extension.invisible
import com.marcosanz.gallerydialog.extension.isNotNullOrEmpty
import com.marcosanz.gallerydialog.extension.visible
import com.marcosanz.gallerydialog.utils.OrientationManager
import com.panoramagl.PLImage
import com.panoramagl.PLManager
import com.panoramagl.PLSphericalPanorama
import com.panoramagl.ios.enumerations.UIDeviceOrientation
import java.util.concurrent.TimeUnit


class Gallery360Dialog() : DialogFragment() {

    /**
     * Indicates if ALT text should be shown
     */
    var showAltText: Boolean = true

    /**
     * Indicates the android:maxLines number of the ALT TextView
     */
    var maxAltLines: Int = 3

    /**
     * Returns the visibility status of the UI (System Bars, alt text, back button, additional buttons)
     */
    var isUIVisible
        get() = _isUIVisible
        set(value) {
            if (value)
                showUI()
            else
                hideUI()
        }

    private var plManager: PLManager? = null


    private lateinit var image: Image

    private lateinit var binding: DlgGallery360Binding

    private var _isUIVisible: Boolean = true

    private var isHeaderConsumed = false
    private var isFooterConsumed = false

    private lateinit var orientationManager: OrientationManager
    private lateinit var options: Gallery360DialogOptions

    private var initialOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    private var oldUIDeviceOrientation: UIDeviceOrientation

    private val windowInsetsController: WindowInsetsControllerCompat?
        get() =
            if (dialog?.window != null)
                WindowCompat.getInsetsController(dialog!!.window!!, dialog!!.window!!.decorView)
                    .apply {
                        systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
            else
                null

    init {
        isCancelable = true

        oldUIDeviceOrientation =
            activity.getUIDeviceOrientation() ?: UIDeviceOrientation.UIDeviceOrientationPortrait

        dialog?.setCanceledOnTouchOutside(false)
    }


    override fun getTheme(): Int = R.style.GalleryDialogTheme

    override fun onSaveInstanceState(outState: Bundle) {
        // 1. Initial screen orientation
        outState.putInt(INITIAL_ORIENTATION, initialOrientation)

        // 2. Last UIOrientation (for sensorial rotation)
        outState.putSerializable(OLD_UI_ORIENTATION, oldUIDeviceOrientation)

        super.onSaveInstanceState(outState)
    }

    /**
     * Restores initial configs from orientation changes
     */
    private fun restoreInstanceState(inState: Bundle?) {
        // 1. Orientation
        initialOrientation = inState?.getInt(INITIAL_ORIENTATION) ?: initialOrientation

        // 2. UI Orientation
        oldUIDeviceOrientation =
            inState?.getSerializableCompat(OLD_UI_ORIENTATION, UIDeviceOrientation::class.java)
                ?: oldUIDeviceOrientation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition(1000L, TimeUnit.MILLISECONDS)

        setStyle(STYLE_NORMAL, R.style.GalleryDialogTheme)

        // 1. Options
        options = arguments?.getParcelableCompat(
            ARG_OPTIONS,
            Gallery360DialogOptions::class.java
        ) ?: Gallery360DialogOptions()

        // 2. Image
        image = arguments?.getParcelableCompat(ARG_IMAGE, Image::class.java) ?: Image.URL("", "")

        orientationManager = OrientationManager(requireActivity())
        if (savedInstanceState == null)
            initialOrientation = orientationManager.displayOrientation
        restoreInstanceState(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            it.window?.let { wdw ->
                wdw.requestFeature(Window.FEATURE_NO_TITLE)
                wdw.setBackgroundDrawable(Color.BLACK.toDrawable())
                wdw.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                wdw.setWindowAnimations(R.style.DialogAnimation)
                WindowCompat.setDecorFitsSystemWindows(wdw, false)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val errorDrawable = if (options.errorDrawable != null)
            AppCompatResources.getDrawable(requireContext(), options.errorDrawable!!)
        else
            null
        binding = DlgGallery360Binding.inflate(inflater, null, false)
        image.getBitmapWithGlide(
            requireContext(),
            errorDrawable,
            ::onPanoramaLoaded,
            ::onPanoramaError
        )
        // I. Config max alt lines
        binding.tvText.maxLines = maxAltLines

        if (image.alt.isNotNullOrEmpty()) {
            visibleFooter()
            binding.tvText.text = image.alt
        } else
            invisibleFooter()

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onPanoramaLoaded(bitmap: Bitmap) {
        plManager = PLManager(requireContext())
        plManager?.apply {
            setContentView(binding.vrContent)
            onCreate()
            val panorama = PLSphericalPanorama()
            panorama.setImage(PLImage(bitmap))
            this.panorama = panorama

            isAcceleratedTouchScrollingEnabled = true
            activateOrientation()

            // El botón esta invisible por defecto
            binding.btSensorialRotation.apply {
                isChecked = options.sensorialRotation
                isVisible = options.sensorialRotation
            }
            if (options.sensorialRotation) {
                val newUIDeviceOrientation = activity.getUIDeviceOrientation()
                startSensorialRotation()
                oldUIDeviceOrientation = newUIDeviceOrientation ?: oldUIDeviceOrientation

                binding.btSensorialRotation.addOnCheckedChangeListener { materialButton, isChecked ->
                    if (isChecked)
                        startSensorialRotation()
                    else
                        stopSensorialRotation()
                }
            }
        }

        val gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    onSingleTap()
                    return true
                }
            })
        binding.vrContent.setOnTouchListener { v, event ->
            plManager?.onTouchEvent(event)

            gestureDetector.onTouchEvent(event)

            true
        }

        binding.vrContent.setOnClickListener {
            onSingleTap()
        }
    }

    private fun onPanoramaError() {
        startPostponedEnterTransition()
        Toast.makeText(
            requireContext(),
            options.errorMessage ?: getString(R.string.loading_error),
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition(1000L, TimeUnit.MILLISECONDS)

        ViewCompat.setOnApplyWindowInsetsListener(binding.lyHeader) { v, windowInsets ->
            if (!isHeaderConsumed) {
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(
                    top = insets.top,
                    left = windowInsets.displayCutout?.safeInsetLeft?.plus(insets.left) ?: 0,
                    right = windowInsets.displayCutout?.safeInsetRight?.plus(insets.right) ?: 0
                )

                isHeaderConsumed = true
            }
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.lyFooter) { v, windowInsets ->
            if (!isFooterConsumed) {
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(bottom = insets.bottom)
                isFooterConsumed = true
            }
            WindowInsetsCompat.CONSUMED
        }

        // Button back
        binding.btBack.setOnClickListener {
            dismiss()
        }

        // Button rotation
        binding.btRotate.run {
            isVisible = options.rotation
            setOnClickListener {
                onRotateClick()
            }
        }

        startPostponedEnterTransition()
    }

    private fun onRotateClick() {
        orientationManager.toggleOrientation()
    }

    private fun onSingleTap() {
        toggleUI()
    }

    private fun onDoubleTap(isExpanding: Boolean) {
        if (isExpanding)
            hideUI()
    }

    /**
     * Toggles the UI Visibility
     */
    private fun toggleUI() {
        if (_isUIVisible)
            hideUI()
        else
            showUI()
    }

    override fun onResume() {
        super.onResume()
        plManager?.onResume()

    }

    override fun onPause() {
        plManager?.onPause()
        super.onPause()
    }

    /**
     * Shows the Window Sytem UI and Footers
     */
    private fun showUI() {
        _isUIVisible = true
        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
        visibleHeader()
        if (image.alt.isNotNullOrEmpty())
            visibleFooter()
    }

    /**
     * Hides the Window Sytem UI and Footers
     */
    private fun hideUI() {
        _isUIVisible = false
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        invisibleHeader()
        invisibleFooter()

    }

    /**
     * Shows the header with an animation
     */
    private fun visibleHeader() =
        binding.lyHeader.visible(true, y = 0f)

    /**
     * Hides the header with an animation
     */
    private fun invisibleHeader() =
        binding.lyHeader.invisible(true, y = -25f)


    /** Shows the footer with an animation if [showAltText]*/
    private fun visibleFooter() {
        if (showAltText)
            binding.lyFooter.visible(true, y = 0f)
    }

    /** Hides the footer with an animation */
    private fun invisibleFooter() =
        binding.lyFooter.invisible(true, y = 25f)


    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.fragments.any { it is GalleryDialog || it is Gallery360Dialog })
            return

        super.show(manager, tag)
    }

    override fun onDestroy() {
        plManager?.onDestroy()

        if (activity?.isChangingConfigurations != true)
            orientationManager.displayOrientation = initialOrientation

        super.onDestroy()
    }

    class Builder private constructor(
        private val image: Image
    ) {
        @DrawableRes
        private var errorDrawable: Int? = null
        private var errorMessage: String? = null
        private var allowRotation: Boolean = true
        private var sensorialRotation: Boolean = true

        companion object {

            fun createWithUrl(
                url: String? = null,
                alt: String? = null
            ) = Builder(
                image = Image.URL(alt, url)
            )

            fun createWithDrawable(
                @DrawableRes drawable: Int? = null,
                alt: String? = null
            ) = Builder(Image.Drawable(alt, drawable))


            fun createWithUri(
                uri: Uri? = null,
                alt: String? = null
            ) = Builder(Image.URI(alt, uri))

        }

        /**
         * Sets the drawable to display when loading fails
         */
        fun setErrorDrawable(@DrawableRes errorDrawable: Int) = this.apply {
            this.errorDrawable = errorDrawable
        }

        /**
         * Sets the error message to display when loading fails
         */
        fun setErrorMessage(errorMessage: String) = this.apply {
            this.errorMessage = errorMessage
        }

        /**
         * Sets if the sensorial rotation should be activated
         */
        fun setSensorialRotation(sensorialRotation: Boolean) = this.apply {
            this.sensorialRotation = sensorialRotation
        }

        /**
         * Specifies if the rotation should be activated
         */
        fun setAllowRotation(allowRotation: Boolean) = this.apply {
            this.allowRotation = allowRotation
        }

        fun build(): Gallery360Dialog = newInstance(
            image = image,
            options = Gallery360DialogOptions(
                errorDrawable = errorDrawable,
                rotation = allowRotation,
                sensorialRotation = sensorialRotation,
                errorMessage = errorMessage
            )
        )
    }

    companion object {
        private const val TAG = "GalleryDialog"
        private const val ARG_IMAGE = "arg_image"
        private const val ARG_OPTIONS = "arg_options"
        private const val INITIAL_ORIENTATION = "INITIAL_ORIENTATION"
        private const val OLD_UI_ORIENTATION = "INITIAL_UI_ORIENTATION"

        /**
         * Creates a new instance of [Gallery360Dialog].
         *
         * @param image Image to display
         * @param options Additional options for the dialog configuration
         *
         * @return A new instance of [GalleryDialog]
         */
        private fun newInstance(
            image: Image,
            options: Gallery360DialogOptions? = null
        ): Gallery360Dialog {
            val fragment = Gallery360Dialog()
            val args = Bundle()

            args.putParcelable(ARG_IMAGE, image)
            args.putParcelable(ARG_OPTIONS, options)

            fragment.arguments = args
            return fragment
        }
    }
}
