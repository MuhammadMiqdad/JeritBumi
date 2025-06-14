package com.dicoding.picodiploma.loginwithanimation.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("data")
    val data: UserData? = null,

    @SerializedName("isBoom")
    val isBoom: Boolean? = null,

    @SerializedName("isServer")
    val isServer: Boolean? = null,

    @SerializedName("output")
    val output: Output? = null
)

data class UserData(
    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("role")
    val role: String? = null
)

data class Output(
    @SerializedName("statusCode")
    val statusCode: Int? = null,

    @SerializedName("payload")
    val payload: Payload? = null
)

data class Payload(
    @SerializedName("statusCode")
    val statusCode: Int? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("message")
    val message: String? = null
)
