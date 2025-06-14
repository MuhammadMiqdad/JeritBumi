package com.dicoding.picodiploma.loginwithanimation.view.user.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.response.LocationData

class AddressAdapter(
    private val context: Context,
    private val onAddressClickListener: (LocationData) -> Unit
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    private val addressList: MutableList<LocationData> = mutableListOf()

    fun submitList(newLocations: List<LocationData>) {
        addressList.clear()
        addressList.addAll(newLocations)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val location = addressList[position]
        holder.bind(location)
        holder.itemView.setOnClickListener {
            onAddressClickListener(location)
        }
    }

    override fun getItemCount(): Int = addressList.size

    inner class AddressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val addressTitle: TextView = view.findViewById(R.id.address_label)
        private val addressDetails: TextView = view.findViewById(R.id.address)
        private val addressCoordinates: TextView = view.findViewById(R.id.addressCoordinates)

        fun bind(location: LocationData) {
            addressTitle.text = location.label
            addressDetails.text = location.address
            addressCoordinates.text = "Lat: ${location.latitude}, Lng: ${location.longitude}"
        }
    }
}
