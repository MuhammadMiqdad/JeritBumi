package com.dicoding.picodiploma.loginwithanimation.response

data class PickupRequestBody(
    val imageUrl: String,
    val classificationType: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val weight: Int,
    val schedule: String
)

