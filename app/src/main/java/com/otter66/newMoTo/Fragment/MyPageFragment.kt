package com.otter66.newMoTo.Fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.google.firebase.ktx.Firebase
import com.otter66.newMoTo.Adapter.MyPostListAdapter
import com.otter66.newMoTo.Data.Post
import com.otter66.newMoTo.Data.User
import com.otter66.newMoTo.R

class MyPageFragment  : Fragment() {

    private lateinit var db: FirebaseFirestore
    private var user: FirebaseUser? = null
    private var currentUserRef: DocumentReference? = null
    private lateinit var myPostListAdapter: MyPostListAdapter

    private lateinit var myPageProfileImageView: ImageView
    private lateinit var myPageUserIdTextView: TextView
    private lateinit var goToModifyProfileButton: Button
    private lateinit var myPostListRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_my_page, container, false) as ViewGroup

        viewInit(rootView)
        userDataInit()
        myPostUpdate()

        return rootView
    }

    private fun myPostUpdate() {
        var currentUserInfo: User = User()
        val myPostList: ArrayList<Post> = ArrayList()
        myPostListAdapter = MyPostListAdapter(activity as Activity, myPostList, currentUserInfo)
        myPostListRecyclerView.layoutManager = LinearLayoutManager(activity)
        myPostListRecyclerView.adapter = myPostListAdapter

        if (Firebase.auth.currentUser != null) {
            currentUserRef!!.get()
                .addOnSuccessListener { document ->
                    currentUserInfo = User(
                        document.data?.get("id").toString(),
                        document.data?.get("job").toString(),
                        document.data?.get("group").toString(),
                        document.data?.get("department").toString(),
                        document.data?.get("profileImage").toString())
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "글을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }

            val collectionReferencePosts: CollectionReference = Firebase.firestore.collection("posts")
            collectionReferencePosts.orderBy("createdDate", Query.Direction.DESCENDING).get()
                .addOnSuccessListener { documents ->
                    myPostList.clear()
                    for (document in documents) {
                        if(document.data["publisher"].toString() == currentUserInfo.id.toString()) {
                            myPostList.add(Post(
                                document.id,
                                document.data["title"].toString(),
                                document.data["twoLineDescription"].toString(),
                                document.data["mainImage"].toString(),
                                document.data["images"] as ArrayList<String?>,
                                document.data["description"].toString(),
                                document.data["update"].toString(),
                                document.data["improvement"].toString(),
                                document.data["linkNames"] as ArrayList<String?>,
                                document.data["linkAddresses"] as ArrayList<String?>,
                                document.data["publisher"].toString(),
                                document.getTimestamp("createdDate")?.toDate())
                            )
                        }
                    }
                    myPostListAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "글을 가져오는데에 실패했습니다", Toast.LENGTH_SHORT).show()
                }

        }
    }

    private fun viewInit(rootView: ViewGroup) {
        myPageProfileImageView = rootView.findViewById(R.id.myPageProfileImageView)
        myPageUserIdTextView = rootView.findViewById(R.id.myPageUserIdTextView)
        goToModifyProfileButton = rootView.findViewById(R.id.goToModifyProfileButton)
        myPostListRecyclerView = rootView.findViewById(R.id.myPostListRecyclerView)
    }

    private fun userDataInit() {
        db = Firebase.firestore
        user = Firebase.auth.currentUser
        currentUserRef = db.collection("users").document(user!!.uid)

        currentUserRef!!.get()
            .addOnSuccessListener { document ->
                myPageProfileImageView.background = null
                Glide.with(activity as Activity).load(document.data?.get("profileImage")).override(1000).circleCrop().into(myPageProfileImageView)
                myPageUserIdTextView.text = document.data?.get("id").toString()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(activity, "정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }


}