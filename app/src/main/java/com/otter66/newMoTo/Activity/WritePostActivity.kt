package com.otter66.newMoTo.Activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.ResourcesCompat.getFont
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.otter66.newMoTo.Data.Post
import com.otter66.newMoTo.R
import com.smarteist.autoimageslider.SliderView
import java.util.*
import kotlin.collections.ArrayList

class WritePostActivity: AppCompatActivity() {

    private var backKeyPressedTime: Long = 0
    private var mainImage: Uri? = null
    private var images: ArrayList<String>? = null
    private var relatedLinkNames: ArrayList<String>? = null
    private var relatedLinkAddresses: ArrayList<String>? = null
    private var currentUserId: String? = null
    private var postDatabase: CollectionReference? = null
    private var postData: Post? = null
    private var relatedLinksCount: Int = 0

    private lateinit var writePostMainImage: ImageView
    private lateinit var writePostProjectTitleEditText: EditText
    private lateinit var writePostTwoLineDescriptionEditText: EditText
    private lateinit var writePostImagesSlider: SliderView
    private lateinit var writePostImageAddFloatingButton: FloatingActionButton
    private lateinit var writePostDescriptionEditText: EditText
    private lateinit var writePostUpdateNoteEditText: EditText
    private lateinit var writePostImprovementEditText: EditText
    private lateinit var writePostRelatedLinkContentLayout: LinearLayout
    private lateinit var submitPost: TextView
    private lateinit var addRelatedLinkButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_post)

        currentUserId = intent.getStringExtra("currentUserId")
        postDatabase = Firebase.firestore.collection("posts")


        //todo related link, images slider

        viewInit()
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this, R.string.back_pressed_prevent_in_write_post, Toast.LENGTH_SHORT).show()
            return
        } else {
            //super.onBackPressed()
            finish()
        }
    }

    private var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.writePostMainImage -> getMainImage()
                R.id.submitPost -> uploadPost()
                R.id.addRelatedLinkButton -> addRelatedLink()
            }
        }

    private fun uploadPost() {
        postData = Post(
            null,
            writePostProjectTitleEditText.text?.toString(),
            writePostTwoLineDescriptionEditText.text?.toString(),
            null,
            null,
            writePostDescriptionEditText.text?.toString(),
            writePostUpdateNoteEditText.text?.toString(),
            writePostImprovementEditText.text?.toString(),
            relatedLinkNames,
            relatedLinkAddresses,
            currentUserId,
            Date(System.currentTimeMillis())
        )
        //todo image는 storage에

        val storageRef = FirebaseStorage.getInstance().reference

        //글 새로 작성할 때
        postDatabase?.add(postData!!)?.
        addOnSuccessListener { currentPostDoc ->
            uploadMainImage(storageRef, currentPostDoc)

            finish()
        }?.addOnFailureListener {
            Toast.makeText(this@WritePostActivity, R.string.upload_fail, Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadMainImage(storageRef: StorageReference, currentPostDoc: DocumentReference?) {
        val currentPostMainImageRef =
            storageRef.child("images/posts/${currentPostDoc}/mainImage")
        if(mainImage != null) {
            val uploadTask = currentPostMainImageRef.putFile(mainImage!!)
            uploadTask.addOnFailureListener {
                Toast.makeText(this@WritePostActivity, R.string.upload_fail, Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
            }.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                currentPostMainImageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val mainImageDownloadUri = task.result
                    postData?.mainImage = mainImageDownloadUri.toString()

                    currentPostDoc?.set(postData!!)?.
                    addOnFailureListener {
                        Toast.makeText(this@WritePostActivity, R.string.upload_fail, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun viewInit() {
        writePostMainImage = findViewById(R.id.writePostMainImage)
        writePostProjectTitleEditText = findViewById(R.id.writePostProjectTitleEditText)
        writePostTwoLineDescriptionEditText = findViewById(R.id.writePostTwoLineDescriptionEditText)
        writePostImagesSlider = findViewById(R.id.writePostImagesSlider)
        writePostImageAddFloatingButton = findViewById(R.id.writePostImageAddFloatingButton)
        writePostDescriptionEditText = findViewById(R.id.writePostDescriptionEditText)
        writePostUpdateNoteEditText = findViewById(R.id.writePostUpdateNoteEditText)
        writePostImprovementEditText = findViewById(R.id.writePostImprovementEditText)
        writePostRelatedLinkContentLayout = findViewById(R.id.writePostRelatedLinkContentLayout)
        addRelatedLinkButton = findViewById(R.id.addRelatedLinkButton)
        submitPost = findViewById(R.id.submitPost)

        writePostMainImage.setOnClickListener(onClickListener)
        submitPost.setOnClickListener(onClickListener)
        addRelatedLinkButton.setOnClickListener(onClickListener)
    }

    private fun addRelatedLink() {
        val linkNameParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linkNameParams.setMargins(0, 15, 0, 0)
        val linkAddressParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linkAddressParams.setMargins(0, 0, 0, 15)

        //todo 여기여기여기!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 링크입력했을 때만 추가추가, ET에서 어떻게 text 가져올거?
        val linkNameEditText: EditText = EditText(this@WritePostActivity)
        val linkAddressEditText: EditText = EditText(this@WritePostActivity)
        linkNameEditText.id = relatedLinksCount
        linkAddressEditText.id = relatedLinksCount
        linkNameEditText.layoutParams = linkNameParams
        linkAddressEditText.layoutParams = linkAddressParams
        linkNameEditText.hint = getString(R.string.please_related_link_name)
        linkAddressEditText.hint = getString(R.string.please_related_link_address)
        linkNameEditText.setTextColor(getColor(R.color.black))
        linkAddressEditText.setTextColor(getColor(R.color.black))
        linkNameEditText.textSize = 15f
        linkAddressEditText.textSize = 15f
        //linkNameEditText.typeface = getFont(this, R.font.nanum_myeongjo)


        writePostRelatedLinkContentLayout.addView(linkNameEditText)
        writePostRelatedLinkContentLayout.addView(linkAddressEditText)
        relatedLinksCount++
    }

    private fun getMainImage() { //저장소 권한이 있는지 확인한 후 이미지 가져옴
        val permission = ContextCompat.checkSelfPermission(
            this@WritePostActivity, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission == PackageManager.PERMISSION_GRANTED) {
            //사진 가져오기
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            setMainImage.launch(intent)
        } else {
            permissionsResultCallback.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val setMainImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK && result.data?.data != null) {
                mainImage = result.data?.data!!
                Glide.with(this@WritePostActivity)
                    .load(mainImage)
                    .override(1000).into(writePostMainImage)
            }
        }

    private val permissionsResultCallback = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        when (it) {
            true -> { }
            false -> {
                Toast.makeText(this@WritePostActivity, R.string.please_give_permission, Toast.LENGTH_SHORT).show()
            }
        }
    }
}