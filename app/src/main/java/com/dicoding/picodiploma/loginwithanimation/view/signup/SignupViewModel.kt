package com.dicoding.picodiploma.loginwithanimation.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.response.RegisterResponse
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import kotlinx.coroutines.launch

class SignupViewModel(
    private val userRepository: UserRepository // Menyuntikkan UserRepository
) : ViewModel() {

    // LiveData untuk menampung hasil pendaftaran
    private val _registerResult = MutableLiveData<RegisterResponse?>()
    val registerResult: LiveData<RegisterResponse?> get() = _registerResult

    // Method untuk melakukan registrasi

    fun register(name: String, email: String, password: String, notelp: String, alamat: String, role: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.register(name, email, password, notelp, alamat, role)
                _registerResult.value = response
            }catch (e: Exception) {
                // Menangani error jika ada (misalnya, masalah jaringan)
                _registerResult.value = RegisterResponse(true, "Terjadi kesalahan: ${e.message}")
            }
        }
    }
}
