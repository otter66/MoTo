package com.otter66.newMoTo.Fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.otter66.newMoTo.R
import com.otter66.newMoTo.Data.Post
import com.smarteist.autoimageslider.SliderView
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.otter66.newMoTo.Adapter.PostSliderAdapter


class PostFragment: Fragment() {

    private lateinit var postInformation: Post
    private lateinit var publisherProfileImage: String
    private lateinit var bundle: Bundle
    private lateinit var postSliderAdapter: PostSliderAdapter
    private lateinit var imagesList: MutableList<String>

    private lateinit var postPublisherProfileImageView: ImageView
    private lateinit var postPublisherIdTextView: TextView
    private lateinit var postTitleTextView: TextView
    private lateinit var postTwoLineDescriptionTextView: TextView
    private lateinit var postImagesSliderView: SliderView
    private lateinit var postDescriptionTextView: TextView
    private lateinit var postUpdateNoteTextView: TextView
    private lateinit var postImprovementTextView: TextView
    private lateinit var postLinksContainer: LinearLayout
    private lateinit var postCreatedDateTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_post, container, false) as ViewGroup

        imagesList = mutableListOf()
        bundle = requireArguments()
        postInformation = bundle.getSerializable("postInfo") as Post
        publisherProfileImage = bundle.getSerializable("publisherProfileImage").toString()

        viewInit(rootView)

        postSliderAdapter = PostSliderAdapter(activity as Activity, imagesList)

        setPostInformation()

        return rootView
    }

    private fun setPostInformation() {
        Glide.with(activity as Activity).load(publisherProfileImage ?: R.drawable.sample_image)
            .override(200, 200).circleCrop().into(postPublisherProfileImageView)
        postPublisherIdTextView.text = postInformation.publisher
        postTitleTextView.text = postInformation.title
        postTwoLineDescriptionTextView.text = postInformation.twoLineDescription
        postImagesSliderView.setSliderAdapter(postSliderAdapter)
        if(postInformation.images != null) {
            for(i in 0 until postInformation.images!!.size) {
                imagesList.add(postInformation.images!![i]!!)
            }
        }
        Log.d("test_log", "imagesList: ${imagesList}")
        postSliderAdapter.notifyDataSetChanged()
        postDescriptionTextView.text = postInformation.description
        postUpdateNoteTextView.text = postInformation.update
        postImprovementTextView.text = postInformation.improvement
        createLinkBundle()
        postCreatedDateTextView.text =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(postInformation.createdDate)

    }

    private fun createLinkBundle() {
        if (postInformation.linkNames != null) {
            for (i in 0 until postInformation.linkNames!!.size) {
                try {
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 15, 0, 15)

                    val textView = TextView(activity)
                    textView.text = postInformation.linkNames!![i]
                    textView.layoutParams = params
                    textView.setTextColor(ContextCompat.getColor(activity as Activity, R.color.blue))
                    textView.textSize = 15f
                    textView.typeface =
                        ResourcesCompat.getFont(activity as Activity, R.font.nanum_myeongjo)

                    textView.setOnClickListener {
                        val webIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(postInformation.linkAddresses?.get(i).toString())
                        )
                        startActivity(webIntent)
                    }

                    postLinksContainer.addView(textView)
                } catch (e: Exception) {
                    Toast.makeText(activity, "브라우저를 찾지 못하였습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun viewInit(rootView: ViewGroup) {
        postPublisherProfileImageView = rootView.findViewById(R.id.postPublisherProfileImageView)
        postPublisherIdTextView = rootView.findViewById(R.id.postPublisherIdTextView)
        postTitleTextView = rootView.findViewById(R.id.postTitleTextView)
        postTwoLineDescriptionTextView = rootView.findViewById(R.id.postTwoLineDescriptionTextView)
        postImagesSliderView = rootView.findViewById(R.id.postImagesSlider)
        postDescriptionTextView = rootView.findViewById(R.id.postDescriptionTextView)
        postUpdateNoteTextView = rootView.findViewById(R.id.postUpdateNoteTextView)
        postImprovementTextView = rootView.findViewById(R.id.postImprovementTextView)
        postLinksContainer = rootView.findViewById(R.id.postLinksContainer)
        postCreatedDateTextView = rootView.findViewById(R.id.postCreatedDateTextView)
    }
}