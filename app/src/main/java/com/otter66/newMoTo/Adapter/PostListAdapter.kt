package com.otter66.newMoTo.Adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.otter66.newMoTo.Activity.PostActivity
import com.otter66.newMoTo.Data.Post
import com.otter66.newMoTo.R
import com.otter66.newMoTo.Data.User
import com.otter66.newMoTo.Util.TextUtil


class PostListAdapter(var activity: Activity, private val postList: ArrayList<Post>, private val userList: ArrayList<User>): RecyclerView.Adapter<PostListAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemPostMainImage: ImageView = itemView.findViewById(R.id.itemPostMainImage)
        val itemPostProfileImage: ImageView = itemView.findViewById(R.id.itemPostProfileImage)
        val itemPostUserIdTextView: TextView = itemView.findViewById(R.id.itemPostUserIdTextView)
        val itemPostTitleTextView: TextView = itemView.findViewById(R.id.itemPostTitleTextView)
        val itemPostTwoLineDescriptionTextView: TextView =
            itemView.findViewById(R.id.itemPostTwoLineDescriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostListAdapter.ViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: PostListAdapter.ViewHolder, position: Int) {
        val publisherProfileImage = getPublisherProfileImage(postList[position].publisher)

        //게시글 item에 post 정보 연결
        if(postList[position].mainImage != null) {
            //일반 사진들은 멀쩡한데 null일 때 가영이가 너무 부담스럽게 확대대서 나와서 이렇게 해줌.. sample 이미지를 바꾸던가 해야지 원.. 가영이의 저주인가..
            Glide.with(activity).load(postList[position].mainImage)
                .override(1000).thumbnail(0.1f).into(holder.itemPostMainImage)
        }
        Glide.with(activity).load(R.drawable.sample_image).circleCrop()
            .into(holder.itemPostProfileImage)
        Glide.with(activity).load(publisherProfileImage ?: R.drawable.sample_image)
            .override(200, 200).circleCrop().into(holder.itemPostProfileImage)
        holder.itemPostUserIdTextView.text = postList[position].publisher ?: ""
        TextUtil.setTextColor(
            activity,
            holder.itemPostUserIdTextView,
            postList[position].publisher ?: ""
        )
        holder.itemPostTitleTextView.text = postList[position].title ?: ""
        holder.itemPostTwoLineDescriptionTextView.text = postList[position].twoLineDescription ?: ""


        holder.itemView.setOnClickListener {
            val intent = Intent(activity, PostActivity::class.java)
            intent.putExtra("postInfo", postList[holder.adapterPosition])
            intent.putExtra(
                "publisherProfileImage",
                getPublisherProfileImage(postList[holder.adapterPosition].publisher))
            intent.putExtra("currentUserInfo", getCurrentUserInfo())
            activity.startActivity(intent)
        }
    }

    private fun getCurrentUserInfo(): User? {
        val user = Firebase.auth.currentUser
        val currentUserRef = Firebase.firestore.collection("users").document(user!!.uid)
        var currentUserInfo: User? = null
        if (Firebase.auth.currentUser != null) {
            currentUserRef.get()
                .addOnSuccessListener { document ->
                    currentUserInfo = User(
                        document.data?.get("id").toString(),
                        document.data?.get("job").toString(),
                        document.data?.get("group").toString(),
                        document.data?.get("department").toString(),
                        document.data?.get("profileImage").toString()
                    )
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "글을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
        return currentUserInfo
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    private fun getPublisherProfileImage(publisherId: String?): String? {
        for (i in 0 until userList.size) {
            if (userList[i].id == publisherId) {
                return userList[i].profileImage
            }
        }
        return null
    }
}