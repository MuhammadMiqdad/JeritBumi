package com.dicoding.picodiploma.loginwithanimation.data

import android.content.Context
import android.content.SharedPreferences

class SavedLocationManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("saved_locations", Context.MODE_PRIVATE)

    fun saveLocation(label: String, lat: Double, lon: Double) {
        prefs.edit().apply {
            putString("${label}_lat", lat.toString())
            putString("${label}_lon", lon.toString())
            apply()
        }
    }

    fun getLocation(label: String): Pair<Double, Double>? {
        val lat = prefs.getString("${label}_lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("${label}_lon", null)?.toDoubleOrNull()

        return if (lat != null && lon != null) {
            Pair(lat, lon)
        } else {
            null
        }
    }

    fun getAllLabels(): List<String> {
        return prefs.all.keys.mapNotNull { key ->
            if (key.endsWith("_lat")) key.removeSuffix("_lat") else null
        }
    }

    fun removeLocation(label: String) {
        prefs.edit().apply {
            remove("${label}_lat")
            remove("${label}_lon")
            apply()
        }
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
