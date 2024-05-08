package com.marcosanz.gallerydialog.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.GestureDetector.OnDoubleTapListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marcosanz.gallerydialog.R
import com.marcosanz.gallerydialog.databinding.ItemGalleryBinding
import com.marcosanz.gallerydialog.entity.Image
import com.marcosanz.gallerydialog.extension.loadFromUrl


internal class GalleryDialogAdapter(
    val items: List<Image>,
    val onSingleTap: () -> Unit,
    val onDoubleTap: (isExpanding: Boolean) -> Unit,
    val errorBitmap: Bitmap?
) : RecyclerView.Adapter<GalleryDialogAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "GalleryDialogAdapter"
    }

    val loadedDrawables = mutableMapOf<Int, Drawable>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_gallery, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.resetZoom()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.bind(i)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(position: Int) {
            with(ItemGalleryBinding.bind(itemView)) {
                //tvDebug.text = position.toString()
                // 1. Imágen
                with(ivImagen) {
                    val image = items[position]
                    doubleTapScale = 2f
                    maxZoom = 5f
                    isSuperZoomEnabled = true
                    Log.d(TAG, "Bind url -> ${image.url}")
                    loadFromUrl(url = image.url, defaultBitmap = errorBitmap) { drw ->
                        if (drw != null)
                            loadedDrawables[position] = drw
                    }
                    setOnDoubleTapListener(object : OnDoubleTapListener {
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            onSingleTap()
                            return true
                        }

                        override fun onDoubleTap(e: MotionEvent): Boolean {
                            onDoubleTap(!isZoomed)
                            return true
                        }

                        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                            return true
                        }

                    })
                    setOnTouchListener { v, event ->
                        var result = true
                        // Only scroll Viewpager if not zoomed
                        if (event.pointerCount >= 2 || (v.canScrollHorizontally(-1) && v.canScrollHorizontally(
                                1
                            ))
                        ) {
                            //multi-touch event
                            result = when (event.action) {
                                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                    // Disallow RecyclerView to intercept touch events.
                                    itemView.parent.requestDisallowInterceptTouchEvent(true)
                                    // Disable touch on view
                                    false
                                }

                                MotionEvent.ACTION_UP -> {
                                    // Allow RecyclerView to intercept touch events.
                                    itemView.parent.requestDisallowInterceptTouchEvent(false)
                                    true
                                }

                                else -> true
                            }
                        }
                        result
                    }
                }
            }
        }

        fun resetZoom() {
            with(ItemGalleryBinding.bind(itemView)) {
                if (ivImagen.isZoomed)
                    ivImagen.resetZoom()
            }
        }
    }
}