package com.dicoding.picodiploma.network

import com.dicoding.picodiploma.loginwithanimation.response.ChangePasswordRequest
import com.dicoding.picodiploma.loginwithanimation.response.ChangePasswordResponse
import com.dicoding.picodiploma.loginwithanimation.response.ChangePasswordTokenResponse
import com.dicoding.picodiploma.loginwithanimation.response.ForgotPasswordResponse
import com.dicoding.picodiploma.loginwithanimation.response.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.response.ProfileResponse
import com.dicoding.picodiploma.loginwithanimation.response.RegisterResponse
import com.dicoding.picodiploma.loginwithanimation.response.SaveLocationRequest
import com.dicoding.picodiploma.loginwithanimation.response.GetLocationsResponse
import com.dicoding.picodiploma.loginwithanimation.response.PickupHistoryResponse
import com.dicoding.picodiploma.loginwithanimation.response.PickupRequestBody
import com.dicoding.picodiploma.loginwithanimation.response.PickupResponse
import com.dicoding.picodiploma.loginwithanimation.response.SaveLocationResponse
import com.dicoding.picodiploma.loginwithanimation.response.StatusBody
import com.dicoding.picodiploma.loginwithanimation.response.StoryResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("notelp") notelp: String,
        @Field("alamat") alamat: String,
        @Field("role") role: String,
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("get-profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): ProfileResponse

    @PUT("change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @FormUrlEncoded
    @POST("forgot-password")
    fun forgotPassword(
        @Field("email") email: String
    ): Call<ForgotPasswordResponse>

    @FormUrlEncoded
    @PUT("forgot-password/change/{token}")
    fun changePasswordToken(
        @Path("token") token: String,
        @Field("newPassword") newPassword: String
    ): Call<ChangePasswordTokenResponse>

    @POST("saved-location")
    fun saveLocation(
        @Header("Authorization") token: String,
        @Body request: SaveLocationRequest
    ): Call<SaveLocationResponse>

    @GET("saved-location")
    fun getCustomLocations(
        @Header("Authorization") token: String
    ): Call<GetLocationsResponse> // Ubah return type menjadi SaveLocationResponse

    @POST("/pickup-request")
    suspend fun createPickupRequest(
        @Body pickupRequest: PickupRequestBody
    ): PickupResponse

    // Ambil semua pickup request milik user (history)
    @GET("/pickup-requests")
    suspend fun getPickupRequests(): PickupHistoryResponse

    // Ambil semua pickup request (admin)
    @GET("/pickup-requests/all")
    suspend fun getAllPickupRequests(): PickupHistoryResponse

    // Accept pickup request
    @PUT("pickup-request/{id}")
    suspend fun updatePickupStatus(
        @Path("id") id: Int,
        @Body body: StatusBody
    ): PickupResponse


    // Cancel pickup request
    @DELETE("/pickup-request/{id}")
    suspend fun cancelPickupRequest(
        @Path("id") id: Int
    ): PickupResponse


    @GET("stories")
    suspend fun getStories(): StoryResponse
}
