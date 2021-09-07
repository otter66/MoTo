package com.otter66.newMoTo.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.otter66.newMoTo.Adapter.PostListAdapter
import com.otter66.newMoTo.Data.Post
import com.otter66.newMoTo.R
import com.otter66.newMoTo.Data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class NoticeBoardFragment : Fragment() {

    var db: FirebaseFirestore? = null
    private lateinit var postListAdapter: PostListAdapter
    private lateinit var postList: ArrayList<Post>
    private lateinit var userList: ArrayList<User>
    private var currentUserInfo: User? = null

    private lateinit var postListRecyclerView: RecyclerView
    private lateinit var itemPostUserIdTextView: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        db = Firebase.firestore
        postList = ArrayList()
        userList = ArrayList()

       currentUserInfo = arguments?.getSerializable("currentUserInfo") as User?

        val rootView = inflater.inflate(R.layout.fragment_notice_board, container, false) as ViewGroup

        // adapt post item
        postListRecyclerView = rootView.findViewById(R.id.postListRecyclerView)
        itemPostUserIdTextView = (inflater.inflate(R.layout.item_post, container, false) as ViewGroup)
                .findViewById(R.id.itemPostUserIdTextView)
        postListRecyclerView.setHasFixedSize(true)
        postListAdapter = PostListAdapter(activity as Activity, postList, userList, currentUserInfo)
        postListRecyclerView.layoutManager = LinearLayoutManager(activity)
        postListRecyclerView.adapter = postListAdapter
        postsUpdate()


        return rootView
    }

    override fun onResume() {
        super.onResume()

        postsUpdate()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun postsUpdate() {
        val collectionReferencePosts: CollectionReference = db!!.collection("posts")
        val collectionReferenceUsers: CollectionReference = db!!.collection("users")

        if (Firebase.auth.currentUser != null) {
            collectionReferencePosts.orderBy("createdDate", Query.Direction.DESCENDING).get()
                    .addOnSuccessListener { documents ->
                        postList.clear()
                        for (document in documents) {
                            postList.add(document.toObject())
                        }
                        postListAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(activity, "글을 가져오는데에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }

            collectionReferenceUsers.get()
                    .addOnSuccessListener { documents ->
                        userList.clear()
                        for (document in documents) {
                            userList.add(document.toObject())
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(activity, "글을 가져오는데에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }
            //postListAdapter.notifyDataSetChanged()
        }
    }
}
