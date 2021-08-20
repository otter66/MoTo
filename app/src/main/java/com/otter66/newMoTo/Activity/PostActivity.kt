package com.otter66.newMoTo.Activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.otter66.newMoTo.Adapter.PostSliderAdapter
import com.otter66.newMoTo.Data.Post
import com.otter66.newMoTo.Data.User
import com.otter66.newMoTo.R
import com.smarteist.autoimageslider.SliderView
import java.text.SimpleDateFormat
import java.util.*

class PostActivity: AppCompatActivity() {

    private lateinit var postInformation: Post
    private lateinit var publisherProfileImage: String
    private var currentUserInfo: User? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)


        imagesList = mutableListOf()
        postInformation = intent.getSerializableExtra("postInfo") as Post
        publisherProfileImage = intent.getStringExtra("publisherProfileImage").toString()
        currentUserInfo = intent.getSerializableExtra("currentUserInfo") as User?
        //todo 현재 유저의 정보와 글 퍼블리셔가 같으면 슬 수정, 삭제 가능하게

        viewInit()

        postSliderAdapter = PostSliderAdapter(this@PostActivity, imagesList)

        setPostInformation()

        }

    private fun setPostInformation() {
        Glide.with(this@PostActivity).load(publisherProfileImage ?: R.drawable.sample_image)
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
        //todo image의 형식인지 체크해주면 좋을 듯
        if(imagesList.size > 0 && imagesList[0] != "") postImagesSliderView.visibility = View.VISIBLE
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

                    val textView = TextView(this@PostActivity)
                    textView.text = postInformation.linkNames!![i]
                    textView.layoutParams = params
                    textView.setTextColor(ContextCompat.getColor(this@PostActivity, R.color.blue))
                    textView.textSize = 15f
                    textView.typeface =
                        ResourcesCompat.getFont(this@PostActivity, R.font.nanum_myeongjo)

                    textView.setOnClickListener {
                        val webIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(postInformation.linkAddresses?.get(i).toString())
                        )
                        startActivity(webIntent)
                    }

                    postLinksContainer.addView(textView)
                } catch (e: Exception) {
                    Toast.makeText(this@PostActivity, "브라우저를 찾지 못하였습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun viewInit() {
        postPublisherProfileImageView = findViewById(R.id.postPublisherProfileImageView)
        postPublisherIdTextView = findViewById(R.id.postPublisherIdTextView)
        postTitleTextView = findViewById(R.id.postTitleTextView)
        postTwoLineDescriptionTextView = findViewById(R.id.postTwoLineDescriptionTextView)
        postImagesSliderView = findViewById(R.id.postImagesSlider)
        postDescriptionTextView = findViewById(R.id.postDescriptionTextView)
        postUpdateNoteTextView = findViewById(R.id.postUpdateNoteTextView)
        postImprovementTextView = findViewById(R.id.postImprovementTextView)
        postLinksContainer = findViewById(R.id.postLinksContainer)
        postCreatedDateTextView = findViewById(R.id.postCreatedDateTextView)
    }

}