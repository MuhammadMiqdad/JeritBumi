package com.dicoding.picodiploma.loginwithanimation.view.user.password

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityChangePasswordTokenBinding
import com.dicoding.picodiploma.loginwithanimation.response.ChangePasswordTokenResponse
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginActivity
import com.dicoding.picodiploma.loginwithanimation.view.user.password.ForgotPasswordActivity
import com.dicoding.picodiploma.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class ChangePasswordTokenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordTokenBinding
    private lateinit var countDownTimer: CountDownTimer
    private val totalTimeMillis: Long = 2 * 60 * 1000 // 2 menit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordTokenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startCountdown()

        binding.btnChange.setOnClickListener {
            val token = binding.etToken.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()

            if (token.isNotEmpty() && newPassword.isNotEmpty()) {
                changePassword(token, newPassword)
            } else {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(totalTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                val formatted = String.format("Sisa waktu: %02d:%02d", minutes, seconds)
                binding.tvCountdown.text = formatted

                // Ganti warna teks berdasarkan sisa waktu
                when {
                    millisUntilFinished <= 30_000 -> { // < 30 detik
                        binding.tvCountdown.setTextColor(Color.RED)
                    }
                    millisUntilFinished <= 60_000 -> { // < 1 menit
                        binding.tvCountdown.setTextColor(Color.parseColor("#FFA500")) // Oranye
                    }
                    else -> {
                        binding.tvCountdown.setTextColor(Color.parseColor("#388E3C")) // Hijau
                    }
                }
            }

            override fun onFinish() {
                Toast.makeText(this@ChangePasswordTokenActivity, "Waktu habis, silakan minta token ulang", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ChangePasswordTokenActivity, ForgotPasswordActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnChange.isEnabled = !isLoading
    }

    private fun changePassword(token: String, newPassword: String) {
        showLoading(true) // tampilkan loading

        ApiClient.getApiServiceWithoutToken().changePasswordToken(token, newPassword)
            .enqueue(object : Callback<ChangePasswordTokenResponse> {
                override fun onResponse(
                    call: Call<ChangePasswordTokenResponse>,
                    response: Response<ChangePasswordTokenResponse>
                ) {
                    showLoading(false) // sembunyikan loading

                    if (response.isSuccessful) {
                        countDownTimer.cancel()
                        Toast.makeText(
                            this@ChangePasswordTokenActivity,
                            response.body()?.message ?: "Berhasil ubah password, silahkan login",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(this@ChangePasswordTokenActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@ChangePasswordTokenActivity, "Gagal ubah password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ChangePasswordTokenResponse>, t: Throwable) {
                    showLoading(false) // sembunyikan loading
                    Toast.makeText(this@ChangePasswordTokenActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}
