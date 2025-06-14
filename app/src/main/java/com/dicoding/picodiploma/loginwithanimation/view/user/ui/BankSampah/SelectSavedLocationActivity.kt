package com.dicoding.picodiploma.loginwithanimation.view.user.ui.BankSampah

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.response.GetLocationsResponse
import com.dicoding.picodiploma.network.ApiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class SelectSavedLocationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: SavedLocationAdapter
    private lateinit var userPreference: UserPreference

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_saved_location)

        try {
            // Initialize UI components
            recyclerView = findViewById(R.id.rvSavedLocations1)
            tvEmptyState = findViewById(R.id.tvEmptyState)

            // Setup RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(this)
            adapter = SavedLocationAdapter { location ->
                try {
                    // Return selected location to calling activity/fragment
                    val resultIntent = Intent().apply {
                        putExtra("selectedLatitude", location.latitude)
                        putExtra("selectedLongitude", location.longitude)
                        putExtra("selectedAddress", location.address)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } catch (e: Exception) {
                    Log.e("SelectSavedLocationActivity", "Error returning location: ${e.message}", e)
                    Toast.makeText(this, "Gagal memilih lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            recyclerView.adapter = adapter

            // Initialize UserPreference
            userPreference = UserPreference.getInstance(dataStore)

            // Load saved locations
            loadSavedLocations()
        } catch (e: Exception) {
            Log.e("SelectSavedLocationActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("LongLogTag")
    private fun loadSavedLocations() {
        // PERBAIKAN: Gunakan first() instead of collect untuk mendapatkan token sekali saja
        lifecycleScope.launch {
            try {
                val user = userPreference.getSession().first() // Menggunakan first() daripada collect
                val token = user.token
                if (token.isNotEmpty()) {
                    fetchSavedLocations(token)
                } else {
                    Log.e("SelectSavedLocationActivity", "Token kosong")
                    Toast.makeText(this@SelectSavedLocationActivity, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SelectSavedLocationActivity", "Error loading saved locations: ${e.message}", e)
                Toast.makeText(this@SelectSavedLocationActivity, "Gagal memuat lokasi tersimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun fetchSavedLocations(token: String) {
        try {
            val apiService = ApiClient.getApiService(token)
            val call = apiService.getCustomLocations("Bearer $token")

            call.enqueue(object : retrofit2.Callback<GetLocationsResponse> {
                override fun onResponse(call: retrofit2.Call<GetLocationsResponse>, response: retrofit2.Response<GetLocationsResponse>) {
                    try {
                        if (response.isSuccessful) {
                            val getLocationsResponse = response.body()
                            val locations = getLocationsResponse?.data ?: emptyList()

                            Log.d("SelectSavedLocationActivity", "Locations fetched successfully: ${locations.size} items")

                            // Update UI based on data
                            if (locations.isEmpty()) {
                                tvEmptyState.visibility = View.VISIBLE
                                recyclerView.visibility = View.GONE
                            } else {
                                tvEmptyState.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                adapter.submitList(locations)
                            }
                        } else {
                            Log.e("SelectSavedLocationActivity", "API response not successful: ${response.code()} - ${response.message()}")
                            Toast.makeText(this@SelectSavedLocationActivity, "Gagal memuat lokasi: ${response.code()}", Toast.LENGTH_SHORT).show()
                            tvEmptyState.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        Log.e("SelectSavedLocationActivity", "Error processing response: ${e.message}", e)
                        Toast.makeText(this@SelectSavedLocationActivity, "Gagal memproses data lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<GetLocationsResponse>, t: Throwable) {
                    Log.e("SelectSavedLocationActivity", "API call failed: ${t.message}", t)
                    Toast.makeText(this@SelectSavedLocationActivity, "Gagal memuat lokasi: ${t.message}", Toast.LENGTH_SHORT).show()
                    tvEmptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            })
        } catch (e: Exception) {
            Log.e("SelectSavedLocationActivity", "Error in fetchSavedLocations: ${e.message}", e)
            Toast.makeText(this, "Gagal mengambil data lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}