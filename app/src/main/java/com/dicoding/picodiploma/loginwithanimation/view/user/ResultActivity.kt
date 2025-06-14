package com.dicoding.picodiploma.loginwithanimation.view.user

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityResultBinding
import org.json.JSONObject

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private var classificationType: String? = null
    private var imageUri: String? = null  // Simpan imageUri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Membuat TextView dapat di-scroll jika kontennya panjang
        binding.resultTextView.movementMethod = ScrollingMovementMethod()

        val rawResult = intent.getStringExtra("result") ?: "Tidak ada hasil klasifikasi"
        imageUri = intent.getStringExtra("imageUri")  // Ambil imageUri dari intent

        val cleanedText = try {
            val jsonObject = JSONObject(rawResult)
            var resultText = jsonObject.getString("result")

            resultText = resultText
                .replace("\\n", "\n")
                .replace("**", "")

            resultText
        } catch (e: Exception) {
            rawResult
        }

        binding.resultTextView.text = cleanedText

        // Parsing Jenis Klasifikasi
        classificationType = extractClassification(cleanedText)

        // Mengatur warna tombol sesuai dengan status enabled
        updateButtonAppearance(binding.nextButton.isEnabled)

        binding.checkboxKg.setOnCheckedChangeListener { _, isChecked ->
            binding.nextButton.isEnabled = isChecked
            updateButtonAppearance(isChecked)
        }

        binding.nextButton.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java).apply {
                putExtra("navigate_to", "notifications")
                putExtra("classificationType", classificationType)
                putExtra("imageUri", imageUri)  // Kirim imageUri ke NotificationsFragment
            }
            startActivity(intent)
            finish()
        }
    }

    private fun updateButtonAppearance(isEnabled: Boolean) {
        if (isEnabled) {
            binding.nextButton.alpha = 1.0f
        } else {
            binding.nextButton.alpha = 0.6f
        }
    }

    private fun extractClassification(text: String): String? {
        val regex = Regex("Jenis Klasifikasi Sampah:\\s*(.*)")
        val match = regex.find(text)
        return match?.groups?.get(1)?.value?.trim()
    }
}
