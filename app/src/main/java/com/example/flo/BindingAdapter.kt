package com.example.flo

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object BindingAdapter {

    @BindingAdapter("imageUrl")
    @JvmStatic
    fun loadImage(imageView: ImageView, url: String?) {
        Glide.with(imageView.context).load(url)
            .placeholder(R.color.cardview_light_background)
            .error(R.color.cardview_light_background)
            .into(imageView)
    }


    @BindingAdapter("songTime")
    @JvmStatic
    fun mSec(view: TextView, mSec: Int) {
        val mSec = mSec*1000.toLong()
        val hours: Long = mSec / 1000 / 60 / 60 % 24
        val minutes: Long = mSec / 1000 / 60 % 60
        val seconds: Long = mSec / 1000 % 60

        if (mSec < 3600000) {
            view.text = String.format("%02d:%02d", minutes, seconds)
        } else {
            view.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

    }
}