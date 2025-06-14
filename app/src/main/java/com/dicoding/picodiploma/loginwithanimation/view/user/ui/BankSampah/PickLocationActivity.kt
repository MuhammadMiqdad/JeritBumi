package com.dicoding.picodiploma.loginwithanimation.view.user.ui.BankSampah

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.response.SaveLocationRequest
import com.dicoding.picodiploma.loginwithanimation.response.SaveLocationResponse
import com.dicoding.picodiploma.network.ApiClient
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class PickLocationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var markerAnnotationManager: PointAnnotationManager
    private var currentMarker: PointAnnotation? = null
    private var selectedPoint: Point? = null

    // UI Components
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var labelEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var instructionsCard: View

    private lateinit var userPreference: UserPreference
    private var currentAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_location)

        try {
            // Initialize UI components
            mapView = findViewById(R.id.mapView)
            latitudeTextView = findViewById(R.id.latitudeTextView)
            longitudeTextView = findViewById(R.id.longitudeTextView)
            addressTextView = findViewById(R.id.addressTextView)
            labelEditText = findViewById(R.id.labelEditText)
            saveButton = findViewById(R.id.saveButton)
            instructionsCard = findViewById(R.id.instructionsCard)

            // Initialize UserPreference
            userPreference = UserPreference.getInstance(applicationContext.dataStore)

            // Initialize map
            mapboxMap = mapView.getMapboxMap()

            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { style ->
                val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.red_marker_1)
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 128, 128, false)

                style.addImage(
                    "marker-icon",
                    scaledBitmap
                )

                // Set camera to Jatinangor
                val jatinangor = Point.fromLngLat(107.7719, -6.9336)
                mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(jatinangor)
                        .zoom(14.0)
                        .build()
                )
            }

            // Initialize annotation manager for markers
            val annotationPlugin = mapView.annotations
            markerAnnotationManager = annotationPlugin.createPointAnnotationManager()

            // Add map click listener
            mapView.gestures.addOnMapClickListener { point ->
                try {
                    // Remove existing marker
                    currentMarker?.let { markerAnnotationManager.delete(it) }

                    // Create new marker
                    val pointAnnotation = markerAnnotationManager.create(
                        PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage("marker-icon")
                    )

                    currentMarker = pointAnnotation
                    selectedPoint = point

                    // Update location info
                    updateLocationInfo(point)
                    true
                } catch (e: Exception) {
                    Log.e("PickLocationActivity", "Error handling map click: ${e.message}", e)
                    Toast.makeText(this, "Gagal memilih lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
                    false
                }
            }

            // Save button click listener
            saveButton.setOnClickListener {
                saveSelectedLocation()
            }
        } catch (e: Exception) {
            Log.e("PickLocationActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationInfo(point: Point) {
        try {
            latitudeTextView.text = String.format("Latitude: %.6f", point.latitude())
            longitudeTextView.text = String.format("Longitude: %.6f", point.longitude())

            // Get address from coordinates using Geocoder
            val geocoder = Geocoder(this, Locale.getDefault())

            // Solusi untuk semua versi Android
            currentAddress = "Alamat tidak ditemukan"

            try {
                // Untuk Android versi baru (Android 10+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(point.latitude(), point.longitude(), 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            currentAddress = address.getAddressLine(0) ?: "Alamat tidak ditemukan"
                            runOnUiThread {
                                addressTextView.text = String.format("Alamat: %s", currentAddress)
                            }
                        } else {
                            runOnUiThread {
                                addressTextView.text = "Alamat tidak ditemukan"
                            }
                        }
                    }
                } else {
                    // Untuk Android versi lama
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(point.latitude(), point.longitude(), 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        currentAddress = address.getAddressLine(0) ?: "Alamat tidak ditemukan"
                        addressTextView.text = String.format("Alamat: %s", currentAddress)
                    } else {
                        addressTextView.text = "Alamat tidak ditemukan"
                    }
                }
            } catch (e: Exception) {
                Log.e("PickLocationActivity", "Error getting address", e)
                addressTextView.text = "Error mendapatkan alamat: ${e.message}"
            }
        } catch (e: Exception) {
            Log.e("PickLocationActivity", "Error updating location info: ${e.message}", e)
        }
    }

    private fun saveSelectedLocation() {
        try {
            val point = selectedPoint
            if (point == null) {
                Toast.makeText(this, "Silakan pilih lokasi di peta", Toast.LENGTH_SHORT).show()
                return
            }

            // Ambil alamat dari variabel currentAddress, bukan dari TextView
            val address = currentAddress
            val label = labelEditText.text.toString().trim()

            // Validate label
            if (label.isEmpty()) {
                Toast.makeText(this, "Label tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return
            }

            // PERBAIKAN: Gunakan first() daripada collect
            lifecycleScope.launch {
                try {
                    val user = userPreference.getSession().first() // Menggunakan first() daripada collect
                    val token = user.token

                    if (token.isEmpty()) {
                        Toast.makeText(this@PickLocationActivity, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Get coordinates
                    val latitude = point.latitude()
                    val longitude = point.longitude()

                    // Log data yang akan dikirim untuk debugging
                    Log.d("PickLocationActivity", "Saving location: Lat=$latitude, Lng=$longitude, Address=$address, Label=$label")

                    // Create request
                    val apiService = ApiClient.getApiService(token)
                    val saveLocationRequest = SaveLocationRequest(latitude, longitude, address, label)

                    // Make API call
                    val call = apiService.saveLocation("Bearer $token", saveLocationRequest)

                    call.enqueue(object : Callback<SaveLocationResponse> {
                        override fun onResponse(
                            call: Call<SaveLocationResponse>,
                            response: Response<SaveLocationResponse>
                        ) {
                            try {
                                if (response.isSuccessful) {
                                    Log.d("PickLocationActivity", "Location saved successfully: ${response.body()}")
                                    Toast.makeText(
                                        this@PickLocationActivity,
                                        "Lokasi berhasil disimpan!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Return result to calling activity
                                    val resultIntent = Intent().apply {
                                        putExtra("selectedLatitude", latitude)
                                        putExtra("selectedLongitude", longitude)
                                        putExtra("selectedAddress", address)
                                    }
                                    setResult(Activity.RESULT_OK, resultIntent)
                                    finish()
                                } else {
                                    Log.e("PickLocationActivity", "Failed to save location: ${response.errorBody()?.string()}")
                                    Toast.makeText(
                                        this@PickLocationActivity,
                                        "Gagal menyimpan lokasi: ${response.code()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e("PickLocationActivity", "Error processing save response: ${e.message}", e)
                                Toast.makeText(
                                    this@PickLocationActivity,
                                    "Error memproses respons: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<SaveLocationResponse>, t: Throwable) {
                            Log.e("PickLocationActivity", "API call failed: ${t.message}", t)
                            Toast.makeText(
                                this@PickLocationActivity,
                                "Error: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } catch (e: Exception) {
                    Log.e("PickLocationActivity", "Error in saveSelectedLocation coroutine: ${e.message}", e)
                    Toast.makeText(
                        this@PickLocationActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e("PickLocationActivity", "Error saving location: ${e.message}", e)
            Toast.makeText(this, "Gagal menyimpan lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Lifecycle methods
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}