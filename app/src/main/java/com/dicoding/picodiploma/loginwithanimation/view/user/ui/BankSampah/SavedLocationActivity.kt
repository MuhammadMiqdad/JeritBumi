package com.dicoding.picodiploma.loginwithanimation.view.user.ui.BankSampah

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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class SavedLocationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedLocationAdapter
    private lateinit var userPreference: UserPreference
    private lateinit var tvEmptyState: TextView
    private lateinit var fabAddLocation: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_location)

        try {
            // Initialize UI components
            recyclerView = findViewById(R.id.rvSavedLocations)
            tvEmptyState = findViewById(R.id.tvEmptyState)
            fabAddLocation = findViewById(R.id.fabAddLocation)

            // Setup RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(this)
            adapter = SavedLocationAdapter { location ->
                try {
                    // Return selected location to calling activity/fragment
                    val resultIntent = Intent().apply {
                        putExtra("selectedLatitude", location.latitude)
                        putExtra("selectedLongitude", location.longitude)
                        putExtra("selectedAddress", location.address)
                        // Tambahkan flag untuk menandai bahwa ini adalah lokasi yang dipilih dari list
                        putExtra("fromSavedLocation", true)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } catch (e: Exception) {
                    Log.e("SavedLocationActivity", "Error returning location: ${e.message}", e)
                    Toast.makeText(this, "Gagal memilih lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            recyclerView.adapter = adapter

            // Initialize UserPreference
            userPreference = UserPreference.getInstance(dataStore)

            // Setup FAB
            fabAddLocation.setOnClickListener {
                try {
                    val intent = Intent(this, PickLocationActivity::class.java)
                    startActivityForResult(intent, REQUEST_ADD_LOCATION)
                } catch (e: Exception) {
                    Log.e("SavedLocationActivity", "Error launching PickLocationActivity: ${e.message}", e)
                    Toast.makeText(this, "Gagal membuka pemilihan lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // Load saved locations
            loadSavedLocations()
        } catch (e: Exception) {
            Log.e("SavedLocationActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavedLocations() {
        // PERBAIKAN: Gunakan first() instead of collect untuk mendapatkan token sekali saja
        lifecycleScope.launch {
            try {
                val user = userPreference.getSession().first() // Menggunakan first() daripada collect
                val token = user.token
                if (token.isNotEmpty()) {
                    fetchSavedLocations(token)
                } else {
                    Log.e("SavedLocationActivity", "Token kosong")
                    Toast.makeText(this@SavedLocationActivity, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SavedLocationActivity", "Error loading saved locations: ${e.message}", e)
                Toast.makeText(this@SavedLocationActivity, "Gagal memuat lokasi tersimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

                            Log.d("SavedLocationActivity", "Locations fetched successfully: ${locations.size} items")

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
                            Log.e("SavedLocationActivity", "API response not successful: ${response.code()} - ${response.message()}")
                            Toast.makeText(this@SavedLocationActivity, "Gagal memuat lokasi: ${response.code()}", Toast.LENGTH_SHORT).show()
                            tvEmptyState.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        Log.e("SavedLocationActivity", "Error processing response: ${e.message}", e)
                        Toast.makeText(this@SavedLocationActivity, "Gagal memproses data lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<GetLocationsResponse>, t: Throwable) {
                    Log.e("SavedLocationActivity", "API call failed: ${t.message}", t)
                    Toast.makeText(this@SavedLocationActivity, "Gagal memuat lokasi: ${t.message}", Toast.LENGTH_SHORT).show()
                    tvEmptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            })
        } catch (e: Exception) {
            Log.e("SavedLocationActivity", "Error in fetchSavedLocations: ${e.message}", e)
            Toast.makeText(this, "Gagal mengambil data lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            if (requestCode == REQUEST_ADD_LOCATION && resultCode == Activity.RESULT_OK) {
                // Reload locations after adding a new one
                loadSavedLocations()
            }
        } catch (e: Exception) {
            Log.e("SavedLocationActivity", "Error in onActivityResult: ${e.message}", e)
        }
    }

    companion object {
        private const val REQUEST_ADD_LOCATION = 1002
    }
}