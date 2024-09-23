package com.marcosanz.app

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import com.bumptech.glide.Glide
import com.marcosanz.app.databinding.ActivitySampleBinding
import com.marcosanz.app.entities.ImageDrawable
import com.marcosanz.app.entities.ImageProvider.getImagesUri
import com.marcosanz.app.entities.ImageProvider.images360Drawable
import com.marcosanz.app.entities.ImageProvider.images360Url
import com.marcosanz.app.entities.ImageProvider.imagesDrawable
import com.marcosanz.app.entities.ImageProvider.imagesUrl
import com.marcosanz.app.entities.ImageURI
import com.marcosanz.app.entities.ImageURL
import com.marcosanz.gallerydialog.dialog.Gallery360Dialog
import com.marcosanz.gallerydialog.dialog.GalleryDialog


class SampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleBinding

    val imagesUri by lazy {
        getImagesUri(
            this, "com.marcosanz.app",
            drawableRes =
            listOf(
                R.drawable.cuellar_pueblo1,
                R.drawable.cuellar_pueblo2
            )
        )
    }

    val images360Uri by lazy {
        getImagesUri(
            this, "com.marcosanz.app",
            drawableRes =
            listOf(
                R.drawable.image360_3,
                R.drawable.image360_4
            )
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        //enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        startPostponedEnterTransition()
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.doOnPreDraw {
            startPostponedEnterTransition()
        }

        prepareImages()
        prepareImages360()
    }


    private fun ImageView.loadFromUrl(url: String?) = Glide.with(this).load(url).into(this)

    private fun ImageView.loadFromUri(uri: Uri?) = Glide.with(this).load(uri).into(this)

    private fun prepareImages() =
        with(binding) {
            ivImageUrl1.run {
                loadFromUrl(imagesUrl[0].thumbnail)
                setOnClickListener {
                    showGalleryDialogURL(imagesUrl)
                }

            }
            ivImageUrl2.run {
                loadFromUrl(imagesUrl[1].thumbnail)
                setOnClickListener {
                    showGalleryDialogURL(imagesUrl, 1)
                }
            }
            ivImageDrawable1.run {
                setImageResource(imagesDrawable[0].resId!!)
                setOnClickListener {
                    showGalleryDialogDrawable(imagesDrawable)
                }
            }
            ivImageDrawable2.run {
                setImageResource(imagesDrawable[1].resId!!)
                setOnClickListener {
                    showGalleryDialogDrawable(imagesDrawable, 1)
                }
            }

            ivImageUri1.run {
                loadFromUri(imagesUri[0].thumbnail)
                setOnClickListener {
                    showGalleryDialogURI(imagesUri)
                }
            }
            ivImageUri2.run {
                loadFromUri(imagesUri[1].thumbnail)
                setOnClickListener {
                    showGalleryDialogURI(imagesUri, 1)
                }
            }
        }

    private fun prepareImages360() =
        with(binding) {
            ivImage360Url1.run {
                loadFromUrl(images360Url[0].thumbnail)
                setOnClickListener {
                    showGalleryDialog360URL(0)
                }

            }
            ivImage360Url2.run {
                loadFromUrl(images360Url[1].thumbnail)
                setOnClickListener {
                    showGalleryDialog360URL(1)
                }
            }
            ivImage360Drawable1.run {
                setImageResource(images360Drawable[0].resId!!)
                setOnClickListener {
                    showGalleryDialog360Drawable(0)
                }
            }
            ivImage360Drawable2.run {
                setImageResource(images360Drawable[1].resId!!)
                setOnClickListener {
                    showGalleryDialog360Drawable(1)
                }
            }

            ivImage360Uri1.run {
                loadFromUri(images360Uri[0].thumbnail)
                setOnClickListener {
                    showGalleryDialog360Uri(0)
                }
            }
            ivImage360Uri2.run {
                loadFromUri(images360Uri[1].thumbnail)
                setOnClickListener {
                    showGalleryDialog360Uri(1)
                }
            }
        }

    private fun showGalleryDialogURL(images: List<ImageURL>, position: Int = 0) =
        GalleryDialog.Builder
            .createWithUrl(
                urls = images.map { it.url },
                alts = images.map { it.alt },
                initialPosition = position
            )
            .setFileProviderAuthorities("com.marcosanz.app")
            .setErrorDrawable(R.drawable.cuellar1)
            .setAllowRotation(true)
            .build()
            .show(supportFragmentManager, "gallery_dialog")

    private fun showGalleryDialogDrawable(images: List<ImageDrawable>, position: Int = 0) =
        GalleryDialog.Builder
            .createWithDrawable(
                drawables = images.map { it.resId },
                alts = images.map { it.alt },
                initialPosition = position
            )
            .setFileProviderAuthorities("com.marcosanz.app")
            .setErrorDrawable(R.drawable.ic_launcher_foreground)
            .setAllowRotation(true)
            .build()
            .show(supportFragmentManager, "gallery_dialog")

    private fun showGalleryDialogURI(images: List<ImageURI>, position: Int = 0) =
        GalleryDialog.Builder
            .createWithUri(
                uris = images.map { it.uri },
                alts = images.map { it.alt },
                initialPosition = position
            )
            .setFileProviderAuthorities("com.marcosanz.app")
            .setErrorDrawable(R.drawable.ic_launcher_foreground)
            .setAllowRotation(false)
            .build()
            .show(supportFragmentManager, "gallery_dialog")

    private fun showGalleryDialog360URL(position: Int) =
        Gallery360Dialog.Builder
            .createWithUrl(
                /*url = images360Url[position].url,
                alt = images360Url[position].alt*/
            )
            .setErrorMessage("Error loading the image")
            .setErrorDrawable(R.drawable.cuellar1)
            .setAllowRotation(false)
            .setSensorialRotation(false)
            .build()
            .show(supportFragmentManager, "gallery_dialog")

    private fun showGalleryDialog360Drawable(position: Int) =
        Gallery360Dialog.Builder
            .createWithDrawable(
                drawable = images360Drawable[position].resId,
                alt = images360Drawable[position].alt
            )
            .setErrorMessage("Error loading the image")
            .setErrorDrawable(R.drawable.cuellar1)
            .setAllowRotation(true)
            .setSensorialRotation(true)
            .build()
            .show(supportFragmentManager, "gallery_dialog")

    private fun showGalleryDialog360Uri(position: Int) =
        Gallery360Dialog.Builder
            .createWithUri(
                uri = images360Uri[position].uri,
                alt = images360Uri[position].alt
            )
            .setErrorMessage("Error loading the image")
            .setErrorDrawable(R.drawable.cuellar1)
            .setAllowRotation(true)
            .setSensorialRotation(true)
            .build()
            .show(supportFragmentManager, "gallery_dialog")
}