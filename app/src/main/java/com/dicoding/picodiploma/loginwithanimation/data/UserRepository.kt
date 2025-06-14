package com.dicoding.picodiploma.loginwithanimation.data

import android.util.Log
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.response.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.response.RegisterResponse
import com.dicoding.picodiploma.loginwithanimation.response.UserProfile
import com.dicoding.picodiploma.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    suspend fun register(name: String, email: String, password: String, notelp: String, alamat: String, role: String): RegisterResponse {
        return apiService.register(name, email, password, notelp, alamat, role)
    }

    suspend fun login(email: String, password: String): LoginResponse {
        try {
            val response = apiService.login(email, password)
            Log.d("UserRepository", "Login response: $response")
            return response
        } catch (e: Exception) {
            Log.e("UserRepository", "Login failed", e)
            // Pastikan exception dilemparkan ulang agar bisa ditangani ViewModel
            throw e
        }
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun getProfile(): UserProfile {
        val session = userPreference.getSession().first() // pakai .first() buat ambil token
        val token = session.token
        val response = apiService.getProfile("Bearer $token")
        return response.data
    }


    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService).also { instance = it }
            }
    }
}
