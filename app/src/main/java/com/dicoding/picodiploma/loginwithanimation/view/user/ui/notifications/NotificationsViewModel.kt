package com.dicoding.picodiploma.loginwithanimation.view.user.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is marketplace Fragment"
    }
    val text: LiveData<String> = _text
}