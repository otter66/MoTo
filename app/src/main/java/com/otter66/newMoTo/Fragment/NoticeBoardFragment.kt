package com.otter66.newMoTo.Fragment

import android.app.Activity
import android.os.Bundle
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
import com.google.firebase.ktx.Firebase


class NoticeBoardFragment : Fragment() {

    var firebaseFirestore: FirebaseFirestore? = null
    private lateinit var postListAdapter: PostListAdapter
    private lateinit var postList: ArrayList<Post>
    private lateinit var userList: ArrayList<User>

    private lateinit var postListRecyclerView: RecyclerView
    private lateinit var itemPostUserIdTextView: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        firebaseFirestore = Firebase.firestore
        postList = ArrayList()
        userList = ArrayList()

        val rootView = inflater.inflate(R.layout.fragment_notice_board, container, false) as ViewGroup

        // adapt post item
        postListRecyclerView = rootView.findViewById(R.id.postListRecyclerView)
        itemPostUserIdTextView = (inflater.inflate(R.layout.item_post, container, false) as ViewGroup)
                .findViewById(R.id.itemPostUserIdTextView)
        postListRecyclerView.setHasFixedSize(true)
        postListAdapter = PostListAdapter(activity as Activity, postList, userList)
        postListRecyclerView.layoutManager = LinearLayoutManager(activity)
        postListRecyclerView.adapter = postListAdapter
        postsUpdate()


        return rootView
    }

    override fun onResume() {
        super.onResume()

        postsUpdate()
    }

    private fun postsUpdate() {
        val collectionReferencePosts: CollectionReference = firebaseFirestore!!.collection("posts")
        val collectionReferenceUsers: CollectionReference = firebaseFirestore!!.collection("users")

        if (Firebase.auth.currentUser != null) {
            collectionReferencePosts.orderBy("createdDate", Query.Direction.DESCENDING).get()
                    .addOnSuccessListener { documents ->
                        postList.clear()
                        for (document in documents) {
                            postList.add(Post(
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
                        //postListAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(activity, "글을 가져오는데에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }

            collectionReferenceUsers.get()
                    .addOnSuccessListener { documents ->
                        userList.clear()
                        for (document in documents) {
                            userList.add(User(
                                    document.data["id"].toString(),
                                    document.data["job"].toString(),
                                    document.data["group"].toString(),
                                    document.data["department"].toString(),
                                    document.data["profileImage"].toString())
                            )
                        }
                        postListAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(activity, "글을 가져오는데에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }

            //postListAdapter.notifyDataSetChanged()
        }
    }
}
