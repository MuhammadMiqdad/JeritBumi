package com.dicoding.picodiploma.loginwithanimation.view.user.ui.home

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.R

class SampahAdapter(
    private val sampahItems: List<SampahItem>,
    private val onItemClick: (SampahItem) -> Unit
) : RecyclerView.Adapter<SampahAdapter.SampahViewHolder>() {

    class SampahViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardViewSampah)
        val imageView: ImageView = itemView.findViewById(R.id.imageViewSampah)
        val titleView: TextView = itemView.findViewById(R.id.textViewSampahTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampahViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sampah_card_item, parent, false)
        return SampahViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: SampahViewHolder, position: Int) {
        val sampahItem = sampahItems[position]
        holder.imageView.setImageResource(sampahItem.imageResId)
        holder.titleView.text = sampahItem.title

        // Mengatur warna kartu sesuai dengan kategori sampah
        holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(sampahItem.cardColorResId))


        // Menentukan warna teks berdasarkan kecerahan latar belakang
        val color = holder.itemView.context.getColor(sampahItem.cardColorResId)
        val brightness = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000
        if (brightness >= 128) {
            holder.titleView.setTextColor(Color.BLACK)
        } else {
            holder.titleView.setTextColor(Color.WHITE)
        }

        holder.cardView.setOnClickListener {
            onItemClick(sampahItem)
        }
    }

    override fun getItemCount(): Int = sampahItems.size
}

data class SampahItem(
    val id: Int,
    val imageResId: Int,
    val title: String,
    val destinationFragmentId: Int,
    val cardColorResId: Int
)