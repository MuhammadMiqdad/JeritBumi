package com.dicoding.picodiploma.loginwithanimation.response

data class ProfileResponse(
    val message: String,
    val data: UserProfile
)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val notelp: String,
    val alamat: String,
    val role: String
)

