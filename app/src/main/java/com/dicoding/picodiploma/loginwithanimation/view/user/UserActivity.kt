package com.dicoding.picodiploma.loginwithanimation.view.user

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityUserBinding
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginActivity
import com.dicoding.picodiploma.loginwithanimation.view.user.profile.ProfileActivity
import com.dicoding.picodiploma.network.ApiClient

class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            UserRepository.getInstance(
                UserPreference.getInstance(applicationContext.dataStore),
                ApiClient.getApiService("") // isi token kalau perlu
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_user) as NavHostFragment
        val navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_garbageBank
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Cek kalau ada extra buat navigasi
        intent?.getStringExtra("navigate_to")?.let { destination ->
            if (destination == "notifications") {
                navHostFragment.view?.post {
                    val classificationType = intent.getStringExtra("classificationType") ?: ""
                    val imageUri = intent.getStringExtra("imageUri") ?: ""  // Mengambil imageUri
                    val bundle = Bundle().apply {
                        putString("classificationType", classificationType)
                        putString("imageUri", imageUri)  // Menyimpan imageUri ke bundle
                    }
                    navController.navigate(R.id.navigation_notifications, bundle)  // Mengirim data ke NotificationsFragment
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // Aksi logout di sini
                userViewModel.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
