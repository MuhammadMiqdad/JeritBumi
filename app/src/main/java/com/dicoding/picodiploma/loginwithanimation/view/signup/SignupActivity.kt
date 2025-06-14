package com.dicoding.picodiploma.loginwithanimation.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivitySignupBinding
import com.dicoding.picodiploma.network.ApiClient

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    private val signupViewModel: SignupViewModel by viewModels {
        SignupViewModelFactory(
            UserRepository.getInstance(
                UserPreference.getInstance(applicationContext.dataStore),
                ApiClient.getApiService("")
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        playAnimation()

        signupViewModel.registerResult.observe(this) { response ->
            // Hide progress bar when response is received
            binding.progressBar.visibility = View.GONE

            Log.d("SignupResponse", "Response: $response")

            // Periksa apakah response tidak null dan memiliki nilai success yang benar
            if (response != null) {
                // Handle success response
                if (response.success is Boolean && response.success) {
                    // Jika berhasil, tampilkan dialog sukses
                    showSuccessDialog(binding.edRegisterEmail.text.toString())
                } else if (response.output != null) {
                    // Jika gagal, ambil pesan error dari output
                    val errorMessage = response.output?.payload?.message
                        ?: "Terjadi kesalahan saat mendaftar. Coba lagi."
                    showErrorDialog(errorMessage)
                } else {
                    // Tangani kasus kesalahan lain jika response tidak sesuai dengan yang diharapkan
                    showErrorDialog("Terjadi kesalahan. Coba lagi.")
                }
            } else {
                // Jika response null, tampilkan error umum
                showErrorDialog("Terjadi kesalahan saat mendaftar. Coba lagi.")
            }
        }
    }



    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        val nameEditText = binding.edRegisterName
        val emailEditText = binding.edRegisterEmail
        val passwordEditText = binding.edRegisterPassword
        val notelpEditText = binding.edRegisterNoTelp
        val alamatEditText = binding.edRegisterAlamat
        val radioGroup = findViewById<RadioGroup>(R.id.rgRole)

        // Add validation
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.emailEditTextLayout.error = "Email tidak valid"
                } else {
                    binding.emailEditTextLayout.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.length < 8) {
                    binding.passwordEditTextLayout.error = "Password harus lebih dari 8 karakter"
                } else {
                    binding.passwordEditTextLayout.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.signupButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val notelp = notelpEditText.text.toString()
            val alamat = alamatEditText.text.toString()
            val selectedRoleId = radioGroup.checkedRadioButtonId
            val selectedRoleButton = findViewById<RadioButton>(selectedRoleId)
            val role = selectedRoleButton.tag.toString() // Ambil nilai 1 atau 2

            // Validate user input
            if (name.isEmpty()) {
                binding.nameEditTextLayout.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            } else {
                binding.nameEditTextLayout.error = null
            }

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEditTextLayout.error = "Email tidak valid"
                return@setOnClickListener
            } else {
                binding.emailEditTextLayout.error = null
            }

            if (password.isEmpty() || password.length < 8) {
                binding.passwordEditTextLayout.error = "Password harus lebih dari 8 karakter"
                return@setOnClickListener
            } else {
                binding.passwordEditTextLayout.error = null
            }

            // Show progress bar while registering
            binding.progressBar.visibility = View.VISIBLE

            // Start registration process
            signupViewModel.register(name, email, password, notelp, alamat, role)
        }
    }

    private fun showSuccessDialog(email: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Yeah!")
            setMessage("Akun dengan $email sudah jadi nih. Cek verifikasi email yang sudah saya berikan untuk melengkapi registrasi.")
            setPositiveButton("Lanjut") { _, _ -> finish() }
            create()
            show()
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(message)
            setPositiveButton("Tutup", null)
            create()
            show()
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val nameTextView = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(100)
        val nameEditTextLayout = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val emailTextView = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                nameTextView,
                nameEditTextLayout,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                signup
            )
            startDelay = 100
        }.

        start()
    }

    fun onSignupClicked(view: View) {
        Toast.makeText(this, "Tombol Daftar ditekan!", Toast.LENGTH_SHORT).show()

    }
}
