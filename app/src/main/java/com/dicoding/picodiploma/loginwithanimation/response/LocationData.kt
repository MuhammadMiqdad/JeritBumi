package com.dicoding.picodiploma.loginwithanimation.response

data class LocationData(
    val id: Int,
    val label: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val user_id: Int,
    val createdAt: String,
    val updatedAt: String
)

