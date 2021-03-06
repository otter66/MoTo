package com.otter66.newMoTo.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.net.Uri
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import com.otter66.newMoTo.Activity.WritePostActivity


class MyPageFragment  : Fragment() {

    private var db: FirebaseFirestore? = null
    private var user: FirebaseUser? = null
    private var currentUserRef: DocumentReference? = null
    private var myPostListAdapter: MyPostListAdapter? = null
    private var currentUserInfo: User? = null
    private val myPostList: ArrayList<Post> = ArrayList()

    private lateinit var myPageProfileImageView: ImageView
    private lateinit var myPageUserIdTextView: TextView
    private lateinit var goToWritePostButton: Button

    //private lateinit var goToModifyProfileButton: Button //add later
    private lateinit var myPostListRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_my_page, container, false) as ViewGroup

        db = Firebase.firestore
        user = Firebase.auth.currentUser
        currentUserRef = db?.collection("users")?.document(user!!.uid)

        viewInit(rootView)
        userDataInit()

        if (currentUserInfo != null) {
            myPostListAdapter =
                MyPostListAdapter(activity as Activity, myPostList, currentUserInfo!!)
            myPostListRecyclerView.setHasFixedSize(true)
            myPostListRecyclerView.layoutManager = LinearLayoutManager(activity)
            myPostListRecyclerView.adapter = myPostListAdapter
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()

        myPostUpdate()
    }

    private var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.myPageProfileImageView -> {
                    val permission = ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)

                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        //todo ?????? ????????????
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.setDataAndType(
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "image/*"
                        )
                        setProfileImage.launch(intent)
                    } else {
                        //permissionsResultCallback.launch(Manifest.permission.READ_CONTACTS)
                        permissionsResultCallback.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
                R.id.goToWritePostButton -> {
                    val intent = Intent(activity, WritePostActivity::class.java)
                    //todo currentUserInfo??? null?????? ????????? ????????? ?????????..?
                    intent.putExtra("currentUserId",currentUserInfo?.id)
                    startActivity(intent)
                }
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun myPostUpdate() {
        if (Firebase.auth.currentUser != null) {
            val collectionReferencePosts: CollectionReference =
                Firebase.firestore.collection("posts")
            collectionReferencePosts.orderBy("createdDate", Query.Direction.DESCENDING).get()
                .addOnSuccessListener { documents ->
                    myPostList.clear()
                    for (document in documents) {
                        if (document.data["publisher"].toString() == currentUserInfo?.id.toString()) {
                            myPostList.add(document.toObject())
                        }
                    }
                    myPostListAdapter?.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "?????? ???????????? ???????????????", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun viewInit(rootView: ViewGroup) {
        myPageProfileImageView = rootView.findViewById(R.id.myPageProfileImageView)
        myPageUserIdTextView = rootView.findViewById(R.id.myPageUserIdTextView)
        goToWritePostButton = rootView.findViewById(R.id.goToWritePostButton)
        //goToModifyProfileButton = rootView.findViewById(R.id.goToModifyProfileButton)
        myPostListRecyclerView = rootView.findViewById(R.id.myPostListRecyclerView)

        myPageProfileImageView.setOnClickListener(onClickListener)
        goToWritePostButton.setOnClickListener(onClickListener)
        //goToModifyProfileButton.setOnClickListener(onClickListener)
    }

    private fun userDataInit() {
        currentUserInfo = arguments?.getSerializable("currentUserInfo") as User?
        if(activity != null) {
            Glide.with(activity as Activity)
                .load(currentUserInfo?.profileImage ?: R.drawable.sample_image)
                .override(1000).circleCrop().into(myPageProfileImageView)
            myPageUserIdTextView.text = currentUserInfo?.id.toString()
        }
    }

    private var setProfileImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            if (result.resultCode == RESULT_OK && result.data?.data != null) {

                val storageRef = FirebaseStorage.getInstance().reference
                val currentUserProfileRef =
                    storageRef.child("images/users/${user?.uid}/profileImage/${currentUserInfo?.id}ProfileImage")
                val selectedImageUri: Uri = result.data?.data!!

                //firebase storage??? ???????????????, user document??? profile??? ???????????????, user document?????? ????????? profile??? ????????????
                val uploadTask = currentUserProfileRef.putFile(selectedImageUri)
                uploadTask.addOnFailureListener {
                    Toast.makeText(activity, R.string.upload_fail, Toast.LENGTH_SHORT).show()
                }.addOnSuccessListener { taskSnapshot ->
                }.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    currentUserProfileRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val newCurrentUserInfo = User(
                            currentUserInfo?.id,
                            currentUserInfo?.stateMessage,
                            currentUserInfo?.job,
                            currentUserInfo?.group,
                            currentUserInfo?.department,
                            downloadUri.toString()
                        )

                        currentUserRef?.set(newCurrentUserInfo)
                            ?.addOnFailureListener {
                                Toast.makeText(activity, R.string.upload_fail, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        Glide.with(activity as Activity).load(selectedImageUri).override(1000)
                            .circleCrop().into(myPageProfileImageView)
                    }
                }
            }
        }

    private val permissionsResultCallback = registerForActivityResult(RequestPermission()){
        when (it) {
            true -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.setDataAndType(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*"
                )
                setProfileImage.launch(intent)
            }
            false -> {
                Toast.makeText(requireContext(), R.string.please_give_permission, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

