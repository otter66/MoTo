package com.otter66.newMoTo.Activity

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.otter66.newMoTo.Adapter.PostSliderAdapter
import com.otter66.newMoTo.Data.Post
import com.otter66.newMoTo.R
import com.smarteist.autoimageslider.SliderView
import java.util.*
import kotlin.collections.ArrayList

class WritePostActivity: AppCompatActivity() {

    private var backKeyPressedTime: Long = 0
    private var mainImage: Uri? = null
    private var images: MutableList<String> = mutableListOf()
    private var imagesUri: MutableList<Uri> = mutableListOf()
    private var relatedLinkNames: MutableList<String> = mutableListOf()
    private var relatedLinkAddresses: MutableList<String> = mutableListOf()
    private var currentUserId: String? = null
    private var postInfo: Post? = null
    private var postDatabase: CollectionReference? = null
    private var postData: Post? = null
    private var relatedLinksCount: Int = 0
    private lateinit var postImagesSliderAdapter: PostSliderAdapter

    private lateinit var writePostMainImage: ImageView
    private lateinit var writePostProjectTitleEditText: EditText
    private lateinit var writePostTwoLineDescriptionEditText: EditText
    private lateinit var writePostImagesSlider: SliderView
    //private lateinit var writePostImageAddFloatingButton: FloatingActionButton
    private lateinit var writePostDescriptionEditText: EditText
    private lateinit var writePostUpdateNoteEditText: EditText
    private lateinit var writePostImprovementEditText: EditText
    private lateinit var writePostRelatedLinkContentLayout: LinearLayout
    private lateinit var submitPost: TextView
    private lateinit var addRelatedLinkButton: TextView
    private val linkNameEditTexts: MutableList<EditText> = mutableListOf()
    private var linkAddressEditTexts: MutableList<EditText> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_post)

        viewInit()

        currentUserId = intent.getStringExtra("currentUserId")
        postInfo = intent.getSerializableExtra("postInfo") as Post?
        if (postInfo != null) setPostInfoOnView()
        postDatabase = Firebase.firestore.collection("posts")
        postImagesSliderAdapter = PostSliderAdapter(this@WritePostActivity, images)
        writePostImagesSlider.setSliderAdapter(postImagesSliderAdapter)

        //todo adapter?????? item?????? ???????????? ????????? item ????????? ???????????? ??????????????? ??? ??????..
        Toast.makeText(this, "??? ????????? ???????????? ??? ????????? ???????????????..", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this, R.string.back_pressed_prevent_in_write_post, Toast.LENGTH_SHORT)
                .show()
            return
        } else {
            super.onBackPressed()
        }
    }

    private var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.writePostMainImage -> getMainImage()
                R.id.submitPost -> uploadPost()
                R.id.addRelatedLinkButton -> addRelatedLink()
                //R.id.writePostImageAddFloatingButton -> getSliderImages()
                R.id.writePostImagesSlider -> getSliderImages()
            }
        }


    private fun setPostInfoOnView() {
        mainImage = postInfo?.mainImage?.toUri()
        Glide.with(this@WritePostActivity)
            .load(mainImage)
            .override(1000).into(writePostMainImage)
        writePostProjectTitleEditText.setText(postInfo?.title)
        writePostTwoLineDescriptionEditText.setText(postInfo?.twoLineDescription)
        images = postInfo?.images?.toMutableList() ?: mutableListOf()
        writePostDescriptionEditText.setText(postInfo?.description)
        writePostUpdateNoteEditText.setText(postInfo?.update)
        writePostImprovementEditText.setText(postInfo?.improvement)
        if(postInfo?.linkNames != null) {
            for(i in 0 until postInfo?.linkNames?.size!!) {
                addRelatedLink(postInfo?.linkNames?.get(i), postInfo?.linkAddresses?.get(i))
            }
        }
    }

    private fun uploadPost() {
        //todo image upload ??? ??? ?????? / ????????? ??? ???????????????..???
        getRelatedLinks()
        postData = Post(
            null,
            writePostProjectTitleEditText.text?.toString(),
            writePostTwoLineDescriptionEditText.text?.toString(),
            mainImage.toString(),
            images as ArrayList<String>?,
            writePostDescriptionEditText.text?.toString(),
            writePostUpdateNoteEditText.text?.toString(),
            writePostImprovementEditText.text?.toString(),
            relatedLinkNames as ArrayList<String>,
            relatedLinkAddresses as ArrayList<String>,
            currentUserId,
            Date(System.currentTimeMillis())
        )

        val storageRef = FirebaseStorage.getInstance().reference

        //??? ?????? ????????? ???
        if(postInfo == null) {
            postDatabase?.add(postData!!)?.addOnSuccessListener { currentPostDoc ->
                currentPostDoc.update("id", currentPostDoc.id)
                    .addOnSuccessListener {
                        uploadImages(storageRef, currentPostDoc)
                        finish()
                    }.addOnFailureListener { }
            }?.addOnFailureListener {
                Toast.makeText(this@WritePostActivity, R.string.upload_fail, Toast.LENGTH_SHORT).show()
            }
        }
        //??? ????????? ???
        else {
            postData!!.id = postInfo!!.id
            postData!!.publisher = postInfo!!.publisher
            postDatabase?.document(postInfo!!.id!!)?.set(postData!!)?.addOnSuccessListener {
                uploadImages(storageRef)
                finish()
            }
        }
    }

    private fun uploadImages(storageRef: StorageReference, currentPostDoc: DocumentReference? = null) {
        //todo [ERROR] ?????? ???????????? ?????? ?????? ??????????????? ??????

        //todo main Image Upload
        val currentPostMainImageRef =
            storageRef.child("images/posts/${currentPostDoc?.id}/mainImage")
        var uploadTask: UploadTask = currentPostMainImageRef.putFile(mainImage!!)
        uploadTask.addOnFailureListener {
            Toast.makeText(this@WritePostActivity, R.string.upload_fail, Toast.LENGTH_SHORT)
                .show()
        }.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            currentPostMainImageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val mainImageDownloadUri = task.result
                postData?.mainImage = mainImageDownloadUri.toString()
                currentPostDoc
                    ?.update("mainImage", mainImageDownloadUri.toString())
                    ?.addOnFailureListener {
                        Toast.makeText(
                            this@WritePostActivity,
                            R.string.upload_fail,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }


        //todo slider Images Upload
        if (imagesUri.size != 0 && !imagesUri[0].toString().contains("http")) {
            val imagesDownloadUriTmp = arrayListOf<String>()
            for (i in 0 until imagesUri.size) {
                val currentPostImagesRef =
                    storageRef.child("images/posts/${currentPostDoc?.id}/images$i")
                val uploadTask = currentPostImagesRef.putFile(imagesUri[i])
                uploadTask.addOnFailureListener {
                    Toast.makeText(this@WritePostActivity, R.string.upload_fail, Toast.LENGTH_SHORT)
                        .show()
                }.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    currentPostImagesRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imagesDownloadUriTmp.add(task.result.toString())
                        if (i == imagesUri.size - 1) {
                            postData?.images = imagesDownloadUriTmp
                            currentPostDoc?.update("images", imagesDownloadUriTmp)
                                ?.addOnFailureListener {
                                    Toast.makeText(
                                        this@WritePostActivity,
                                        R.string.upload_fail,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }?.addOnSuccessListener {
                                    Toast.makeText(
                                        this@WritePostActivity,
                                        R.string.upload_success,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
            }
        }
    }

    private fun getRelatedLinks() {
        for (i in 0 until relatedLinksCount) {
            if (linkNameEditTexts[i].text != null && linkNameEditTexts[i].text.toString() != ""
                && linkAddressEditTexts[i].text != null && linkAddressEditTexts[i].text.toString() != ""
            ) {
                relatedLinkNames.add(linkNameEditTexts[i].text.toString())
                relatedLinkAddresses.add(linkAddressEditTexts[i].text.toString())
            }
        }
    }

    private fun viewInit() {
        writePostMainImage = findViewById(R.id.writePostMainImage)
        writePostProjectTitleEditText = findViewById(R.id.writePostProjectTitleEditText)
        writePostTwoLineDescriptionEditText = findViewById(R.id.writePostTwoLineDescriptionEditText)
        writePostImagesSlider = findViewById(R.id.writePostImagesSlider)
        //writePostImageAddFloatingButton = findViewById(R.id.writePostImageAddFloatingButton)
        writePostDescriptionEditText = findViewById(R.id.writePostDescriptionEditText)
        writePostUpdateNoteEditText = findViewById(R.id.writePostUpdateNoteEditText)
        writePostImprovementEditText = findViewById(R.id.writePostImprovementEditText)
        writePostRelatedLinkContentLayout = findViewById(R.id.writePostRelatedLinkContentLayout)
        addRelatedLinkButton = findViewById(R.id.addRelatedLinkButton)
        submitPost = findViewById(R.id.submitPost)

        writePostMainImage.setOnClickListener(onClickListener)
        //writePostImageAddFloatingButton.setOnClickListener(onClickListener)
        writePostImagesSlider.setOnClickListener(onClickListener)
        addRelatedLinkButton.setOnClickListener(onClickListener)
        submitPost.setOnClickListener(onClickListener)
    }

    //todo link delete??? ????????????
    private fun addRelatedLink(linkName: String? = null, linkAddress: String? = null) {
        val linkContainer = LinearLayout(this@WritePostActivity)
        linkContainer.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        linkContainer.orientation = LinearLayout.VERTICAL

        val linkNameAndDeleteContainer = LinearLayout(this@WritePostActivity)
        linkNameAndDeleteContainer.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        linkNameAndDeleteContainer.orientation = LinearLayout.HORIZONTAL

        val linkDeleteButton = ImageView(this@WritePostActivity)
        linkDeleteButton.layoutParams = LayoutParams(80, 80)
        linkDeleteButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_delete_forever_24))
        linkDeleteButton.id = relatedLinksCount

        val linkNameParams: LayoutParams = LayoutParams(
            0, LayoutParams.WRAP_CONTENT
        ).apply { weight = 1f }
        linkNameParams.setMargins(0, 15, 0, 0)
        val linkAddressParams: LayoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        linkAddressParams.setMargins(0, 0, 0, 20)

        linkNameEditTexts.add(EditText(this@WritePostActivity))
        linkAddressEditTexts.add(EditText(this@WritePostActivity))
        linkNameEditTexts[relatedLinksCount].layoutParams = linkNameParams
        linkAddressEditTexts[relatedLinksCount].layoutParams = linkAddressParams
        linkNameEditTexts[relatedLinksCount].hint = getString(R.string.please_related_link_name)
        linkAddressEditTexts[relatedLinksCount].hint =
            getString(R.string.please_related_link_address)
        linkNameEditTexts[relatedLinksCount].setTextColor(getColor(R.color.black))
        linkAddressEditTexts[relatedLinksCount].setTextColor(getColor(R.color.black))
        linkNameEditTexts[relatedLinksCount].textSize = 15f
        linkAddressEditTexts[relatedLinksCount].textSize = 15f
        if(linkName != null) linkNameEditTexts[relatedLinksCount].setText(linkName)
        if(linkAddress != null) linkAddressEditTexts[relatedLinksCount].setText(linkAddress)

        linkNameAndDeleteContainer.addView(linkNameEditTexts[relatedLinksCount])
        linkNameAndDeleteContainer.addView(linkDeleteButton)
        linkContainer.addView(linkNameAndDeleteContainer)
        linkContainer.addView(linkAddressEditTexts[relatedLinksCount])
        writePostRelatedLinkContentLayout.addView(linkContainer)
        relatedLinksCount++

        linkDeleteButton.setOnClickListener {
            //id??? ?????? ???????????? ????????? id??? ???????????????..???? ????????? id??? ??????????????????????????????.. ?????? ??? ???????????? ??????????????? ??? ??????..?
            val deletePosition = linkDeleteButton.id
            if (deletePosition < relatedLinksCount) {
                linkNameEditTexts.removeAt(deletePosition)
                linkAddressEditTexts.removeAt(deletePosition)
                relatedLinksCount--
                linkContainer.removeAllViews()
            } else { //delete??? ?????? ????????? ????????? ??? ?????? id??? ????????? ???????????? ?????? ??????
                //todo ???????????????????????? ?????? ?????? ??? . . .???....???????..?
                //todo ???????????? ????????????????????? ?????????????????? ??????
                //todo ??? ??????????????? ???????????????..?
                //???.....?????? ??????????????? ..???...??????.. ??????
            }
        }
    }

    private fun getMainImage() { //????????? ????????? ????????? ????????? ??? ????????? ?????????
        val permission = ContextCompat.checkSelfPermission(
            this@WritePostActivity, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permission == PackageManager.PERMISSION_GRANTED) {
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

    private fun getSliderImages() {
        val permission = ContextCompat.checkSelfPermission(
            this@WritePostActivity, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permission == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            setSliderImages.launch(intent)
            Toast.makeText(this, "????????? ??? ???????????????", Toast.LENGTH_SHORT).show()

        } else {
            permissionsResultCallback.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val setSliderImages =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK && result.data?.clipData != null) {
                val clipData: ClipData? = result.data?.clipData
                if(clipData != null) {
                    if(clipData.itemCount > 10) {
                        Toast.makeText(this, R.string.notice_max_images_count, Toast.LENGTH_SHORT).show()
                    } else {
                        if(images.count() > 0) {
                            images = mutableListOf()
                            imagesUri = mutableListOf()
                        } else {
                            for(i in 0 until clipData.itemCount) {
                                images.add(clipData.getItemAt(i).uri.toString())
                                imagesUri.add(clipData.getItemAt(i).uri)
                            }
                            postImagesSliderAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }

    private val permissionsResultCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            when (it) {
                true -> {
                }
                false -> {
                    Toast.makeText(
                        this@WritePostActivity,
                        R.string.please_give_permission,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
}