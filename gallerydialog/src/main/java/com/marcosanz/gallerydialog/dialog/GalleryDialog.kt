package com.marcosanz.gallerydialog.dialog

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.marcosanz.gallerydialog.R
import com.marcosanz.gallerydialog.adapter.GalleryDialogAdapter
import com.marcosanz.gallerydialog.databinding.DlgGalleryBinding
import com.marcosanz.gallerydialog.entity.Image
import com.marcosanz.gallerydialog.extension.animate
import com.marcosanz.gallerydialog.extension.getParcelableArrayListCompat
import com.marcosanz.gallerydialog.extension.getParcelableCompat
import com.marcosanz.gallerydialog.extension.invisible
import com.marcosanz.gallerydialog.extension.isNotNullOrEmpty
import com.marcosanz.gallerydialog.extension.loadDrawable
import com.marcosanz.gallerydialog.extension.saveBitmapToPictures
import com.marcosanz.gallerydialog.extension.shareImage
import com.marcosanz.gallerydialog.extension.toBitmap
import com.marcosanz.gallerydialog.extension.visible
import com.marcosanz.gallerydialog.utils.OrientationManager
import java.util.concurrent.TimeUnit

class GalleryDialog() : DialogFragment() {

    /**
     * Returns the currently selected [Image]
     */
    val currentImage
        get() = _currentImage

    /**
     * Returns the list of [Image] used by the Dialog
     */
    val imageList
        get() = images

    /**
     * Indicates if ALT text should be shown
     */
    var showAltText: Boolean = true

    /**
     * Indicates the android:maxLines number of the ALT TextView
     */
    var maxAltLines: Int = 3

    private lateinit var options: GalleryDialogOptions

    private var initialSelection = true

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

    private var images: List<Image> = emptyList()
    private var initialImage: Int = 0

    private lateinit var binding: DlgGalleryBinding
    private lateinit var galleryAdapter: GalleryDialogAdapter

    private var _isUIVisible: Boolean = true
    private var initialActionBarColor: Int? = null
    private var _currentImage: Image? = null

    private var isHeaderConsumed = false
    private var isFooterConsumed = false

    private lateinit var orientationManager: OrientationManager

    private var _infoToast: Toast? = null

    private var initialOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition(1000L, TimeUnit.MILLISECONDS)

        setStyle(STYLE_NORMAL, R.style.GalleryDialogTheme)

        // 1. Image list
        images = arguments.getParcelableArrayListCompat(ARG_IMAGES, Image::class.java)?.toList()
            ?: emptyList()

        // 2. Initial image position
        initialImage = arguments?.getInt(ARG_INITIAL_IMAGE) ?: 0

        // 3. Options
        options = arguments?.getParcelableCompat(ARG_OPTIONS, GalleryDialogOptions::class.java)
            ?: GalleryDialogOptions()


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
        binding = DlgGalleryBinding.inflate(inflater, null, false)

        // I. Config max alt lines
        binding.tvText.maxLines = maxAltLines
        return binding.root
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

        // Button more / expanded options
        binding.btMore.setOnClickListener { v ->
            if (options.fileProviderAuthorities != null)
                showMenu(v, R.menu.popop_menu)
            else
                showMenu(v, R.menu.popop_menu_no_share)
        }

        // Button rotation
        binding.btRotate.run {
            isVisible = options.rotation
            setOnClickListener {
                onRotateClick()
            }
        }

        val errorDrawable = if (options.errorDrawable != null)
            ResourcesCompat.getDrawable(resources, options.errorDrawable!!, null)
        else
            null
        galleryAdapter =
            GalleryDialogAdapter(images, ::onSingleTap, ::onDoubleTap, errorDrawable)

        binding.viewpager.adapter = galleryAdapter

