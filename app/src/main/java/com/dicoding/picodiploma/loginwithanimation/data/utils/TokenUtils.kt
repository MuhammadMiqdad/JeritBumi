package com.dicoding.picodiploma.loginwithanimation.data.utils

import android.util.Base64
import org.json.JSONObject

object TokenUtils {
    fun decodeJWT(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val json = String(decodedBytes, Charsets.UTF_8)
            JSONObject(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
