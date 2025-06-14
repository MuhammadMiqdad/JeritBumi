package com.dicoding.picodiploma.loginwithanimation.view.user.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.databinding.ItemPickupHistoryBinding
import com.dicoding.picodiploma.loginwithanimation.response.PickupData

class PickupHistoryAdapter(private val pickupList: List<PickupData>) :
    RecyclerView.Adapter<PickupHistoryAdapter.PickupViewHolder>() {

    inner class PickupViewHolder(val binding: ItemPickupHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickupViewHolder {
        val binding = ItemPickupHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PickupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PickupViewHolder, position: Int) {
        val item = pickupList[position]
        with(holder.binding) {
            textType.text = item.classificationType
            textAddress.text = "Alamat: ${item.address}"
            textSchedule.text = "Jadwal: ${item.schedule}"
            textLocation.text = "Lat: ${item.latitude}, Lng: ${item.longitude}"
            textWeight.text = "Berat: ${item.weight} kg"
            textStatus.text = "Status: ${item.status}"

            Glide.with(imagePickup.context)
                .load(item.imageUrl) // Pastikan item.imageUrl adalah URL gambar yang valid
                .into(imagePickup)
        }
    }

    override fun getItemCount(): Int = pickupList.size
}
