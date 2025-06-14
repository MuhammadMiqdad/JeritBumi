package com.dicoding.picodiploma.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://authjeritbumi-production-ae42.up.railway.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // API Service tanpa token (untuk Register & Login)
    fun getApiServiceWithoutToken(): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // API Service dengan token (untuk request yang butuh autentikasi)
    fun getApiService(token: String): ApiService {
        val authClient = client.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token") // Menyisipkan token ke Header
                    .build()
                chain.proceed(request)
            }
            .build()

        val authRetrofit = retrofit.newBuilder()
            .client(authClient)
            .build()

        return authRetrofit.create(ApiService::class.java)
    }
}
