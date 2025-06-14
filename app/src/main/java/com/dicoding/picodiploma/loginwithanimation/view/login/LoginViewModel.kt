package com.dicoding.picodiploma.loginwithanimation.view.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.utils.TokenUtils
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<UserModel>()
    val loginResult: LiveData<UserModel> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = userRepository.login(email, password)
                val token = response.token

                if (!token.isNullOrEmpty()) {
                    val decoded: JSONObject? = TokenUtils.decodeJWT(token)

                    decoded?.let {
                        val userId = it.optString("id") // atau "userId" tergantung field JWT
                        val name = it.optString("name")
                        val emailFromToken = it.optString("email")
                        val role = it.optString("role")

                        val user = UserModel(
                            userId = userId,
                            name = name,
                            token = token,
                            role = role,
                            isLogin = true
                        )

                        userRepository.saveSession(user)
                        _loginResult.value = user
                    } ?: run {
                        _errorMessage.value = "Gagal decode token"
                    }
                } else {
                    val errorMsg = response.output?.payload?.message ?: "Login gagal. Coba lagi."
                    _errorMessage.value = errorMsg
                }

            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.localizedMessage}"
                Log.e("LoginViewModel", "Login error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            userRepository.saveSession(user)
        }
    }
}
