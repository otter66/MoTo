package com.otter66.newMoTo.Util

import android.app.Activity
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.set
import androidx.core.text.toSpannable
import com.otter66.newMoTo.R

object TextUtil {

    fun setTextColor(activity:Activity, textView: TextView, text: String) {
        val gradientStarColor = ContextCompat.getColor(activity, R.color.app_main_color_start)
        val gradientEndColor = ContextCompat.getColor(activity, R.color.app_main_color_end)
        val spannable = text.toSpannable()
        spannable[0..text.length] = LinearGradientSpan(text, text, gradientStarColor, gradientEndColor)
        textView.text = spannable
        //참고: https://android-dev.tistory.com/m/55?category=813913
    }
}