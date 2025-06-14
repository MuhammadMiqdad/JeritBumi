package com.dicoding.picodiploma.loginwithanimation.view.user.ui.notifications

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityHistoryBinding
import com.dicoding.picodiploma.loginwithanimation.response.PickupData
import com.dicoding.picodiploma.network.ApiClient
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(dataStore)

        lifecycleScope.launch {
            userPreference.getSession().collect { user ->
                val token = user.token
                val api = ApiClient.getApiService(token)
                try {
                    val response = api.getPickupRequests()
                    showHistory(response.data)
                } catch (e: Exception) {
                    Toast.makeText(this@HistoryActivity, "Gagal mengambil data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showHistory(pickupList: List<PickupData>) {
        val adapter = PickupHistoryAdapter(pickupList)
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter
    }
}
