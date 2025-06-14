package com.dicoding.picodiploma.networkClassification
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiServiceClassification {
    @Multipart
    @POST("classify")
    suspend fun classifyImage(
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>
}