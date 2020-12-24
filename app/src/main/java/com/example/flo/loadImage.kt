package com.example.flo

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


@BindingAdapter("imageUrl")
fun loadImage(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url)
        .placeholder(R.color.cardview_dark_background)
        .error(R.drawable.ic_baseline_close_24)
        .into(imageView)
}
