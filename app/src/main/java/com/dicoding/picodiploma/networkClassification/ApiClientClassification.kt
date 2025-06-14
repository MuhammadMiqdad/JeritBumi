package com.dicoding.picodiploma.networkClassification

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiClientClassification {
    private const val BASE_URL = "https://apiclassification-production.up.railway.app/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: ApiServiceClassification by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create()) // Karena response text biasa
            .build()
            .create(ApiServiceClassification::class.java)
    }
}