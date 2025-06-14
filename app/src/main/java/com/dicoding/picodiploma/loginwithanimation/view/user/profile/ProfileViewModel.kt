package com.dicoding.picodiploma.loginwithanimation.view.user.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.response.UserProfile
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> = _profile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getProfile()
                _profile.value = result
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to fetch profile", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
