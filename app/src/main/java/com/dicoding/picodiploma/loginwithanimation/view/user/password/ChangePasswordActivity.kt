package com.dicoding.picodiploma.loginwithanimation.view.user.password

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityChangePasswordBinding
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.response.ChangePasswordRequest
import com.dicoding.picodiploma.loginwithanimation.response.ChangePasswordResponse
import com.dicoding.picodiploma.network.ApiClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var pref: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = UserPreference.getInstance(dataStore)

        setupPasswordToggle(binding.etOldPassword)
        setupPasswordToggle(binding.etNewPassword)
        setupPasswordToggle(binding.etRepeatPassword)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Change Password"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Log.d("ChangePasswordActivity", "Toolbar title: ${binding.toolbar.title}")
        Log.d("ChangePasswordActivity", "SupportActionBar title: ${supportActionBar?.title}")



        binding.btnChangePassword.setOnClickListener {
            val oldPass = binding.etOldPassword.text.toString()
            val newPass = binding.etNewPassword.text.toString()
            val repeatPass = binding.etRepeatPassword.text.toString()

            when {
                oldPass.isEmpty() || newPass.isEmpty() || repeatPass.isEmpty() -> {
                    Toast.makeText(this, "Field tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
                newPass.length < 8 || repeatPass.length < 8 -> {
                    Toast.makeText(this, "Password harus lebih dari 8 karakter", Toast.LENGTH_SHORT).show()
                }
                newPass != repeatPass -> {
                    Toast.makeText(this, "Kata sandi baru dan ulangi tidak cocok", Toast.LENGTH_SHORT).show()
                }
                oldPass == newPass -> {
                    Toast.makeText(this, "Kata sandi baru tidak boleh sama dengan yang lama", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    changePassword(oldPass, newPass)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordToggle(editText: EditText) {
        var isPasswordVisible = false

        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = editText.compoundDrawables[drawableEnd]
                if (drawable != null && event.rawX >= (editText.right - drawable.bounds.width() - editText.paddingEnd)) {
                    isPasswordVisible = !isPasswordVisible
                    val selection = editText.selectionEnd
                    if (isPasswordVisible) {
                        editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0)
                    } else {
                        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0)
                    }
                    editText.setSelection(selection)
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.etNewPassword.doAfterTextChanged {
            val pass = it.toString()
            if (pass.length < 8) {
                binding.etNewPassword.error = "Password harus lebih dari 8 karakter"
            } else {
                binding.etNewPassword.error = null
            }
        }

        binding.etRepeatPassword.doAfterTextChanged {
            val pass = it.toString()
            if (pass.length < 8) {
                binding.etRepeatPassword.error = "Password harus lebih dari 8 karakter"
            } else {
                binding.etRepeatPassword.error = null
            }
        }
    }

    private fun changePassword(oldPass: String, newPass: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            try {
                val token = pref.getSession().first().token
                val apiService = ApiClient.getApiService(token)
                val request = ChangePasswordRequest(oldPass, newPass)
                val response = apiService.changePassword(request)

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val message = response.body()?.output?.payload?.message ?: "Password berhasil diganti"
                    Toast.makeText(this@ChangePasswordActivity, message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val gson = Gson()
                            val errorResponse = gson.fromJson(errorBody, ChangePasswordResponse::class.java)
                            val errorMessage = errorResponse.output?.payload?.message ?: "Gagal mengganti password"
                            Toast.makeText(this@ChangePasswordActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@ChangePasswordActivity, "Gagal mengganti password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, "Gagal mengganti password", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: HttpException) {
                binding.progressBar.visibility = View.GONE
                if (e.code() == 401 || e.code() == 400) {
                    Toast.makeText(this@ChangePasswordActivity, "Password lama salah", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ChangePasswordActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@ChangePasswordActivity, "Terjadi kesalahan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}