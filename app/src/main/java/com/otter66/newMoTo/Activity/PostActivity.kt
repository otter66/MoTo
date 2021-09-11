package com.otter66.newMoTo.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
    private var storageRef: StorageReference? = null
    private var db: FirebaseFirestore? = null

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
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

        db = Firebase.firestore
        storageRef = FirebaseStorage.getInstance().reference

        viewInit()

        //todo 현재 유저의 정보와 글 퍼블리셔가 같으면 슬 수정, 삭제 가능하게

        postSliderAdapter = PostSliderAdapter(this@PostActivity, imagesList)
        setSupportActionBar(toolbar)
        setPostInformation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.post_menu, menu)
        if(currentUserInfo?.id.toString() == postInformation.publisher.toString()) {
            menu?.add(Menu.NONE, Menu.FIRST + 0, Menu.NONE, "수정")
            menu?.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, "삭제")
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(currentUserInfo?.id.toString() == postInformation.publisher.toString()) {
            //todo    0: 수정  1: 삭제
            when (item.itemId) {
                Menu.FIRST + 0 -> goToModifyPost()
                Menu.FIRST + 1 -> deletePost()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToModifyPost() {
        val intent = Intent(this@PostActivity, WritePostActivity::class.java)
        intent.putExtra("postInfo", postInformation)
        startActivity(intent)
    }

    private fun deletePost() {
        //todo 막 가져온 게시글 정보 안에 사진들을 검사하고, 사진들을 삭제하고 업데이트 해주고, 사진들이 있다면 삭제해주고 하는게 더 안전할 것 같다
        if (postInformation.id != null) {
            if (postInformation.mainImage != null) {
                val currentPostMainImageRef =
                    storageRef?.child("images/posts/${postInformation.id}/mainImage")
                currentPostMainImageRef?.delete()
                    ?.addOnFailureListener {
                        Toast.makeText(this@PostActivity, R.string.post_delete_fail, Toast.LENGTH_SHORT).show()
                    }
            }
            if (postInformation.images != null) {
                for (i in 0 until (postInformation.images ?: mutableListOf()).size) {
                    val currentPostImageRef =
                        storageRef?.child("images/posts/${postInformation.id}/images$i")
                    currentPostImageRef?.delete()
                        ?.addOnFailureListener {
                            Toast.makeText(this@PostActivity, R.string.post_delete_fail, Toast.LENGTH_SHORT).show()
                        }
                }
            }

            db?.collection("posts")?.document(postInformation.id!!)?.delete()
                ?.addOnSuccessListener {
                    finish()
                }

        } else {
            Toast.makeText(this@PostActivity, R.string.wrong_access, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPostInformation() {
        Glide.with(this@PostActivity).load(publisherProfileImage ?: R.drawable.sample_image)
            .override(200, 200).circleCrop().into(postPublisherProfileImageView)
        postPublisherIdTextView.text = postInformation.publisher
        postTitleTextView.text = postInformation.title
        postTwoLineDescriptionTextView.text = postInformation.twoLineDescription
        postImagesSliderView.setSliderAdapter(postSliderAdapter)
        if (postInformation.images != null) {
            for (i in 0 until postInformation.images!!.size) {
                imagesList.add(postInformation.images!![i])
            }
        }
        //todo image의 형식인지 체크해주면 좋을 듯
        if (imagesList.size > 0 && imagesList[0] != "") postImagesSliderView.visibility =
            View.VISIBLE
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
        toolbar = findViewById(R.id.contentToolbar)
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