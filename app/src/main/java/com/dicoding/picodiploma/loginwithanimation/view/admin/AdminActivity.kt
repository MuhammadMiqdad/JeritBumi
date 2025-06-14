package com.dicoding.picodiploma.loginwithanimation.view.admin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.response.PickupData
import com.dicoding.picodiploma.loginwithanimation.response.StatusBody
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginActivity
import com.dicoding.picodiploma.loginwithanimation.view.user.profile.ProfileActivity
import com.dicoding.picodiploma.network.ApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var infoPanelCard: CardView
    private lateinit var infoPanel: LinearLayout
    private lateinit var imageView: ImageView
    private lateinit var typeView: TextView
    private lateinit var addressView: TextView
    private lateinit var latLongView: TextView
    private lateinit var weightView: TextView
    private lateinit var scheduleView: TextView
    private lateinit var statusView: TextView
    private lateinit var btnAcceptOrder: Button
    private lateinit var btnCancelOrder: Button
    private lateinit var btnNavigate: Button
    private lateinit var btnNearestOrder: Button
    private lateinit var btnAllOrders: Button
    private lateinit var buttonContainer: LinearLayout
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var userPreference: UserPreference

    private val markerMap = mutableMapOf<PointAnnotation, List<PickupData>>()
    private var pickupList: List<PickupData> = emptyList()
    private var todayPickupList: List<PickupData> = emptyList() // New: filtered list for today
    private var selectedPickupId: Int = -1
    private var selectedPickupData: PickupData? = null
    private var currentUserLocation: Point? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val jatinangorLat = -6.9336
    private val jatinangorLon = 107.7719

    // Multiple date formatters to handle different possible formats
    private val dateFormatters = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    )

    private val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val todayCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val adminViewModel: AdminViewModel by viewModels {
        AdminViewModelFactory(
            UserRepository.getInstance(
                UserPreference.getInstance(applicationContext.dataStore),
                ApiClient.getApiService("")
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Admin Dashboard - Today's Orders"

        initializeViews()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermissionIfNeeded()

        setupMap()
        setupButtonInteractions()
        setupNavigationButton()
    }

    private fun initializeViews() {
        mapView = findViewById(R.id.mapView)
        infoPanelCard = findViewById(R.id.infoPanelCard)
        infoPanel = findViewById(R.id.infoPanel)
        imageView = findViewById(R.id.imageView)
        typeView = findViewById(R.id.typeView)
        addressView = findViewById(R.id.addressView)
        latLongView = findViewById(R.id.latLongView)
        weightView = findViewById(R.id.weightView)
        scheduleView = findViewById(R.id.scheduleView)
        statusView = findViewById(R.id.statusView)
        btnAcceptOrder = findViewById(R.id.btnAcceptOrder)
        btnCancelOrder = findViewById(R.id.btnCancelOrder)
        btnNearestOrder = findViewById(R.id.btnNearestOrder)
        btnAllOrders = findViewById(R.id.btnAllOrders)
        buttonContainer = findViewById(R.id.buttonContainer)

        userPreference = UserPreference.getInstance(applicationContext.dataStore)
    }

    private fun setupMap() {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            Log.d("AdminActivity", "Map style loaded successfully")

            // Set initial camera position
            mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(jatinangorLon, jatinangorLat))
                    .zoom(12.0)
                    .build()
            )

            // Initialize point annotation manager
            pointAnnotationManager = mapView.annotations.createPointAnnotationManager()

            // Setup marker click listener
            setupMarkerClickListener()

            // Fetch pickup requests
            fetchPickupRequests()
        }
    }

    private fun setupNavigationButton() {
        btnNavigate = Button(this).apply {
            text = "🧭 Navigate"
            setBackgroundColor(ContextCompat.getColor(this@AdminActivity, R.color.army_green))
            setTextColor(ContextCompat.getColor(this@AdminActivity, android.R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
                height = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
            }

            setOnClickListener {
                animateButtonClick(this) {
                    startNavigation()
                }
            }
        }

        // Find the buttons container in the info panel
        val buttonsLayout = infoPanel.findViewById<LinearLayout>(R.id.buttonsLayout)

        // Add navigation button above the accept/cancel buttons
        buttonsLayout.addView(btnNavigate, 0)
    }

    private fun setupButtonInteractions() {
        btnAcceptOrder.setOnClickListener {
            animateButtonClick(btnAcceptOrder) {
                updatePickupStatus("accepted")
                disableActionButtons()
            }
        }

        btnCancelOrder.setOnClickListener {
            animateButtonClick(btnCancelOrder) {
                updatePickupStatus("canceled")
                disableActionButtons()
            }
        }

        btnNearestOrder.setOnClickListener {
            Log.d("AdminActivity", "Nearest Orders button clicked")
            animateButtonClick(btnNearestOrder) {
                showNearestOrders()
            }
        }

        btnAllOrders.setOnClickListener {
            Log.d("AdminActivity", "All Today's Orders button clicked")
            animateButtonClick(btnAllOrders) {
                showAllTodayOrders()
            }
        }
    }

    private fun disableActionButtons() {
        // Disable buttons to prevent multiple clicks
        btnAcceptOrder.isEnabled = false
        btnCancelOrder.isEnabled = false

        // Make buttons invisible
        btnAcceptOrder.visibility = View.GONE
        btnCancelOrder.visibility = View.GONE
    }

    private fun showNearestOrders() {
        currentUserLocation?.let { location ->
            Log.d("AdminActivity", "Showing nearest today's orders from location: ${location.latitude()}, ${location.longitude()}")
            showMarkersWithinRadius(location, 2500.0)
            Toast.makeText(this, "Showing nearest today's orders within 2.5km", Toast.LENGTH_SHORT).show()
        } ?: run {
            Log.w("AdminActivity", "User location not available for nearest orders")
            Toast.makeText(this, "Location not available. Please enable location services.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAllTodayOrders() {
        Log.d("AdminActivity", "Showing all today's orders: ${todayPickupList.size} items")
        addMarkersToMap(todayPickupList)
        Toast.makeText(this, "Showing all ${todayPickupList.size} today's orders", Toast.LENGTH_SHORT).show()
    }

    private fun animateButtonClick(button: Button, action: () -> Unit) {
        button.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                button.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .withEndAction {
                        action()
                    }
                    .start()
            }
            .start()
    }

    private fun startNavigation() {
        try {
            val currentLocation = currentUserLocation
            val selectedPickup = selectedPickupData

            if (currentLocation != null && selectedPickup != null) {
                val originLat = currentLocation.latitude()
                val originLon = currentLocation.longitude()
                val destLat = selectedPickup.latitude
                val destLon = selectedPickup.longitude

                val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$originLat,$originLon&destination=$destLat,$destLon")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                try {
                    startActivity(mapIntent)
                } catch (e: Exception) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    startActivity(browserIntent)
                }
            } else {
                Toast.makeText(this, "Current location or destination not available", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("AdminActivity", "Error starting navigation: ${e.message}", e)
            Toast.makeText(this, "Failed to start navigation", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLocationPermissionIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            showUserCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showUserCurrentLocation()
        }
    }

    private fun showUserCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        mapView.location.updateSettings {
            this.enabled = true
            this.pulsingEnabled = true
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val point = Point.fromLngLat(location.longitude, location.latitude)
                currentUserLocation = point

                Log.d("AdminActivity", "User location updated: ${location.latitude}, ${location.longitude}")

                drawCircleArea(point, 2500.0)

                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(13.0)
                        .build()
                )

                mapView.location.updateSettings {
                    this.enabled = true
                }
            }
        }
    }

    private fun drawCircleArea(center: Point, radiusInMeters: Double) {
        val polygonManager = mapView.annotations.createPolygonAnnotationManager()
        val steps = 64
        val coordinates = mutableListOf<Point>()

        for (i in 0 until steps) {
            val angle = i * (360.0 / steps)
            val radians = Math.toRadians(angle)
            val dx = radiusInMeters * Math.cos(radians)
            val dy = radiusInMeters * Math.sin(radians)

            val earthRadius = 6371000.0
            val newLat = center.latitude() + (dy / earthRadius) * (180 / Math.PI)
            val newLng = center.longitude() + (dx / (earthRadius * Math.cos(Math.PI * center.latitude() / 180))) * (180 / Math.PI)

            coordinates.add(Point.fromLngLat(newLng, newLat))
        }
        coordinates.add(coordinates[0]) // Tambahkan kembali titik pertama untuk menutup polygon

        polygonManager.deleteAll()

        // Fixed: Use listOf(coordinates) to create List<List<Point>>
        val polygonOptions = PolygonAnnotationOptions()
            .withPoints(listOf(coordinates))  // Wrap coordinates in listOf to match expected type
            .withFillColor("#8888ff")
            .withFillOpacity(0.2)

        polygonManager.create(polygonOptions)
    }

    private fun showMarkersWithinRadius(center: Point, radiusMeters: Double) {
        // Modified: Use todayPickupList instead of pickupList
        val nearbyPickups = todayPickupList.filter {
            val results = FloatArray(1)
            Location.distanceBetween(
                center.latitude(), center.longitude(),
                it.latitude, it.longitude,
                results
            )
            results[0] <= radiusMeters
        }

        Log.d("AdminActivity", "Found ${nearbyPickups.size} today's pickups within ${radiusMeters}m radius")
        addMarkersToMap(nearbyPickups)
    }

    // Improved method: Filter pickup requests to only today's orders with better date parsing
    private fun filterTodayPickups(allPickups: List<PickupData>): List<PickupData> {
        Log.d("AdminActivity", "Filtering ${allPickups.size} pickups for today ($todayDate)")

        val filteredPickups = allPickups.filter { pickup ->
            val isToday = isPickupToday(pickup)
            Log.d("AdminActivity", "Pickup ${pickup.id} with schedule '${pickup.schedule}' - isToday: $isToday")
            isToday
        }

        Log.d("AdminActivity", "Filtered result: ${filteredPickups.size} pickups for today")
        return filteredPickups
    }

    // Improved method to check if a pickup is for today
    private fun isPickupToday(pickup: PickupData): Boolean {
        return try {
            // Method 1: Try to parse with different date formatters
            val scheduleDate = parseScheduleDate(pickup.schedule)
            if (scheduleDate != null) {
                val pickupCalendar = Calendar.getInstance().apply {
                    time = scheduleDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                return pickupCalendar.timeInMillis == todayCalendar.timeInMillis
            }

            // Method 2: If parsing fails, try string extraction
            val extractedDateString = extractDateFromSchedule(pickup.schedule)
            if (extractedDateString.isNotEmpty()) {
                return extractedDateString == todayDate
            }

            // Method 3: If both methods fail, include it (better to show extra than miss orders)
            Log.w("AdminActivity", "Could not parse date for pickup ${pickup.id}, including it to be safe")
            true

        } catch (e: Exception) {
            Log.w("AdminActivity", "Error checking if pickup ${pickup.id} is today: ${e.message}")
            // If we can't parse the date, include it to be safe
            true
        }
    }

    // New method to parse schedule date with multiple formatters
    private fun parseScheduleDate(schedule: String): Date? {
        for (formatter in dateFormatters) {
            try {
                return formatter.parse(schedule)
            } catch (e: Exception) {
                // Try next formatter
                continue
            }
        }

        // Try parsing just the date part if schedule contains time
        val datePart = schedule.split(" ")[0].split("T")[0]
        if (datePart != schedule) {
            for (formatter in dateFormatters) {
                try {
                    return formatter.parse(datePart)
                } catch (e: Exception) {
                    continue
                }
            }
        }

        return null
    }

    // Improved helper method to extract date from schedule string
    private fun extractDateFromSchedule(schedule: String): String {
        return try {
            Log.d("AdminActivity", "Extracting date from schedule: '$schedule'")

            // Handle different possible formats
            when {
                // ISO format: "2024-01-15T10:00:00" or "2024-01-15T10:00:00Z"
                schedule.contains("T") -> {
                    val datePart = schedule.split("T")[0]
                    Log.d("AdminActivity", "ISO format detected, extracted: '$datePart'")
                    datePart
                }
                // Standard format: "2024-01-15 10:00:00"
                schedule.contains(" ") -> {
                    val datePart = schedule.split(" ")[0]
                    Log.d("AdminActivity", "Space separated format detected, extracted: '$datePart'")
                    datePart
                }
                // Just date: "2024-01-15"
                schedule.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                    Log.d("AdminActivity", "Pure date format detected: '$schedule'")
                    schedule
                }
                // DD/MM/YYYY format
                schedule.matches(Regex("\\d{2}/\\d{2}/\\d{4}")) -> {
                    val parts = schedule.split("/")
                    val convertedDate = "${parts[2]}-${parts[1]}-${parts[0]}"
                    Log.d("AdminActivity", "DD/MM/YYYY format detected, converted to: '$convertedDate'")
                    convertedDate
                }
                // MM/DD/YYYY format (assuming this if no other pattern matches)
                schedule.matches(Regex("\\d{2}/\\d{2}/\\d{4}")) -> {
                    val parts = schedule.split("/")
                    val convertedDate = "${parts[2]}-${parts[0]}-${parts[1]}"
                    Log.d("AdminActivity", "MM/DD/YYYY format detected, converted to: '$convertedDate'")
                    convertedDate
                }
                // Try to extract first 10 characters if they look like a date
                schedule.length >= 10 && schedule.take(10).matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                    val datePart = schedule.take(10)
                    Log.d("AdminActivity", "First 10 chars look like date: '$datePart'")
                    datePart
                }
                else -> {
                    Log.w("AdminActivity", "Unknown date format in schedule: '$schedule'")
                    ""
                }
            }
        } catch (e: Exception) {
            Log.e("AdminActivity", "Error extracting date from schedule '$schedule': ${e.message}")
            ""
        }
    }

    private fun fetchPickupRequests() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val token = userPreference.getSession().first().token
                Log.d("AdminActivity", "Fetching pickup requests with token: ${token.take(10)}...")

                val response = ApiClient.getApiService(token).getAllPickupRequests()
                pickupList = response.data

                Log.d("AdminActivity", "Fetched ${pickupList.size} total pickup requests")

                // Log some sample schedules to understand the format
                pickupList.take(5).forEach { pickup ->
                    Log.d("AdminActivity", "Sample pickup ${pickup.id} schedule: '${pickup.schedule}'")
                }

                // Filter to get only today's pickups
                todayPickupList = filterTodayPickups(pickupList)

                Log.d("AdminActivity", "Filtered to ${todayPickupList.size} today's pickup requests")

                runOnUiThread {
                    // Display only today's pickups
                    addMarkersToMap(todayPickupList)

                    // Show toast with today's orders count
                    val message = if (todayPickupList.isEmpty()) {
                        "No orders found for today ($todayDate). Total orders: ${pickupList.size}"
                    } else {
                        "Loaded ${todayPickupList.size} orders for today ($todayDate)"
                    }

                    Toast.makeText(this@AdminActivity, message, Toast.LENGTH_LONG).show()

                    // If no today's orders, optionally show all orders for debugging
                    if (todayPickupList.isEmpty() && pickupList.isNotEmpty()) {
                        Log.d("AdminActivity", "No today's orders found, showing all orders for debugging")
                        addMarkersToMap(pickupList)
                        Toast.makeText(this@AdminActivity, "Showing all ${pickupList.size} orders (debug mode)", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminActivity", "Failed to fetch pickups: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@AdminActivity, "Failed to load pickup requests: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun addMarkersToMap(pickupRequests: List<PickupData>) {
        Log.d("AdminActivity", "Adding ${pickupRequests.size} markers to map")

        // Clear existing markers
        pointAnnotationManager.deleteAll()
        markerMap.clear()

        // Group pickups by location (latitude and longitude)
        val groupedPickups = pickupRequests.groupBy { Pair(it.latitude, it.longitude) }

        Log.d("AdminActivity", "Grouped pickups into ${groupedPickups.size} unique locations")

        for ((location, pickups) in groupedPickups) {
            try {
                // Use the status of the first pickup at this location for marker color
                val firstPickup = pickups.first()
                val drawableRes = when (firstPickup.status.lowercase()) {
                    "accepted" -> R.drawable.marker_accepted
                    "canceled" -> R.drawable.marker_canceled
                    else -> R.drawable.red_marker
                }

                val bitmap = vectorToBitmap(drawableRes)
                if (bitmap != null) {
                    val point = Point.fromLngLat(location.second, location.first)
                    val markerOptions = PointAnnotationOptions()
                        .withPoint(point)
                        .withIconImage(bitmap)

                    val marker = pointAnnotationManager.create(markerOptions)
                    markerMap[marker] = pickups // Store all pickups at this location

                    Log.d("AdminActivity", "Added marker for location with ${pickups.size} pickups at ${location.first}, ${location.second}")
                } else {
                    Log.w("AdminActivity", "Failed to create bitmap for marker")
                }
            } catch (e: Exception) {
                Log.e("AdminActivity", "Error adding marker for location ${location}: ${e.message}", e)
            }
        }

        Log.d("AdminActivity", "Total markers added: ${markerMap.size}")
    }

    private fun vectorToBitmap(drawableResId: Int): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(this, drawableResId)
            drawable?.let {
                val bitmap = Bitmap.createBitmap(
                    it.intrinsicWidth,
                    it.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                it.setBounds(0, 0, canvas.width, canvas.height)
                it.draw(canvas)
                bitmap
            }
        } catch (e: Exception) {
            Log.e("AdminActivity", "Error creating bitmap from drawable: ${e.message}", e)
            null
        }
    }

    private fun setupMarkerClickListener() {
        pointAnnotationManager.addClickListener { annotation ->
            Log.d("AdminActivity", "Marker clicked")

            val pickups = markerMap[annotation]
            if (pickups != null && pickups.isNotEmpty()) {
                Log.d("AdminActivity", "Found ${pickups.size} pickups for clicked marker")

                if (pickups.size == 1) {
                    // If there's only one pickup at this location, show it directly
                    showInfoPanel(pickups.first())
                } else {
                    // If there are multiple pickups, show a selection dialog
                    showPickupSelectionDialog(pickups)
                }
                return@addClickListener true
            } else {
                Log.w("AdminActivity", "No pickup data found for clicked marker")
                return@addClickListener false
            }
        }
    }

    private fun showPickupSelectionDialog(pickups: List<PickupData>) {
        // Create a dialog to select which pickup to view
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Multiple Orders at This Location")

        // Create list of items showing basic info about each pickup
        val items = pickups.map { pickup ->
            "ID: ${pickup.id} - Type: ${pickup.classificationType} - Status: ${pickup.status}"
        }.toTypedArray()

        builder.setItems(items) { dialog, which ->
            // Show the selected pickup
            showInfoPanel(pickups[which])
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showInfoPanel(request: PickupData) {
        Log.d("AdminActivity", "Showing info panel for pickup: ${request.id}")

        selectedPickupId = request.id
        selectedPickupData = request

        // Show both CardView and LinearLayout
        infoPanelCard.visibility = View.VISIBLE
        infoPanel.visibility = View.VISIBLE

        // Enhanced info panel animation
        infoPanelCard.translationY = 500f
        infoPanelCard.alpha = 0f

        infoPanelCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Load image with Glide
        Glide.with(this@AdminActivity)
            .load(Uri.parse(request.imageUrl))
            .centerCrop()
            .placeholder(R.drawable.red_marker)
            .error(R.drawable.red_marker)
            .into(imageView)

        // Update text content with emoji prefixes
        typeView.text = "📋 Type: ${request.classificationType}"
        addressView.text = "📍 Address: ${request.address}"
        latLongView.text = "🗺️ Coordinates: ${String.format("%.6f", request.latitude)}, ${String.format("%.6f", request.longitude)}"
        weightView.text = "⚖️ Weight: ${request.weight} kg"
        scheduleView.text = "🕒 Schedule: ${request.schedule}"
        statusView.text = "📊 Status: ${request.status}"

        // Update status text color based on status
        when (request.status.lowercase()) {
            "accepted" -> statusView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            "canceled" -> statusView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            else -> statusView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
        }

        // Set button state based on pickup status
        updateButtonState(request.status)
    }

    private fun hideInfoPanel() {
        infoPanelCard.animate()
            .translationY(500f)
            .alpha(0f)
            .setDuration(300)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                infoPanelCard.visibility = View.GONE
                infoPanel.visibility = View.GONE
            }
            .start()
    }

    private fun updatePickupStatus(newStatus: String) {
        if (selectedPickupId == -1) return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val token = userPreference.getSession().first().token
                val response = ApiClient.getApiService(token)
                    .updatePickupStatus(selectedPickupId, StatusBody(newStatus))

                runOnUiThread {
                    // Update the status text immediately
                    statusView.text = "📊 Status: $newStatus"

                    // Update status text color based on new status
                    when (newStatus.lowercase()) {
                        "accepted" -> statusView.setTextColor(ContextCompat.getColor(this@AdminActivity, android.R.color.holo_green_dark))
                        "canceled" -> statusView.setTextColor(ContextCompat.getColor(this@AdminActivity, android.R.color.holo_red_dark))
                        else -> statusView.setTextColor(ContextCompat.getColor(this@AdminActivity, android.R.color.holo_orange_dark))
                    }

                    // Update the selected pickup data
                    selectedPickupData?.let {
                        selectedPickupData = it.copy(status = newStatus)
                    }

                    // Update button state after status change
                    updateButtonState(newStatus)

                    Toast.makeText(this@AdminActivity, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()

                    // Refresh the map after a short delay
                    android.os.Handler(mainLooper).postDelayed({
                        refreshPickupMap()
                    }, 1000)
                }
            } catch (e: Exception) {
                Log.e("AdminActivity", "Failed to update status: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@AdminActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Add this new helper function to handle button states
    private fun updateButtonState(status: String) {
        when (status.lowercase()) {
            "accepted" -> {
                // Accept button is disabled (already accepted)
                btnAcceptOrder.visibility = View.VISIBLE
                btnAcceptOrder.isEnabled = false
                btnAcceptOrder.alpha = 0.5f
                btnAcceptOrder.text = "✓ Accepted"
                btnAcceptOrder.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))

                // Cancel button is enabled (can be canceled)
                btnCancelOrder.visibility = View.VISIBLE
                btnCancelOrder.isEnabled = false
                btnCancelOrder.alpha = 1f
                btnCancelOrder.text = "✗ Cancel"
                btnCancelOrder.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            }
            "canceled" -> {
                // Accept button is enabled (can be accepted again)
                btnAcceptOrder.visibility = View.VISIBLE
                btnAcceptOrder.isEnabled = false
                btnAcceptOrder.alpha = 1f
                btnAcceptOrder.text = "✓ Accept"
                btnAcceptOrder.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))

                // Cancel button is disabled (already canceled)
                btnCancelOrder.visibility = View.VISIBLE
                btnCancelOrder.isEnabled = false
                btnCancelOrder.alpha = 0.5f
                btnCancelOrder.text = "✗ Canceled"
                btnCancelOrder.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            }
            else -> {
                // Both buttons are enabled for pending status
                btnAcceptOrder.visibility = View.VISIBLE
                btnAcceptOrder.isEnabled = true
                btnAcceptOrder.alpha = 1f
                btnAcceptOrder.text = "✓ Accept"
                btnAcceptOrder.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))

                btnCancelOrder.visibility = View.VISIBLE
                btnCancelOrder.isEnabled = true
                btnCancelOrder.alpha = 1f
                btnCancelOrder.text = "✗ Cancel"
                btnCancelOrder.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            }
        }
    }
    // Add this new helper function
    private fun updateButtonVisibility(status: String) {
        when (status.lowercase()) {
            "accepted", "canceled" -> {
                // Hide buttons with animation
                btnAcceptOrder.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        btnAcceptOrder.visibility = View.GONE
                        btnAcceptOrder.isEnabled = false
                    }
                    .start()

                btnCancelOrder.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        btnCancelOrder.visibility = View.GONE
                        btnCancelOrder.isEnabled = false
                    }
                    .start()
            }
            else -> {
                // Show buttons if status is pending or other
                btnAcceptOrder.visibility = View.VISIBLE
                btnAcceptOrder.isEnabled = true
                btnAcceptOrder.alpha = 1f

                btnCancelOrder.visibility = View.VISIBLE
                btnCancelOrder.isEnabled = true
                btnCancelOrder.alpha = 1f
            }
        }
    }

    private fun refreshPickupMap() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val token = userPreference.getSession().first().token
                val response = ApiClient.getApiService(token).getAllPickupRequests()
                pickupList = response.data

                // Filter to get only today's pickups
                todayPickupList = filterTodayPickups(pickupList)

                runOnUiThread {
                    // Display only today's pickups
                    addMarkersToMap(todayPickupList)
                }
            } catch (e: Exception) {
                Log.e("AdminActivity", "Error refreshing pickup requests: ${e.message}", e)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            adminViewModel.logout()
            startActivity(Intent(this@AdminActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    override fun onBackPressed() {
        if (infoPanelCard.visibility == View.VISIBLE) {
            hideInfoPanel()
        } else {
            super.onBackPressed()
        }
    }

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
        super.onDestroy()
        mapView.onDestroy()
    }
}