package com.otter66.newMoTo.Adapter

import com.otter66.newMoTo.R
import android.content.Context
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.otter66.newMoTo.Activity.WritePostActivity
import com.smarteist.autoimageslider.SliderViewAdapter

class PostSliderAdapter(private val context: Context, private val mSliderItems: MutableList<String>) :
    SliderViewAdapter<PostSliderAdapter.SliderAdapterVH>() {

    inner class SliderAdapterVH(itemView: View) : ViewHolder(itemView) {
        val imageViewBackground: ImageView = itemView.findViewById(R.id.iv_auto_image_slider)
    }

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider_layout, parent, false)
        Log.d("test_log", "images (in Adapter): $mSliderItems")

        return SliderAdapterVH(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        val sliderItem: String = mSliderItems[position]
        Glide.with(viewHolder.itemView)
            .load(sliderItem)
            .apply(RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
            .fitCenter()
            .override(2000)
            .thumbnail(0.1f)
            .into(viewHolder.imageViewBackground)
    }

    override fun getCount(): Int {
        return mSliderItems.size
    }
}

//https://github.com/smarteist/Android-Image-Slider