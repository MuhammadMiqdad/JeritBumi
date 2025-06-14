package com.dicoding.picodiploma.loginwithanimation.view.user.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.dicoding.picodiploma.loginwithanimation.databinding.FragmentDashboardBinding
import com.dicoding.picodiploma.loginwithanimation.view.user.ResultActivity
import com.dicoding.picodiploma.networkClassification.ApiClientClassification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentPhotoPath: String
    private var selectedImageFile: File? = null
    private var imageUri: Uri? = null  // Simpan URI gambar

    companion object {
        private const val REQUEST_GALLERY = 100
        private const val REQUEST_CAMERA = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.galleryButton.setOnClickListener {
            openGallery()
        }

        binding.cameraButton.setOnClickListener {
            openCamera()
        }

        binding.analyzeButton.setOnClickListener {
            selectedImageFile?.let {
                analyzeImage(it)
            } ?: Toast.makeText(requireContext(), "Pilih atau ambil gambar dulu", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity?.let {
            try {
                val photoFile = createImageFile()
                val photoURI: Uri = FileProvider.getUriForFile(
                    it,
                    "${it.packageName}.fileprovider",
                    photoFile
                )
                currentPhotoPath = photoFile.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, REQUEST_CAMERA)
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireActivity().externalCacheDir
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir!!)
    }

    private fun analyzeImage(file: File) {
        // Tampilkan progress bar dan nonaktifkan tombol
        showLoading(true)
        setButtonsEnabled(false)

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClientClassification.instance.classifyImage(body)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    setButtonsEnabled(true)
                    if (response.isSuccessful && response.body() != null) {
                        val resultText = response.body()!!.string()
                        val intent = Intent(requireContext(), ResultActivity::class.java)
                        intent.putExtra("result", resultText)
                        intent.putExtra("imageUri", imageUri.toString()) // Kirim imageUri ke ResultActivity
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengklasifikasikan gambar", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    setButtonsEnabled(true)
                    Toast.makeText(requireContext(), "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.analyzeButton.isEnabled = enabled
        binding.cameraButton.isEnabled = enabled
        binding.galleryButton.isEnabled = enabled

        // Ubah teks tombol analyze saat loading
        binding.analyzeButton.text = if (enabled) {
            getString(com.dicoding.picodiploma.loginwithanimation.R.string.analyze)
        } else {
            "Menganalisis..."
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_GALLERY -> {
                val imageUri: Uri? = data?.data
                if (imageUri != null) {
                    binding.previewImageView.setImageURI(imageUri)
                    selectedImageFile = getFileFromUri(imageUri)
                    this.imageUri = imageUri // Simpan imageUri
                }
            }

            REQUEST_CAMERA -> {
                val file = File(currentPhotoPath)
                if (file.exists()) {
                    binding.previewImageView.setImageURI(Uri.fromFile(file))
                    selectedImageFile = file
                    this.imageUri = Uri.fromFile(file) // Simpan imageUri
                }
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val contentResolver = requireActivity().contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = createImageFile()

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}