package com.dicoding.picodiploma.loginwithanimation.view.user.ui.BankSampah

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.response.LocationData
import com.dicoding.picodiploma.loginwithanimation.view.user.adapter.AddressAdapter
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.VectorSource
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import java.util.Locale

class BankSampahFragment : Fragment() {

    private lateinit var mapView: MapView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val locationPickerResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                if (result.resultCode == Activity.RESULT_OK) {
                    val latitude = result.data?.getDoubleExtra("selectedLatitude", -1.0)
                    val longitude = result.data?.getDoubleExtra("selectedLongitude", -1.0)
                    val address = result.data?.getStringExtra("selectedAddress")
                    if (latitude != null && longitude != null && latitude != -1.0 && longitude != -1.0) {
                        updateLocationInfo(latitude, longitude, address)
                    }
                }
            } catch (e: Exception) {
                Log.e("BankSampahFragment", "Error in locationPickerResultLauncher: ${e.message}", e)
                Toast.makeText(context, "Terjadi kesalahan saat memilih lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    private val savedLocationResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                if (result.resultCode == Activity.RESULT_OK) {
                    val latitude = result.data?.getDoubleExtra("selectedLatitude", -1.0)
                    val longitude = result.data?.getDoubleExtra("selectedLongitude", -1.0)
                    val address = result.data?.getStringExtra("selectedAddress")
                    val fromSavedLocation = result.data?.getBooleanExtra("fromSavedLocation", false) ?: false

                    if (latitude != null && longitude != null && latitude != -1.0 && longitude != -1.0) {
                        // Tambahkan log untuk debugging
                        Log.d("BankSampahFragment", "Received location: Lat=$latitude, Lng=$longitude, Address=$address, FromSaved=$fromSavedLocation")
                        updateLocationInfo(latitude, longitude, address)
                    }
                }
            } catch (e: Exception) {
                Log.e("BankSampahFragment", "Error in savedLocationResultLauncher: ${e.message}", e)
                Toast.makeText(context, "Terjadi kesalahan saat memilih lokasi tersimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    private lateinit var infoPanel: View
    private lateinit var titleView: TextView
    private lateinit var addressView: TextView
    private lateinit var coordinateView: TextView
    private lateinit var navigateButton: Button
    private lateinit var useCurrentLocationButton: View
    private lateinit var btnPickLocation: View
    private lateinit var addressListContainer: RecyclerView
    private lateinit var addressAdapter: AddressAdapter
    private var pickedLocationMarker: PointAnnotation? = null
    private lateinit var pointAnnotationManager: PointAnnotationManager

    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    private var selectedTpsLatitude: Double? = null
    private var selectedTpsLongitude: Double? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            val view = inflater.inflate(R.layout.fragment_bank_sampah, container, false)

            // Inisialisasi komponen UI
            mapView = view.findViewById(R.id.mapView)
            infoPanel = view.findViewById(R.id.infoPanel)
            titleView = view.findViewById(R.id.tpsTitle)
            addressView = view.findViewById(R.id.tpsAddress)
            coordinateView = view.findViewById(R.id.tpsCoordinate)
            navigateButton = view.findViewById(R.id.navigateButton)

            // FAB buttons di versi baru
            useCurrentLocationButton = view.findViewById(R.id.useCurrentLocationButton)
            btnPickLocation = view.findViewById(R.id.btnPickLocation)

            // RecyclerView untuk daftar alamat
            addressListContainer = view.findViewById(R.id.addressListContainer)

            // Setup RecyclerView dan adapter
            addressListContainer.layoutManager = LinearLayoutManager(requireContext())
            addressAdapter = AddressAdapter(requireContext()) { location ->
                try {
                    // Handle address item click
                    updateLocationInfo(location.latitude, location.longitude, location.address)
                    addressListContainer.visibility = View.GONE
                } catch (e: Exception) {
                    Log.e("BankSampahFragment", "Error handling address click: ${e.message}", e)
                    Toast.makeText(context, "Gagal memilih alamat: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            addressListContainer.adapter = addressAdapter

            // Setup click listeners
            btnPickLocation.setOnClickListener {
                try {
                    val intent = Intent(requireContext(), SavedLocationActivity::class.java)
                    savedLocationResultLauncher.launch(intent)
                } catch (e: Exception) {
                    Log.e("BankSampahFragment", "Error launching SavedLocationActivity: ${e.message}", e)
                    Toast.makeText(context, "Gagal membuka daftar lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            useCurrentLocationButton.setOnClickListener {
                try {
                    // Hapus marker hasil pick location dari annotation manager (jika ada)
                    pickedLocationMarker?.let {
                        pointAnnotationManager.delete(it)
                        pickedLocationMarker = null
                    }

                    // Hapus marker hasil pick location dari style (SymbolLayer dan GeoJsonSource)
                    mapView.getMapboxMap().getStyle { style ->
                        if (style.styleLayerExists("selected-location-layer")) {
                            style.removeStyleLayer("selected-location-layer")
                        }
                        if (style.styleSourceExists("selected-location-source")) {
                            style.removeStyleSource("selected-location-source")
                        }
                    }

                    // Aktifkan kembali location component
                    val locationComponent = mapView.location
                    locationComponent.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                    }

                    val indicatorListener = object : OnIndicatorPositionChangedListener {
                        override fun onIndicatorPositionChanged(point: Point) {
                            selectedLatitude = point.latitude()
                            selectedLongitude = point.longitude()

                            val cameraOptions = CameraOptions.Builder()
                                .center(point)
                                .zoom(15.0)
                                .build()
                            mapView.getMapboxMap().setCamera(cameraOptions)

                            locationComponent.removeOnIndicatorPositionChangedListener(this)
                        }
                    }

                    locationComponent.addOnIndicatorPositionChangedListener(indicatorListener)
                } catch (e: Exception) {
                    Log.e("BankSampahFragment", "Error using current location: ${e.message}", e)
                    Toast.makeText(context, "Gagal menggunakan lokasi saat ini: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // Setup Mapbox
            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
                try {
                    val vectorDrawable = resources.getDrawable(R.drawable.red_marker, null) as VectorDrawable
                    val bitmap = Bitmap.createBitmap(
                        vectorDrawable.intrinsicWidth,
                        vectorDrawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val annotationPlugin = mapView.annotations
                    pointAnnotationManager = annotationPlugin.createPointAnnotationManager()

                    val canvas = Canvas(bitmap)
                    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    vectorDrawable.draw(canvas)
                    style.addImage("marker-15", bitmap, true)

                    val vectorSource = VectorSource.Builder("bank-sampah-source")
                        .url("mapbox://tegar22006.ddvq3c4y")
                        .build()
                    style.addSource(vectorSource)

                    val symbolLayer = SymbolLayer("bank-sampah-layer", "bank-sampah-source")
                        .sourceLayer("data_TPS_mapbox_-_gabungan-5t7swm")
                        .iconImage("marker-15")
                        .iconAnchor(IconAnchor.BOTTOM)
                    style.addLayer(symbolLayer)

                    val cameraOptions = CameraOptions.Builder()
                        .center(Point.fromLngLat(107.5732, -6.9031))
                        .zoom(8.0)
                        .build()
                    mapView.getMapboxMap().setCamera(cameraOptions)

                    if (checkLocationPermission()) {
                        activateUserLocation()
                    } else {
                        requestLocationPermission()
                    }

                    mapView.gestures.addOnMapClickListener { point ->
                        val screenPoint = mapView.getMapboxMap().pixelForCoordinate(point)
                        mapView.getMapboxMap().queryRenderedFeatures(
                            RenderedQueryGeometry(screenPoint),
                            RenderedQueryOptions(listOf("bank-sampah-layer"), null)
                        ) { result ->
                            result.value?.firstOrNull()?.let { queriedRenderedFeature ->
                                val feature = queriedRenderedFeature.queriedFeature
                                feature?.let {
                                    val title = it.feature.getStringProperty("Title") ?: "Tidak diketahui"
                                    val address = it.feature.getStringProperty("Address") ?: "Alamat tidak tersedia"
                                    val latitude = it.feature.getNumberProperty("latitude")?.toDouble() ?: -6.9031
                                    val longitude = it.feature.getNumberProperty("longitude")?.toDouble() ?: 107.5732

                                    titleView.text = title
                                    addressView.text = address
                                    coordinateView.text = "Lat: $latitude, Lng: $longitude"
                                    showInfoPanelWithAnimation()

                                    selectedTpsLatitude = latitude
                                    selectedTpsLongitude = longitude
                                } ?: run {
                                    hideInfoPanelWithAnimation()
                                }
                            } ?: run {
                                hideInfoPanelWithAnimation()
                            }
                        }
                        true
                    }

                    navigateButton.setOnClickListener {
                        try {
                            val originLat = selectedLatitude ?: -1.0
                            val originLon = selectedLongitude ?: -1.0
                            val destinationLat = selectedTpsLatitude
                            val destinationLon = selectedTpsLongitude

                            if (originLat != -1.0 && originLon != -1.0 && destinationLat != null && destinationLon != null) {
                                startNavigation(originLat, originLon, destinationLat, destinationLon)
                            } else {
                                Toast.makeText(context, "Lokasi asal atau tujuan belum dipilih", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("BankSampahFragment", "Error starting navigation: ${e.message}", e)
                            Toast.makeText(context, "Gagal memulai navigasi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Tambahkan click listener untuk menutup panel info
                    infoPanel.setOnClickListener {
                        // Tidak melakukan apa-apa, hanya mencegah klik melalui panel
                    }
                } catch (e: Exception) {
                    Log.e("BankSampahFragment", "Error loading map style: ${e.message}", e)
                    Toast.makeText(context, "Gagal memuat peta: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            return view
        } catch (e: Exception) {
            Log.e("BankSampahFragment", "Error in onCreateView: ${e.message}", e)
            Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            // Fallback view jika terjadi error
            return inflater.inflate(R.layout.fragment_bank_sampah, container, false)
        }
    }

    private fun showInfoPanelWithAnimation() {
        try {
            // Pastikan panel info awalnya di luar layar (di bawah)
            infoPanel.translationY = 500f
            infoPanel.alpha = 0f
            infoPanel.visibility = View.VISIBLE

            // Animasi untuk menampilkan panel info dari bawah ke atas
            infoPanel.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        } catch (e: Exception) {
            Log.e("BankSampahFragment", "Error showing info panel: ${e.message}", e)
            // Fallback jika animasi gagal
            infoPanel.visibility = View.VISIBLE
        }
    }

    private fun hideInfoPanelWithAnimation() {
        try {
            // Animasi untuk menyembunyikan panel info ke bawah
            infoPanel.animate()
                .translationY(500f)
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    infoPanel.visibility = View.GONE
                }
                .start()
        } catch (e: Exception) {
            Log.e("BankSampahFragment", "Error hiding info panel: ${e.message}", e)
            // Fallback jika animasi gagal
            infoPanel.visibility = View.GONE
        }
    }

    private fun activateUserLocation() {
        try {
            val locationComponent = mapView.location
            locationComponent.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
            val indicatorListener = object : OnIndicatorPositionChangedListener {
                override fun onIndicatorPositionChanged(point: Point) {
                    // Update lokasi pengguna terkini
                    selectedLatitude = point.latitude()
                    selectedLongitude = point.longitude()

                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(14.0)
                            .build()
                    )
                    locationComponent.removeOnIndicatorPositionChangedListener(this)
                }
            }
            locationComponent.addOnIndicatorPositionChangedListener(indicatorListener)
        } catch (e: Exception) {
            Log.e("BankSampahFragment", "Error activating user location: ${e.message}", e)
        }
    }

    private fun checkLocationPermission(): Boolean {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun startNavigation(originLat: Double, originLon: Double, destLat: Double, destLon: Double) {
        try {
            val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$originLat,$originLon&destination=$destLat,$destLon")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            startActivity(mapIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Aplikasi Google Maps tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationInfo(latitude: Double, longitude: Double, address: String?) {
        try {
            Log.d("BankSampahFragment", "Updating location info: Lat=$latitude, Lng=$longitude, Address=$address")

            titleView.text = "Lokasi yang dipilih"
            coordinateView.text = "Lat: $latitude, Lng: $longitude"
            addressView.text = address ?: "Alamat tidak ditemukan"

            selectedLatitude = latitude
            selectedLongitude = longitude

            addMarkerToMap(latitude, longitude)

            val locationComponent = mapView.location
            locationComponent.updateSettings {
                enabled = false
                pulsingEnabled = false
            }

            // Show info panel
            showInfoPanelWithAnimation()
        } catch (e: Exception) {
            Log.e("BankSampahFragment", "Error updating location info: ${e.message}", e)
            Toast.makeText(context, "Gagal memperbarui informasi lokasi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMarkerToMap(latitude: Double, longitude: Double) {
        try {
            val point = Point.fromLngLat(longitude, latitude)
            val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.red_marker_1)
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 256, 256, false)

            mapView.getMapboxMap().getStyle { style ->
                style.addImage("red-marker-1", scaledBitmap, true)

                // Remove existing marker if any
                if (style.styleSourceExists("selected-location-source")) {
                    style.removeStyleLayer("selected-location-layer")
                    style.removeStyleSource("selected-location-source")
                }

                val symbolLayerSource = GeoJsonSource.Builder("selected-location-source")
                    .feature(Feature.fromGeometry(point))
                    .build()

                style.addSource(symbolLayerSource)

                val symbolLayer = SymbolLayer("selected-location-layer", "selected-location-source")
                    .iconImage("red-marker-1")
                    .iconAnchor(IconAnchor.BOTTOM)

                style.addLayer(symbolLayer)

                val cameraOptions = CameraOptions.Builder()
                    .center(point)
                    .zoom(14.0)
                    .build()

                mapView.getMapboxMap().setCamera(cameraOptions)
            }
        } catch (e: Exception) {
            Log.e("BankSampahFragment", "Error adding marker to map: ${e.message}", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activateUserLocation()
            } else {
                Toast.makeText(context, "Izin lokasi diperlukan untuk fitur ini", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

