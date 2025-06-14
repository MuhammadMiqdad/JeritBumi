package com.dicoding.picodiploma.loginwithanimation.view.user.ui.notifications

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.databinding.FragmentNotificationsBinding
import com.dicoding.picodiploma.loginwithanimation.response.GetLocationsResponse
import com.dicoding.picodiploma.loginwithanimation.response.LocationData
import com.dicoding.picodiploma.loginwithanimation.response.PickupRequestBody
import com.dicoding.picodiploma.network.ApiClient

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreference: UserPreference
    private var locations: List<LocationData> = emptyList()
    private var weight: Int = 3
    private var selectedChip: Chip? = null
    private var selectedLocationIndex: Int = -1
    private var isAnyScheduleAvailable: Boolean = false

    // Tambahan untuk cek state form
    private var hasClassification: Boolean = false
    private var hasValidWeight: Boolean = true // default true karena weight minimal 3
    private var hasSelectedLocation: Boolean = false
    private var hasSelectedSchedule: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val classificationType = arguments?.getString("classificationType")
        val imageUri = arguments?.getString("imageUri")

        // Set default state - kosong semua
        setupDefaultState(classificationType, imageUri)

        // Setup komponen UI
        setupLocationDropdown()
        setupWeightControls()
        setupUserSession()
        setupHistoryButton(imageUri)

        disablePastSchedules()
        setupScheduleChips()
        updateWeightDisplay()

        return root
    }

    private fun setupDefaultState(classificationType: String?, imageUri: String?) {
        // Reset semua ke state default/kosong
        hasClassification = !classificationType.isNullOrEmpty()
        hasValidWeight = true // weight default 3, sudah valid
        hasSelectedLocation = false
        hasSelectedSchedule = false

        // Set classification atau default message
        if (!classificationType.isNullOrEmpty()) {
            binding.textNotifications.text = classificationType
            hasClassification = true
        } else {
            binding.textNotifications.text = "Belum ada klasifikasi sampah"
            hasClassification = false
        }

        // Set image atau default placeholder
        if (!imageUri.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(Uri.parse(imageUri))
                .into(binding.imageClassification)
        } else {
            binding.imageClassification.setImageResource(android.R.drawable.ic_menu_report_image)
        }

        // Reset weight ke default
        weight = 3
        updateWeightDisplay()

        // Reset location selection
        selectedLocationIndex = -1
        clearLocationDetails()
        binding.spinnerLocationsText.setText("", false)

        // Reset schedule selection
        binding.radioGroupSchedule.clearCheck()
        selectedChip = null

        // Update state awal tombol order
        updateOrderButtonState()
    }

    private fun setupWeightControls() {
        binding.buttonMinus.setOnClickListener {
            if (weight > 3) {
                weight--
                updateWeightDisplay()
                updateOrderButtonState()
            }
        }

        binding.buttonPlus.setOnClickListener {
            weight++
            updateWeightDisplay()
            updateOrderButtonState()
        }
    }

    private fun setupUserSession() {
        userPreference = UserPreference.getInstance(requireContext().dataStore)
        lifecycleScope.launch {
            userPreference.getSession().collect { user ->
                val token = user.token
                if (token.isNotEmpty()) {
                    fetchSavedLocations(token)
                    val classificationType = arguments?.getString("classificationType")
                    val imageUri = arguments?.getString("imageUri")
                    setupOrderButton(token, classificationType, imageUri)
                }
            }
        }
    }

    private fun setupHistoryButton(imageUri: String?) {
        binding.btnHistory.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            intent.putExtra("imageUri", imageUri)
            startActivity(intent)
        }
    }

    private fun setupLocationDropdown() {
        binding.spinnerLocations.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                android.util.Log.d("NotificationsFragment", "Spinner selected position: $position")
                updateLocationDetails(position)
                updateOrderButtonState()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                clearLocationDetails()
                updateOrderButtonState()
            }
        }

        binding.spinnerLocationsText.setOnItemClickListener { _, _, position, _ ->
            android.util.Log.d("NotificationsFragment", "AutoComplete selected position: $position")
            binding.spinnerLocations.setSelection(position)
            updateLocationDetails(position)
            updateOrderButtonState()
        }
    }

    private fun updateLocationDetails(position: Int) {
        if (locations.isNotEmpty() && position >= 0 && position < locations.size) {
            selectedLocationIndex = position
            hasSelectedLocation = true
            val selectedLocation = locations[position]

            binding.textAddress.text = "Alamat: ${selectedLocation.address}"
            binding.textCoordinates.text =
                "Latitude: ${selectedLocation.latitude} Longitude: ${selectedLocation.longitude}"

            binding.spinnerLocationsText.setText(selectedLocation.label, false)
        } else {
            clearLocationDetails()
        }
    }

    private fun clearLocationDetails() {
        selectedLocationIndex = -1
        hasSelectedLocation = false
        binding.textAddress.text = "Alamat: -"
        binding.textCoordinates.text = "Latitude: - Longitude: -"
    }

    private fun setupScheduleChips() {
        isAnyScheduleAvailable = false

        binding.radioGroupSchedule.setOnCheckedChangeListener { group, checkedId ->
            val chips = listOf(binding.radio08, binding.radio10, binding.radio13, binding.radio15, binding.radio1830)
            for (chip in chips) {
                if (chip.isEnabled) {
                    chip.chipStrokeWidth = 2f
                }
            }
            selectedChip = group.findViewById(checkedId)
            hasSelectedSchedule = selectedChip != null
            updateOrderButtonState()
        }

        // Cek jadwal yang tersedia tapi JANGAN auto-select
        val chips = listOf(binding.radio08, binding.radio10, binding.radio13, binding.radio15, binding.radio1830)
        for (chip in chips) {
            if (chip.isEnabled) {
                isAnyScheduleAvailable = true
                // HAPUS auto-selection, biarkan user memilih sendiri
            }
        }

        // Reset selection state
        hasSelectedSchedule = false
        selectedChip = null

        if (!isAnyScheduleAvailable) {
            binding.scheduleErrorMessage.visibility = View.VISIBLE
            binding.scheduleErrorMessage.text = "Tidak ada jadwal penjemputan yang tersedia hari ini. Silakan coba lagi besok."
        } else {
            binding.scheduleErrorMessage.visibility = View.GONE
        }

        updateOrderButtonState()
    }

    private fun setupOrderButton(token: String, classificationType: String?, imageUri: String?) {
        binding.btnOrder.setOnClickListener {
            if (!isFormComplete()) {
                showFormValidationMessage()
                return@setOnClickListener
            }

            val selectedSchedule = getSelectedSchedule()

            if (selectedLocationIndex != -1 && selectedLocationIndex < locations.size && selectedSchedule != null) {
                val selectedLocation = locations[selectedLocationIndex]
                val requestBody = PickupRequestBody(
                    imageUrl = imageUri ?: "",
                    classificationType = classificationType ?: "Tidak diketahui",
                    address = selectedLocation.address,
                    latitude = selectedLocation.latitude,
                    longitude = selectedLocation.longitude,
                    weight = weight,
                    schedule = selectedSchedule
                )

                // Tampilkan loading state
                showLoading(true)

                lifecycleScope.launch {
                    try {
                        val response = ApiClient.getApiService(token).createPickupRequest(requestBody)

                        // Sembunyikan loading
                        showLoading(false)

                        Toast.makeText(requireContext(), "Pesanan berhasil dikirim!", Toast.LENGTH_LONG).show()
                        resetUI()
                    } catch (e: Exception) {
                        // Sembunyikan loading
                        showLoading(false)

                        Toast.makeText(requireContext(), "Gagal mengirim pesanan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                showFormValidationMessage()
            }
        }

        updateOrderButtonState()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            // Tampilkan progress bar dan disable tombol
            binding.progressBar.visibility = View.VISIBLE
            binding.btnOrder.isEnabled = false
            binding.btnOrder.text = "Memproses..."
            binding.btnOrder.alpha = 0.7f
            binding.btnHistory.isEnabled = false
            binding.btnHistory.alpha = 0.7f
        } else {
            // Sembunyikan progress bar dan enable tombol
            binding.progressBar.visibility = View.GONE
            binding.btnOrder.isEnabled = true
            binding.btnHistory.isEnabled = true
            binding.btnHistory.alpha = 1.0f
            updateOrderButtonState() // Restore button state
        }
    }

    private fun isFormComplete(): Boolean {
        return hasClassification &&
                hasValidWeight &&
                hasSelectedLocation &&
                hasSelectedSchedule &&
                isAnyScheduleAvailable
    }

    private fun updateOrderButtonState() {
        val isComplete = isFormComplete()
        binding.btnOrder.isEnabled = isComplete
        binding.btnOrder.alpha = if (isComplete) 1.0f else 0.5f

        // Update text tombol berdasarkan state
        if (isComplete) {
            binding.btnOrder.text = "Pesan Pengambilan"
        } else {
            binding.btnOrder.text = "Lengkapi Form"
        }
    }

    private fun showFormValidationMessage() {
        val missingFields = mutableListOf<String>()

        if (!hasClassification) {
            missingFields.add("klasifikasi sampah")
        }
        if (!hasSelectedLocation) {
            missingFields.add("lokasi pengambilan")
        }
        if (!hasSelectedSchedule) {
            missingFields.add("jadwal pengambilan")
        }
        if (!isAnyScheduleAvailable) {
            Toast.makeText(requireContext(),
                "Tidak ada jadwal penjemputan yang tersedia hari ini. Silakan coba lagi besok.",
                Toast.LENGTH_LONG).show()
            return
        }

        if (missingFields.isNotEmpty()) {
            val message = if (missingFields.size == 1) {
                "Silakan pilih ${missingFields[0]}."
            } else {
                "Form harus diisi lengkap. Silakan pilih: ${missingFields.joinToString(", ")}."
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getSelectedSchedule(): String? {
        return selectedChip?.text?.toString()
    }

    private fun resetUI() {
        // Reset ke state default kosong
        binding.spinnerLocations.setSelection(0)
        binding.spinnerLocationsText.setText("", false)
        clearLocationDetails()
        binding.radioGroupSchedule.clearCheck()
        selectedChip = null
        hasSelectedSchedule = false
        weight = 3
        updateWeightDisplay()
        setupScheduleChips()
        updateOrderButtonState()
    }

    private fun updateWeightDisplay() {
        binding.textWeightValue.text = weight.toString()
        hasValidWeight = weight >= 3
        val isEnabled = weight > 3
        binding.buttonMinus.isEnabled = isEnabled
        binding.buttonMinus.alpha = if (isEnabled) 1.0f else 0.5f
    }

    private fun disablePastSchedules() {
        val currentTime = Calendar.getInstance()
        val scheduleMap = mapOf(
            binding.radio08 to "08:00",
            binding.radio10 to "10:00",
            binding.radio13 to "13:00",
            binding.radio15 to "15:00",
            binding.radio1830 to "19:30"
        )

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        var allDisabled = true

        for ((radioButton, timeStr) in scheduleMap) {
            val scheduleTime = Calendar.getInstance()
            val date = sdf.parse(timeStr)
            date?.let {
                scheduleTime.time = it
                scheduleTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR))
                scheduleTime.set(Calendar.MONTH, currentTime.get(Calendar.MONTH))
                scheduleTime.set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH))

                if (currentTime.after(scheduleTime)) {
                    radioButton.isEnabled = false
                    radioButton.isClickable = false
                    radioButton.alpha = 0.5f
                    radioButton.chipStrokeWidth = 1f
                } else {
                    allDisabled = false
                }
            }
        }

        isAnyScheduleAvailable = !allDisabled
    }

    private fun fetchSavedLocations(token: String) {
        android.util.Log.d("NotificationsFragment", "Fetching locations with token: ${token.take(10)}...")

        val authHeader = "Bearer $token"

        ApiClient.getApiService(token).getCustomLocations(authHeader)
            .enqueue(object : retrofit2.Callback<GetLocationsResponse> {
                override fun onResponse(
                    call: retrofit2.Call<GetLocationsResponse>,
                    response: retrofit2.Response<GetLocationsResponse>
                ) {
                    android.util.Log.d("NotificationsFragment", "Response code: ${response.code()}")
                    android.util.Log.d("NotificationsFragment", "Response body: ${response.body()}")

                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        locations = responseBody.data ?: emptyList()

                        android.util.Log.d("NotificationsFragment", "Locations loaded: ${locations.size}")

                        if (locations.isNotEmpty()) {
                            locations.forEachIndexed { index, location ->
                                android.util.Log.d("NotificationsFragment",
                                    "Location $index: ${location.label} - ${location.address}")
                            }

                            setupLocationAdapters()
                            // JANGAN auto-select lokasi pertama, biarkan kosong
                            clearLocationDetails()
                            binding.locationErrorMessage.visibility = View.GONE
                        } else {
                            showLocationError("Tidak ada lokasi tersimpan. Silakan tambahkan lokasi terlebih dahulu.")
                        }
                    } else {
                        val errorMsg = "Gagal memuat lokasi. Code: ${response.code()}, Message: ${response.message()}"
                        android.util.Log.e("NotificationsFragment", errorMsg)
                        showLocationError(errorMsg)
                    }
                    updateOrderButtonState()
                }

                override fun onFailure(call: retrofit2.Call<GetLocationsResponse>, t: Throwable) {
                    val errorMsg = "Error memuat lokasi: ${t.message}"
                    android.util.Log.e("NotificationsFragment", "Error loading locations", t)
                    showLocationError(errorMsg)
                    updateOrderButtonState()
                }
            })
    }

    private fun setupLocationAdapters() {
        try {
            val labels = locations.map { it.label }
            android.util.Log.d("NotificationsFragment", "Setting up adapters with labels: $labels")

            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerLocations.adapter = spinnerAdapter

            val dropdownAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, labels)
            binding.spinnerLocationsText.setAdapter(dropdownAdapter)

            // JANGAN set initial text, biarkan kosong agar user harus memilih
            binding.spinnerLocationsText.setText("", false)
            clearLocationDetails()
            android.util.Log.d("NotificationsFragment", "Location adapters setup complete - no initial selection")

        } catch (e: Exception) {
            android.util.Log.e("NotificationsFragment", "Error setting up location adapters", e)
            showLocationError("Error setting up location list: ${e.message}")
        }
    }

    private fun showLocationError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        binding.locationErrorMessage.visibility = View.VISIBLE
        binding.locationErrorMessage.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}