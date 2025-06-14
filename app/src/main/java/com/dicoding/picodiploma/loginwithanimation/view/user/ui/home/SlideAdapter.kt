package com.dicoding.picodiploma.loginwithanimation.view.user.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.R

class SlideAdapter(private val slides: List<SlideItem>) :
    RecyclerView.Adapter<SlideAdapter.SlideViewHolder>() {

    class SlideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // No need to find views here as we'll use the parent view directly
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        // Create a simple view to hold our position
        val view = View(parent.context)
        return SlideViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        // This will be handled in the fragment
    }

    override fun getItemCount(): Int = slides.size
}

data class SlideItem(
    val imageResId: Int,
    val text: String
)