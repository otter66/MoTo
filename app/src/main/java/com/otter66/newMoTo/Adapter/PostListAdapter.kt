package com.otter66.newMoTo.Adapter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.otter66.newMoTo.Fragment.PostFragment
import com.otter66.newMoTo.Data.Post
import com.otter66.newMoTo.R
import com.otter66.newMoTo.Data.User
import com.otter66.newMoTo.Util.Util


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
        Glide.with(activity).load(postList[position].mainImage ?: R.drawable.sample_image)
            .override(1000).thumbnail(0.1f).into(holder.itemPostMainImage)
        Glide.with(activity).load(R.drawable.sample_image).circleCrop()
            .into(holder.itemPostProfileImage)
        Glide.with(activity).load(publisherProfileImage ?: R.drawable.sample_image)
            .override(200, 200).circleCrop().into(holder.itemPostProfileImage)
        holder.itemPostUserIdTextView.text = postList[position].publisher ?: ""
        Util.setTextColor(
            activity,
            holder.itemPostUserIdTextView,
            postList[position].publisher ?: ""
        )
        holder.itemPostTitleTextView.text = postList[position].title ?: ""
        holder.itemPostTwoLineDescriptionTextView.text = postList[position].twoLineDescription ?: ""

        //아이템 클릭시 게시글 fragment on
        holder.itemView.setOnClickListener {
            val postFragment = PostFragment()
            val bundle = Bundle()

            Log.d("test_log", "postInfo: ${postList[holder.adapterPosition]}")
            bundle.putSerializable("postInfo", postList[holder.adapterPosition])
            bundle.putSerializable(
                "publisherProfileImage",
                getPublisherProfileImage(postList[holder.adapterPosition].publisher)
            )
            postFragment.arguments = bundle

            (activity as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.contentLayout, postFragment)
                .addToBackStack(null)
                .commit()
        }
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