package com.dicoding.picodiploma.loginwithanimation.view.user.password

import android.content.Intent
import android.os.Bundle
import android.view.WindowInsetsAnimation
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityForgotPasswordBinding
import com.dicoding.picodiploma.loginwithanimation.response.ForgotPasswordResponse
import com.dicoding.picodiploma.network.ApiClient
import retrofit2.Call
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                sendForgotPassword(email)
            } else {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnToChange.setOnClickListener {
            startActivity(Intent(this, ChangePasswordTokenActivity::class.java))
        }
    }

    private fun sendForgotPassword(email: String) {
        ApiClient.getApiServiceWithoutToken().forgotPassword(email).enqueue(object :
            retrofit2.Callback<ForgotPasswordResponse> {
            override fun onResponse(
                call: Call<ForgotPasswordResponse>,
                response: Response<ForgotPasswordResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, response.body()?.message ?: "Cek email Anda", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, "Email tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                Toast.makeText(this@ForgotPasswordActivity, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
