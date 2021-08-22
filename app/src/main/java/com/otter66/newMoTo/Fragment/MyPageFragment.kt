package com.otter66.newMoTo.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.otter66.newMoTo.Adapter.MyPostListAdapter
import com.otter66.newMoTo.Data.Post
import com.otter66.newMoTo.Data.User
import com.otter66.newMoTo.R
import com.otter66.newMoTo.Util.PermissionUtil.checkAndGetCameraPermission
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class MyPageFragment  : Fragment() {

    private lateinit var db: FirebaseFirestore
    private var user: FirebaseUser? = null
    private var currentUserRef: DocumentReference? = null
    private lateinit var myPostListAdapter: MyPostListAdapter
    private var currentUserInfo: User? = null
    private val myPostList: ArrayList<Post> = ArrayList()

    private lateinit var myPageProfileImageView: ImageView
    private lateinit var myPageUserIdTextView: TextView
    //private lateinit var goToModifyProfileButton: Button //add later
    private lateinit var myPostListRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_my_page, container, false) as ViewGroup

        db = Firebase.firestore
        user = Firebase.auth.currentUser
        currentUserRef = db.collection("users").document(user!!.uid)

        lifecycleScope.launch {
            viewInit(rootView)
            userDataInit()

            if(currentUserInfo != null) {
                myPostListAdapter = MyPostListAdapter(activity as Activity, myPostList, currentUserInfo!!)
                myPostListRecyclerView.setHasFixedSize(true)
                myPostListRecyclerView.layoutManager = LinearLayoutManager(activity)
                myPostListRecyclerView.adapter = myPostListAdapter
            }
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()

        myPostUpdate()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun myPostUpdate() {
        if (Firebase.auth.currentUser != null) {
            val collectionReferencePosts: CollectionReference = Firebase.firestore.collection("posts")
            collectionReferencePosts.orderBy("createdDate", Query.Direction.DESCENDING).get()
                .addOnSuccessListener { documents ->
                    myPostList.clear()
                    for (document in documents) {
                        if(document.data["publisher"].toString() == currentUserInfo?.id.toString()) {
                            myPostList.add(document.toObject())
                        }
                    }
                    myPostListAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "글을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
                }

        }
    }

    private fun viewInit(rootView: ViewGroup) {
        myPageProfileImageView = rootView.findViewById(R.id.myPageProfileImageView)
        myPageUserIdTextView = rootView.findViewById(R.id.myPageUserIdTextView)
        //goToModifyProfileButton = rootView.findViewById(R.id.goToModifyProfileButton)
        myPostListRecyclerView = rootView.findViewById(R.id.myPostListRecyclerView)

        myPageProfileImageView.setOnClickListener(onClickListener)
        //goToModifyProfileButton.setOnClickListener(onClickListener)
    }

    private suspend fun userDataInit() {
        if (Firebase.auth.currentUser != null) {
            currentUserRef!!.get()
                .addOnSuccessListener { document ->
                    currentUserInfo = document.toObject<User>()
                    Glide.with(activity as Activity).load(currentUserInfo?.profileImage ?: R.drawable.sample_image).override(1000).circleCrop().into(myPageProfileImageView)
                    myPageUserIdTextView.text = currentUserInfo?.id.toString()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "정보를 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
                .await()
        }
    }

    private var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.myPageProfileImageView -> {
                    if(checkAndGetCameraPermission(activity as Activity)) {
                        //todo permission 얻어오는데 뭐가 문제가 있는걸까..
                    }
                    //todo 사진 가져오기
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    setProfileImageToGalleryImage.launch(intent)
                }
            }
        }

    private var setProfileImageToGalleryImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->

        if(result.resultCode == RESULT_OK && result.data?.data != null) {
            val selectedImageUri: Uri = result.data?.data!!
            val storageRef = FirebaseStorage.getInstance("gs://newmoto-8f49d.appspot.com/").reference
            val userProfileRef = storageRef.child("images/users/${user?.uid}/profileImage/${selectedImageUri}")
            val file: Uri = Uri.fromFile(File(selectedImageUri.toString()))
            val uploadTask = userProfileRef.putFile(file)

            //todo 웨..? 웨..... 웨 uploadTask error남..  dho . .. . .. . . ..,., ., ., .
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Log.d("test_log", "uploadTask.addOnFailureListener")
                Toast.makeText(activity, R.string.upload_fail, Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                val newCurrentUserInfo = User(
                    currentUserInfo?.id,
                    currentUserInfo?.stateMessage,
                    currentUserInfo?.job,
                    currentUserInfo?.group,
                    currentUserInfo?.department,
                    userProfileRef.downloadUrl.toString()
                )
                currentUserRef?.set(newCurrentUserInfo)
                    ?.addOnFailureListener { Toast.makeText(activity, R.string.upload_fail, Toast.LENGTH_SHORT).show() }
                Glide.with(activity as Activity).load(selectedImageUri).override(1000).circleCrop().into(myPageProfileImageView)
            }
        }
    }

}
