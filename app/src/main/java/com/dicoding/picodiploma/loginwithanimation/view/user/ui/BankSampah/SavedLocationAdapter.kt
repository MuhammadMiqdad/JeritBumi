package com.dicoding.picodiploma.loginwithanimation.view.user.ui.BankSampah

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.response.LocationData

class SavedLocationAdapter(private val onClick: (LocationData) -> Unit) : ListAdapter<LocationData, SavedLocationAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = getItem(position)
        holder.bind(location)
        holder.itemView.setOnClickListener { onClick(location) }
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val labelTextView: TextView = itemView.findViewById(R.id.tvLabel)
        private val addressTextView: TextView = itemView.findViewById(R.id.tvAddress)
        private val coordinatesTextView: TextView = itemView.findViewById(R.id.tvCoordinates)

        fun bind(location: LocationData) {
            labelTextView.text = location.label
            addressTextView.text = location.address
            coordinatesTextView.text = "Lat: ${location.latitude}, Lng: ${location.longitude}"
        }
    }

    class LocationDiffCallback : DiffUtil.ItemCallback<LocationData>() {
        override fun areItemsTheSame(oldItem: LocationData, newItem: LocationData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LocationData, newItem: LocationData): Boolean {
            return oldItem == newItem
        }
    }
}