        binding.viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                onImageChanged(images[position])
            }
        })
        binding.viewpager.setCurrentItem(initialImage, false)

        startPostponedEnterTransition()
    }

    private fun onRotateClick() {
        hasRotationChanged = true
        orientationManager.toggleOrientation()
    }


    private fun onImageChanged(image: Image) {
        _currentImage = image
        if (_isUIVisible && image.alt.isNotNullOrEmpty())
            visibleFooter()
        else {
            invisibleFooter()
        }

        updateText(image)
    }

    private fun updateText(image: Image) {
        if (image.alt.isNullOrEmpty())
            binding.lyFooter.animate(duration = 0.2f, 25f, alpha = 0f) {
                binding.tvText.text = ""
            }
        else
            binding.tvText.text = image.alt
    }

    private fun onSingleTap() {
        toggleUI()
    }

    private fun onDoubleTap(isExpanding: Boolean) {
        if (isExpanding)
            hideUI()
    }

    private fun toggleUI() {
        if (_isUIVisible)
            hideUI()
        else
            showUI()
    }

    private fun showUI() {
        _isUIVisible = true
        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
        visibleHeader()
        if (currentImage?.alt.isNotNullOrEmpty())
            visibleFooter()
    }


    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)

        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_share -> onClickShare()
                R.id.menu_save -> onClickSave()
            }

            true
        }

        popup.setOnDismissListener {
            Log.i(TAG, "MENU DISMISS")
        }
        popup.show()
    }

    private fun onClickShare() {
        context?.loadDrawable(images[binding.viewpager.currentItem].url) { bitmap ->
            val shareMsg = options.messageSharing
            bitmap?.shareImage(requireContext(), options.fileProviderAuthorities!!, shareMsg)
        }
    }

    private fun onClickSave() {
        showInfoToast(options.messageDownloading ?: getString(R.string.saving_picture))
        val savedDrawable = galleryAdapter.loadedDrawables[binding.viewpager.currentItem]

        if (savedDrawable != null)
            savedDrawable.toBitmap().saveToPictures()
        else
            context?.loadDrawable(images[binding.viewpager.currentItem].url) { bitmap ->
                bitmap.saveToPictures()
            }
    }

    private fun Bitmap?.saveToPictures() {
        if (this?.saveBitmapToPictures(requireContext()) == true)
            showInfoToast(options.messageSuccessfulDownload ?: getString(R.string.picture_saved))
        else
            showInfoToast(options.messageErrorDownload ?: getString(R.string.saving_error))


    }

    private fun showInfoToast(message: String?) {
        _infoToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        _infoToast?.show()
    }

    private fun hideUI() {
        _isUIVisible = false
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        invisibleHeader()
        invisibleFooter()

    }

    private fun visibleHeader() =
        binding.lyHeader.visible(true, y = 0f)


    private fun invisibleHeader() =
        binding.lyHeader.invisible(true, y = -25f)


    private fun visibleFooter() {
        if (showAltText)
            binding.lyFooter.visible(true, y = 0f)
    }

    override fun onDestroy() {
        if (initialActionBarColor != null)
            activity?.window?.statusBarColor = initialActionBarColor!!

        if (!hasRotationChanged)
            orientationManager.displayOrientation = initialOrientation

        super.onDestroy()
    }


    private fun invisibleFooter() =
        binding.lyFooter.invisible(true, y = 25f)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(INITIAL_ORIENTATION, initialOrientation)
        if (initialActionBarColor != null)
            outState.putInt(INITIAL_ACTIONBAR_COLOR, initialActionBarColor!!)
        super.onSaveInstanceState(outState)
    }

    /**
     * Restores initial configs from orientation changes
     */
    private fun restoreInstanceState(inState: Bundle?) {
        // 1. Actionbar color
        initialActionBarColor =
            inState?.getInt(INITIAL_ACTIONBAR_COLOR) ?: activity?.window?.statusBarColor


        // 2. Orientation
        initialOrientation = inState?.getInt(INITIAL_ORIENTATION)
            ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }


    companion object {
        private const val TAG = "GalleryDialog"
        private const val ARG_IMAGES = "arg_images"
        private const val ARG_INITIAL_IMAGE = "arg_initial_image"
        private const val ARG_OPTIONS = "arg_options"
        private const val INITIAL_ORIENTATION = "INITIAL_ORIENTATION"
        private const val INITIAL_ACTIONBAR_COLOR = "INITIAL_ACTIONBAR_COLOR"

        fun newInstance(
            images: List<Image>,
            initialImage: Int = 0,
            galleryDialogOptions: GalleryDialogOptions? = null
        ): GalleryDialog {
            val fragment = GalleryDialog()
            val args = Bundle()

            args.putParcelableArrayList(ARG_IMAGES, ArrayList(images))
            args.putInt(ARG_INITIAL_IMAGE, initialImage)
            args.putParcelable(ARG_OPTIONS, galleryDialogOptions)

            fragment.arguments = args
            return fragment
        }

    }

}
