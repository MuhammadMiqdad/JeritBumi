package com.dicoding.picodiploma.loginwithanimation.view.user.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.view.user.password.ChangePasswordActivity
import com.dicoding.picodiploma.network.ApiClient
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val pref = UserPreference.getInstance(dataStore)
        val changePasswordText = findViewById<TextView>(R.id.tvChangePassword)

        changePasswordText.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        // Ambil token dari DataStore (suspend, jadi pakai coroutine)
        lifecycleScope.launch {
            pref.getSession().collect { user ->
                val token = user.token
                if (token.isNotEmpty()) {
                    val apiService = ApiClient.getApiService(token) // ← Ini pakai token
                    val repository = UserRepository.getInstance(pref, apiService)
                    val factory = ProfileViewModelFactory(repository)
                    viewModel = ViewModelProvider(this@ProfileActivity, factory)[ProfileViewModel::class.java]

                    val nameText = findViewById<TextView>(R.id.tv_name)
                    val emailText = findViewById<TextView>(R.id.email_value)
                    val phoneText = findViewById<TextView>(R.id.phone_value)
                    val addressText = findViewById<TextView>(R.id.address_value)

                    val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

                    viewModel.isLoading.observe(this@ProfileActivity) { isLoading ->
                        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }


                    viewModel.profile.observe(this@ProfileActivity) { profile ->
                        nameText.text = profile.name
                        emailText.text = profile.email
                        phoneText.text = profile.notelp
                        addressText.text = profile.alamat


                    }

                    viewModel.getProfile()
                }
            }
        }
    }
}
