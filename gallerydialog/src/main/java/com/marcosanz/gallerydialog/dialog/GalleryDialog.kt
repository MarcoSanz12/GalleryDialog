package com.marcosanz.gallerydialog.dialog

import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
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
    private val currentImage
        get() = _currentImage

    /**
     * Returns the list of [Image] used by the Dialog
     */
    private val imageList
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

        setStyle(STYLE_NO_TITLE, R.style.GalleryDialogTheme)

        // 1. Image list
        images = arguments?.getParcelableArrayListCompat(ARG_IMAGES, Image::class.java)?.toList()
            ?: emptyList()

        // To allow for errorDrawable to show up
        if (images.isEmpty())
            images = listOf(Image.Drawable())

        // 2. Initial image position
        initialImage = arguments?.getInt(ARG_INITIAL_IMAGE) ?: 0

        // 3. Options
        options = arguments?.getParcelableCompat(ARG_OPTIONS, GalleryDialogOptions::class.java)
            ?: GalleryDialogOptions()


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
        binding = DlgGalleryBinding.inflate(inflater, container, false)

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

        binding.btMore.isVisible =
            options.isDownloadEnabled || !options.fileProviderAuthorities.isNullOrEmpty()

        // Button more / expanded options
        binding.btMore.setOnClickListener { v ->
            showMenu(v, R.menu.popop_menu)
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
        else
            invisibleFooter()


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
        val wrapperContext: Context = ContextThemeWrapper(v.context, R.style.GalleryPopupTheme)
        val popup = PopupMenu(wrapperContext, v)

        popup.menuInflater.inflate(menuRes, popup.menu)

        if (options.fileProviderAuthorities.isNullOrEmpty())
            popup.menu.removeItem(R.id.menu_share)
        if (!options.isDownloadEnabled)
            popup.menu.removeItem(R.id.menu_save)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_share -> onClickShare()
                R.id.menu_save -> onClickSave()
            }

            true
        }
        popup.show()
    }

    private fun onClickShare() {
        context?.loadDrawable(
            images[binding.viewpager.currentItem],
            errorDrawable = options.errorDrawable
        ) { bitmap ->
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
            context?.loadDrawable(
                images[binding.viewpager.currentItem],
                errorDrawable = options.errorDrawable
            ) { bitmap ->
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
        if (activity?.isChangingConfigurations != true)
            orientationManager.displayOrientation = initialOrientation

        super.onDestroy()
    }


    private fun invisibleFooter() =
        binding.lyFooter.invisible(true, y = 25f)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(INITIAL_ORIENTATION, initialOrientation)
        super.onSaveInstanceState(outState)
    }

    /**
     * Restores initial configs from orientation changes
     */
    private fun restoreInstanceState(inState: Bundle?) {
        // 1 Initial orientation
        inState?.getInt(INITIAL_ORIENTATION)?.let {
            initialOrientation = it
        }
    }

    override fun getTheme(): Int = R.style.GalleryDialogTheme


    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.fragments.any { it is GalleryDialog || it is Gallery360Dialog })
            return

        super.show(manager, tag)
    }

    class Builder private constructor(
        private val images: List<Image>,
        private val initialPosition: Int = 0,
    ) {
        private var fileProviderAuthorities: String? = null
        private var messageSharing: String? = null
        private var messageDownloading: String? = null
        private var messageSuccesfulDownload: String? = null
        private var messageErrorDownload: String? = null
        private var isDownloadEnabled: Boolean = true

        @DrawableRes
        private var errorDrawable: Int? = null
        private var allowRotation: Boolean = true

        companion object {
            fun createWithUrl(
                urls: List<String?>? = null,
                alts: List<String?>? = null,
                initialPosition: Int = 0
            ) =
                Builder(
                    images =
                        urls?.map {
                            val index = urls.indexOf(it)
                            Image.URL(alt = alts?.getOrNull(index), url = it)
                        } ?: emptyList(),
                    initialPosition = initialPosition
                )

            fun createWithUrl(
                url: String? = null,
                alt: String? = null
            ) = Builder(
                images = listOf(Image.URL(alt = alt, url = url))
            )

            fun createWithDrawable(
                drawables: List<Int?>? = null,
                alts: List<String?>? = null,
                initialPosition: Int = 0
            ) =
                Builder(
                    images =
                        drawables?.map {
                            val index = drawables.indexOf(it)
                            Image.Drawable(alt = alts?.getOrNull(index), drawable = it)
                        } ?: emptyList(),
                    initialPosition = initialPosition
                )

            fun createWithDrawable(
                @DrawableRes drawable: Int? = null,
                alt: String? = null
            ) = Builder(listOf(Image.Drawable(alt = alt, drawable = drawable)))


            fun createWithUri(
                uris: List<Uri?>? = null,
                alts: List<String?>? = null,
                initialPosition: Int = 0
            ) = Builder(
                images =
                    uris?.map {
                        val index = uris.indexOf(it)
                        Image.URI(alt = alts?.getOrNull(index), uri = it)
                    } ?: emptyList(),
                initialPosition = initialPosition
            )

            fun createWithUri(
                uri: Uri? = null,
                alt: String? = null
            ) = Builder(listOf(Image.URI(alt = alt, uri = uri)))

        }

        /**
         * Sets the fileProviderAuthorities for the [GalleryDialog] to allow sharing images
         */
        fun setFileProviderAuthorities(fileProviderAuthorities: String) = this.apply {
            this.fileProviderAuthorities = fileProviderAuthorities
        }

        /**
         * Sets the messages for the [GalleryDialog]
         */
        fun setMessages(
            messageSharing: String? = null,
            messageDownloading: String? = null,
            messageSuccesfulDownload: String? = null,
            messageErrorDownload: String? = null
        ) = this.apply {
            this.messageSharing = messageSharing
            this.messageDownloading = messageDownloading
            this.messageSuccesfulDownload = messageSuccesfulDownload
            this.messageErrorDownload = messageErrorDownload
        }

        fun setIsDownloadEnabled(isDownloadEnabled: Boolean) = this.apply {
            this.isDownloadEnabled = isDownloadEnabled
        }

        /**
         * Sets the drawable to display when loading fails
         */
        fun setErrorDrawable(@DrawableRes errorDrawable: Int) = this.apply {
            this.errorDrawable = errorDrawable
        }

        /**
         * Specifies if the rotation should be activated
         */
        fun setAllowRotation(allowRotation: Boolean) = this.apply {
            this.allowRotation = allowRotation
        }

        fun build(): GalleryDialog = newInstance(
            images = images,
            initialImage = initialPosition,
            options = GalleryDialogOptions(
                fileProviderAuthorities = fileProviderAuthorities,
                messageSharing = messageSharing,
                messageDownloading = messageDownloading,
                messageSuccessfulDownload = messageSuccesfulDownload,
                messageErrorDownload = messageErrorDownload,
                errorDrawable = errorDrawable,
                isDownloadEnabled = isDownloadEnabled,
                rotation = allowRotation
            )
        )
    }


    companion object {
        private const val TAG = "GalleryDialog"
        private const val ARG_IMAGES = "arg_images"
        private const val ARG_INITIAL_IMAGE = "arg_initial_image"
        private const val ARG_OPTIONS = "arg_options"
        private const val INITIAL_ORIENTATION = "INITIAL_ORIENTATION"
        private const val INITIAL_ACTIONBAR_COLOR = "INITIAL_ACTIONBAR_COLOR"

        /**
         * Creates a new instance of [GalleryDialog].
         *
         * @param images List of [Image] to display
         * @param initialImage Initial image position
         * @param options Additional options for the dialog configuration
         *
         * @return A new instance of [GalleryDialog]
         */
        private fun newInstance(
            images: List<Image>,
            initialImage: Int = 0,
            options: GalleryDialogOptions? = null
        ): GalleryDialog {
            val fragment = GalleryDialog()
            val args = Bundle()

            args.putParcelableArrayList(ARG_IMAGES, ArrayList(images))
            args.putInt(ARG_INITIAL_IMAGE, initialImage)
            args.putParcelable(ARG_OPTIONS, options)

            fragment.arguments = args
            return fragment
        }

    }

}
