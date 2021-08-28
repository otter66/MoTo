package com.otter66.newMoTo.Adapter

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.otter66.newMoTo.Activity.PostActivity
import com.otter66.newMoTo.Data.Post
import com.otter66.newMoTo.Data.User
import com.otter66.newMoTo.R


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
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: MyPostListAdapter.ViewHolder, position: Int) {
        //게시글 item에 post 정보 연결
        if(currentUserPostList.size > 0) {
            if(currentUserPostList[position].mainImage?.contains("http") == true) {
                //일반 사진들은 멀쩡한데 null일 때 가영이가 너무 부담스럽게 확대대서 나와서 이렇게 해줌.. sample 이미지를 바꾸던가 해야지 원.. 가영이의 저주인가..
                Glide.with(activity).load(currentUserPostList[position].mainImage)
                    .override(1000).thumbnail(0.1f).into(holder.itemPostMainImage)
            }
            holder.itemPostProfileImage.visibility = View.GONE
            holder.itemPostProfileBackground.visibility = View.GONE
            holder.itemPostTitleTextView.text = currentUserPostList[position].title ?: ""
            holder.itemPostTwoLineDescriptionTextView.text = currentUserPostList[position].twoLineDescription ?: ""

            holder.itemView.setOnClickListener {
                Log.d("test_log", "currentUserInfo(in Adapter): $currentUserInfo")
                val intent = Intent(activity, PostActivity::class.java)
                intent.putExtra("postInfo", currentUserPostList[holder.adapterPosition])
                intent.putExtra(
                    "publisherProfileImage",
                    currentUserInfo.profileImage)
                intent.putExtra("currentUserInfo", currentUserInfo)
                activity.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return currentUserPostList.size
    }
}