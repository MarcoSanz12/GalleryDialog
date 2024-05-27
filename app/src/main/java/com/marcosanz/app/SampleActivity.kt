package com.marcosanz.app

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import com.bumptech.glide.Glide
import com.marcosanz.app.databinding.ActivitySampleBinding
import com.marcosanz.gallerydialog.dialog.Gallery360Dialog
import com.marcosanz.gallerydialog.dialog.Gallery360DialogOptions
import com.marcosanz.gallerydialog.dialog.GalleryDialog
import com.marcosanz.gallerydialog.dialog.GalleryDialogOptions
import com.marcosanz.gallerydialog.entity.Image


class SampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleBinding

    private val images = listOf(
        Image(
            thumbnail = "https://media.licdn.com/dms/image/C4E03AQHc8TgMSagC7Q/profile-displayphoto-shrink_800_800/0/1614784623062?e=2147483647&v=beta&t=0dARt006OVNRV5uozNO_aXoxOG7Wk-BxX5cDwrWNe9g",
            url = "https://media.licdn.com/dms/image/C4E03AQHc8TgMSagC7Q/profile-displayphoto-shrink_800_800/0/1614784623062?e=2147483647&v=beta&t=0dARt006OVNRV5uozNO_aXoxOG7Wk-BxX5cDwrWNe9g",
            alt = "Famous spanish youtuber Willyrex"
        ),
        Image(
            thumbnail = "https://brobible.com/wp-content/uploads/2022/05/its-morbin-time.jpg",
            url = "https://brobible.com/wp-content/uploads/2022/05/its-morbin-time.jpg",
            alt = "Doctor Michael Morbius from the comic series Batman"
        ),
        Image(
            thumbnail = "https://avatars.githubusercontent.com/u/100760981?v=4",
            url = "https://avatars.githubusercontent.com/u/100760981?v=4",
            alt = "Danwolve98 - Danwolve98"
        ),
        Image(
            thumbnail = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQLP7J4nTJmmUmqWeIRK5QBRRmCQhzPlDxt5VaWewvQSw&s",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQLP7J4nTJmmUmqWeIRK5QBRRmCQhzPlDxt5VaWewvQSw&s",
        )
    )

    private val images360 = listOf(
        Image(
            thumbnail = "https://s.studiobinder.com/wp-content/uploads/2019/11/360-Video-Featured-StudioBinder-Compressed.jpg",
            url = "https://s.studiobinder.com/wp-content/uploads/2019/11/360-Video-Featured-StudioBinder-Compressed.jpg",
            alt = "360 city"
        ),
        Image(
            thumbnail = "https://www.antevenio.com/wp-content/uploads/2017/02/ejemplos-de-v%C3%ADdeos-360.jpg",
            url = "https://www.antevenio.com/wp-content/uploads/2017/02/ejemplos-de-v%C3%ADdeos-360.jpg",
            alt = "Panoramic example 2"
        ),
        Image(
            thumbnail = "https://media.macphun.com/img/uploads/customer/blog/2432/169339558164ef2a7d7179d5.77274305.jpg?q=85&w=1680",
            url = "https://media.macphun.com/img/uploads/customer/blog/2432/169339558164ef2a7d7179d5.77274305.jpg?q=85&w=1680",
        ),
        Image(
            thumbnail = "https://img.youtube.com/vi/VaZcXQ8Th7s/maxresdefault.jpg",
            url = "https://img.youtube.com/vi/VaZcXQ8Th7s/maxresdefault.jpg",
        )
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startPostponedEnterTransition()
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.doOnPreDraw {
            startPostponedEnterTransition()
        }

        binding.ivImage1.run {
            loadFromUrl(images[0].thumbnail)
            setGalleryDialogOnClick(0)
        }
        binding.ivImage2.run {
            loadFromUrl(images[1].thumbnail)
            setGalleryDialogOnClick(1)
        }
        binding.ivImage3.run {
            loadFromUrl(images[2].thumbnail)
            setGalleryDialogOnClick(2)
        }
        binding.ivImage4.run {
            loadFromUrl(images[3].thumbnail)
            setGalleryDialogOnClick(3)
        }

        binding.ivImage3601.run {
            loadFromUrl(images360[0].thumbnail)
            setGallery360DialogOnClick(0)
        }
        binding.ivImage3602.run {
            loadFromUrl(images360[1].thumbnail)
            setGallery360DialogOnClick(1)
        }
        binding.ivImage3603.run {
            loadFromUrl(images360[2].thumbnail)
            setGallery360DialogOnClick(2)
        }
        binding.ivImage3604.run {
            loadFromUrl(images360[3].thumbnail)
            setGallery360DialogOnClick(3)
        }

    }

    private fun ImageView.loadFromUrl(url: String?) = Glide.with(this).load(url).into(this)

    private fun ImageView.setGalleryDialogOnClick(position: Int) = setOnClickListener {
        GalleryDialog.newInstance(
            images,
            position,
            GalleryDialogOptions(
                fileProviderAuthorities = "com.marcosanz.app",
                errorDrawable = R.drawable.ic_launcher_foreground
            )
        ).show(supportFragmentManager, "gallery_dialog")
    }

    private fun ImageView.setGallery360DialogOnClick(position: Int) = setOnClickListener {
        Gallery360Dialog.newInstance(
            images360[position],
            Gallery360DialogOptions()
        ).show(supportFragmentManager, "gallery_dialog")
    }
}