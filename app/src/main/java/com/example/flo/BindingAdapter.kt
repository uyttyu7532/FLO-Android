package com.example.flo

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


object BindingAdapter {

    @BindingAdapter("imageUrl")
    @JvmStatic
    fun loadImage(imageView: ImageView, url: String?) {
        Glide.with(imageView.context).load(url)
            .placeholder(R.color.colorNotBlack)
            .error(R.color.colorNotBlack)
            .into(imageView)
    }


    @BindingAdapter("songTime")
    @JvmStatic
    fun mSec(view: TextView, mSec: Int) {
        val mSec = mSec.toLong()
        val hours: Long = mSec / 60 / 60 % 24
        val minutes: Long = mSec / 60 % 60
        val seconds: Long = mSec % 60

        if (mSec < 3600000) {
            view.text = String.format("%02d:%02d", minutes, seconds)
        } else {
            view.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    @BindingAdapter("highLightBold")
    @JvmStatic
    fun highLightBold(view: TextView, isHighLight : Boolean) {
        when (isHighLight) {
            true -> view.setTypeface(null, Typeface.BOLD)
            else -> view.setTypeface(null, Typeface.NORMAL)
        }
    }

    @BindingAdapter("highLightSize")
    @JvmStatic
    fun highLightSize(view: TextView, isHighLight : Boolean) {
        when (isHighLight) {
            true -> view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
            else -> view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.toFloat())
        }
    }
}