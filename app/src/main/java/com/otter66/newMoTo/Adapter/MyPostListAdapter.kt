package com.otter66.newMoTo.Adapter

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.otter66.newMoTo.Activity.PostActivity
import com.otter66.newMoTo.Data.Post
import com.otter66.newMoTo.Data.User
import com.otter66.newMoTo.R
import com.otter66.newMoTo.Util.Util


class MyPostListAdapter(var activity: Activity, private val currentUserPostList: ArrayList<Post>, private val currentUserInfo: User): RecyclerView.Adapter<MyPostListAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemPostMainImage: ImageView = itemView.findViewById(R.id.itemPostMainImage)
        val itemPostProfileImage: ImageView = itemView.findViewById(R.id.itemPostProfileImage)
        val itemPostTitleTextView: TextView = itemView.findViewById(R.id.itemPostTitleTextView)
        val itemPostTwoLineDescriptionTextView: TextView =
            itemView.findViewById(R.id.itemPostTwoLineDescriptionTextView)

        val itemPostProfileBackground: ImageView = itemView.findViewById(R.id.itemPostProfileBackground)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostListAdapter.ViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        Log.d("test_log", "currentUser: ${currentUserInfo}")
        Log.d("test_log", "currentUserPostList: ${currentUserPostList}")
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: MyPostListAdapter.ViewHolder, position: Int) {
        //게시글 item에 post 정보 연결
        if(currentUserPostList.size > 0) {
            Glide.with(activity).load(currentUserPostList[position].mainImage ?: R.drawable.sample_image)
                .override(1000).thumbnail(0.1f).into(holder.itemPostMainImage)
            holder.itemPostProfileImage.visibility = View.GONE
            holder.itemPostProfileBackground.visibility = View.GONE
            holder.itemPostTitleTextView.text = currentUserPostList[position].title ?: ""
            holder.itemPostTwoLineDescriptionTextView.text = currentUserPostList[position].twoLineDescription ?: ""

            holder.itemView.setOnClickListener {
                val intent = Intent(activity, PostActivity::class.java)
                intent.putExtra("postInfo", currentUserPostList[holder.adapterPosition])
                intent.putExtra("currentUserInfo", currentUserInfo)
                activity.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return currentUserPostList.size
    }
}