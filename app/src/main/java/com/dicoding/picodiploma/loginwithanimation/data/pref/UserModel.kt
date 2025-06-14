package com.dicoding.picodiploma.loginwithanimation.data.pref

data class UserModel(
    val userId: String,  // Menambahkan userId
    val name: String,    // Menambahkan name
    val token: String,  // Menambahkan token
    val role: String,
    val isLogin: Boolean  // Status login

)
