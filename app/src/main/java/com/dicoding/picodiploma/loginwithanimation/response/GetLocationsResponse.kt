package com.dicoding.picodiploma.loginwithanimation.response

data class GetLocationsResponse(
    val message: String,
    val data: List<LocationData> // Ubah ini menjadi List<LocationData> untuk menyesuaikan dengan array yang ada di JSON
)
