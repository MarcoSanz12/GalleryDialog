package com.marcosanz.gallerydialog.dialog

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import com.marcosanz.gallerydialog.R
import com.marcosanz.gallerydialog.databinding.DlgGallery360Binding
import com.marcosanz.gallerydialog.entity.Image
import com.marcosanz.gallerydialog.extension.getBitmapFromUrl
import com.marcosanz.gallerydialog.extension.getParcelableCompat
import com.marcosanz.gallerydialog.extension.getSerializableCompat
import com.marcosanz.gallerydialog.extension.getUIDeviceOrientation
import com.marcosanz.gallerydialog.extension.invisible
import com.marcosanz.gallerydialog.extension.isNotNullOrEmpty
import com.marcosanz.gallerydialog.extension.notNull
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

    private var initialActionbarColor: Int? = null
    private var initialOrientation: Int
    private var oldUIDeviceOrientation: UIDeviceOrientation
    private var hasRotationChanged = false

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

        initialOrientation =
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        oldUIDeviceOrientation =
            activity.getUIDeviceOrientation() ?: UIDeviceOrientation.UIDeviceOrientationPortrait

        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // 1. Initial screen orientation
        outState.putInt(INITIAL_ORIENTATION, initialOrientation)

        // 2. Last UIOrientation (for sensorial rotation)
        outState.putSerializable(OLD_UI_ORIENTATION, oldUIDeviceOrientation)

        // 3. Initial actionbar color
        initialActionbarColor.notNull {
            outState.putInt(INITIAL_ACTIONBAR_COLOR, it)
        }

        super.onSaveInstanceState(outState)
    }

    /**
     * Restores initial configs from orientation changes
     */
    private fun restoreInstanceState(inState: Bundle?) {
        // 1. Actionbar color
        initialActionbarColor =
            inState?.getInt(INITIAL_ACTIONBAR_COLOR) ?: activity?.window?.statusBarColor

        // 2. Orientation
        initialOrientation = inState?.getInt(INITIAL_ORIENTATION) ?: initialOrientation

        // 3. UI Orientation
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
        image = arguments.getParcelableCompat(ARG_IMAGE, Image::class.java) ?: Image()

        orientationManager = OrientationManager(requireActivity())
        restoreInstanceState(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            it.window?.let { wdw ->
                wdw.requestFeature(Window.FEATURE_NO_TITLE)
                wdw.setBackgroundDrawable(ColorDrawable(Color.BLACK))
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
        binding = DlgGallery360Binding.inflate(inflater, null, false)
        getBitmapFromUrl(requireContext(), image.url, ::onPanoramaLoaded, ::onPanoramaError)
        // I. Config max alt lines
        binding.tvText.maxLines = maxAltLines

        if (image.alt.isNotNullOrEmpty()) {
            visibleFooter()
            binding.tvText.text = image.alt
        } else
            invisibleFooter()

        return binding.root
    }

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

            if (options.sensorialRotation) {
                val newUIDeviceOrientation = activity.getUIDeviceOrientation()
                newUIDeviceOrientation.notNull {
                    //updateGyroscopeRotationByOrientation(oldUIDeviceOrientation, it)
                }
                Log.i("DEVICE_ORIENTATION", "PanoramaGL -> $currentDeviceOrientation")
                Log.i("DEVICE_ORIENTATION", "Old -> $oldUIDeviceOrientation\n New -> $newUIDeviceOrientation")
                startSensorialRotation()
                oldUIDeviceOrientation = newUIDeviceOrientation ?: oldUIDeviceOrientation
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
            options.errorMessage ?: getString(R.string.saving_error),
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
                    left = windowInsets.displayCutout?.safeInsetLeft ?: 0,
                    right = windowInsets.displayCutout?.safeInsetRight ?: 0
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
        hasRotationChanged = true
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

    override fun onDestroy() {
        if (initialActionbarColor != null)
            activity?.window?.statusBarColor = initialActionbarColor!!
        plManager?.onDestroy()
        if (!hasRotationChanged) {
            orientationManager.displayOrientation = initialOrientation
        }
        super.onDestroy()
    }

    companion object {
        private const val TAG = "GalleryDialog"
        private const val ARG_IMAGE = "arg_image"
        private const val ARG_OPTIONS = "arg_options"
        private const val INITIAL_ORIENTATION = "INITIAL_ORIENTATION"
        private const val OLD_UI_ORIENTATION = "INITIAL_UI_ORIENTATION"
        private const val INITIAL_ACTIONBAR_COLOR = "INITIAL_ACTIONBAR_COLOR"
        fun newInstance(
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
