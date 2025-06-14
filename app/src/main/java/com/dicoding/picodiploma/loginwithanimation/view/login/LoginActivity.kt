package com.dicoding.picodiploma.loginwithanimation.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.view.admin.AdminActivity
import com.dicoding.picodiploma.loginwithanimation.view.signup.SignupActivity
import com.dicoding.picodiploma.loginwithanimation.view.story.StoryActivity
import com.dicoding.picodiploma.loginwithanimation.view.user.UserActivity
import com.dicoding.picodiploma.loginwithanimation.view.user.password.ForgotPasswordActivity
import com.dicoding.picodiploma.network.ApiClient

class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(
            UserRepository.getInstance(
                UserPreference.getInstance(applicationContext.dataStore),
                ApiClient.getApiService("")
            )
        )
    }

    private lateinit var edLoginEmail: EditText
    private lateinit var edLoginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupView()
        setupUI()
        setupAction()
        observeViewModel()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupUI() {
        edLoginEmail = findViewById(R.id.ed_login_email)
        edLoginPassword = findViewById(R.id.ed_login_password)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.progressBar)

        edLoginEmail.addTextChangedListener(emailTextWatcher)
        edLoginPassword.addTextChangedListener(passwordTextWatcher)
    }

    private fun setupAction() {
        loginButton.setOnClickListener {
            val email = edLoginEmail.text.toString()
            val password = edLoginPassword.text.toString()

            if (validateInput(email, password)) {
                loginViewModel.login(email, password)
            } else {
                showAlert("Error", "Email dan password tidak boleh kosong atau salah format.")
            }
        }

        val registerTextView: TextView = findViewById(R.id.registerTextView)
        val fullText = "Belum punya akun? Register"
        val spannableString = android.text.SpannableString(fullText)

        val clickableSpan = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                startActivity(intent)
            }
        }

        val startIndex = fullText.indexOf("Register")
        val endIndex = startIndex + "Register".length

        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        registerTextView.text = spannableString
        registerTextView.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        registerTextView.highlightColor = android.graphics.Color.TRANSPARENT

        val tvForgotPassword: TextView = findViewById(R.id.tvForgotPassword)
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

    }

    private fun observeViewModel() {
        loginViewModel.loginResult.observe(this) { user ->
            val welcomeMessage = "Selamat Datang ${user.name}! \nJerit Bumi siap menemani langkah pedulimu. \nAyo mulai aksi kecilmu, untuk dampak besar bagi bumi \uD83C\uDF0E✨"

            AlertDialog.Builder(this).apply {
                setTitle("Login Berhasil")
                setMessage(welcomeMessage)
                setCancelable(false)
                setPositiveButton("Lanjut") { _, _ ->
                    when (user.role.lowercase()) {
                        "admin" -> startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                        "user" -> startActivity(Intent(this@LoginActivity, UserActivity::class.java))
                        else -> startActivity(Intent(this@LoginActivity, StoryActivity::class.java))
                    }
                    finish()
                }
                create()
                show()
            }
        }

        loginViewModel.errorMessage.observe(this) { message ->
            showAlert("Login Gagal", message)
        }

        loginViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            loginButton.isEnabled = !isLoading
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edLoginEmail.error = "Email tidak valid"
            isValid = false
        }
        if (password.isEmpty() || password.length < 8) {
            edLoginPassword.error = "Password minimal 8 karakter"
            isValid = false
        }
        return isValid
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun playAnimation() {
        val imageView: View = findViewById(R.id.imageView)
        val titleTextView: View = findViewById(R.id.titleTextView)
        val messageTextView: View = findViewById(R.id.messageTextView)
        val emailTextView: View = findViewById(R.id.emailTextView)
        val emailEditTextLayout: View = findViewById(R.id.emailEditTextLayout)
        val passwordTextView: View = findViewById(R.id.passwordTextView)
        val passwordEditTextLayout: View = findViewById(R.id.passwordEditTextLayout)
        val loginButton: View = findViewById(R.id.loginButton)

        ObjectAnimator.ofFloat(imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val fadeInDuration = 100L
        AnimatorSet().apply {
            playSequentially(
                ObjectAnimator.ofFloat(titleTextView, View.ALPHA, 1f).setDuration(fadeInDuration),
                ObjectAnimator.ofFloat(messageTextView, View.ALPHA, 1f).setDuration(fadeInDuration),
                ObjectAnimator.ofFloat(emailTextView, View.ALPHA, 1f).setDuration(fadeInDuration),
                ObjectAnimator.ofFloat(emailEditTextLayout, View.ALPHA, 1f).setDuration(fadeInDuration),
                ObjectAnimator.ofFloat(passwordTextView, View.ALPHA, 1f).setDuration(fadeInDuration),
                ObjectAnimator.ofFloat(passwordEditTextLayout, View.ALPHA, 1f).setDuration(fadeInDuration),
                ObjectAnimator.ofFloat(loginButton, View.ALPHA, 1f).setDuration(fadeInDuration)
            )
        }.start()
    }

    private val emailTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            edLoginEmail.error =
                if (!s.isNullOrEmpty() && !Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    "Format email tidak valid"
                } else null
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
    }

    private val passwordTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            edLoginPassword.error =
                if (!s.isNullOrEmpty() && s.length < 8) {
                    "Password minimal 8 karakter"
                } else null
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
    }
}
