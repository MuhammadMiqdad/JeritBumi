package com.dicoding.picodiploma.loginwithanimation.response

data class PickupResponse(
    val message: String,
    val data: PickupData
)

data class PickupData(
    val id: Int,
    val imageUrl: String,
    val classificationType: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val weight: Int,
    val schedule: String,
    val status: String
)

