package com.dicoding.picodiploma.loginwithanimation.response

data class SaveLocationRequest(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val label: String
)
