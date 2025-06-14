package com.dicoding.picodiploma.loginwithanimation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.response.ListStoryItem
import com.bumptech.glide.Glide
import android.widget.ImageView
import android.widget.TextView

class StoryAdapter(
    private val onItemClick: (ListStoryItem) -> Unit
) : ListAdapter<ListStoryItem, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story)
    }

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivItemPhoto: ImageView = itemView.findViewById(R.id.iv_item_photo)
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)

        fun bind(story: ListStoryItem) {
            // Bind the data to the views
            tvItemName.text = story.name

            // Load the image using Glide (you need to add the Glide dependency in your build.gradle)
            Glide.with(itemView.context)
                .load(story.photoUrl) // Assume `photoUrl` is a valid URL for the image
                .into(ivItemPhoto)

            // Set up the click listener
            itemView.setOnClickListener { onItemClick(story) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
