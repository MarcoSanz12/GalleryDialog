package com.marcosanz.app

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import com.bumptech.glide.Glide
import com.marcosanz.app.databinding.ActivitySampleBinding
import com.marcosanz.gallerydialog.dialog.GalleryDialog
import com.marcosanz.gallerydialog.dialog.GalleryDialogOptions
import com.marcosanz.gallerydialog.entity.Image


class SampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleBinding

    private val images = listOf(
        Image(
            thumbnail = "https://i.pinimg.com/474x/83/39/27/833927616d89898d489796340f122d84.jpg",
            url = "https://i.pinimg.com/474x/83/39/27/833927616d89898d489796340f122d84.jpg",
            alt = "Famous spanish cantautor Guillermo Ibañez during his tour over europe\naaaaaaaaaaaaaaa\naaaaaaaaaaaaa"
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

    }

    private fun ImageView.loadFromUrl(url: String?) = Glide.with(this).load(url).into(this)

    private fun ImageView.setGalleryDialogOnClick(position: Int) = setOnClickListener {
        GalleryDialog.newInstance(
            images,
            position,
            GalleryDialogOptions(fileProviderAuthorities = "com.marcosanz.app")
        ).show(supportFragmentManager, "gallery_dialog")
    }
}