package com.dicoding.picodiploma.loginwithanimation.response

data class ChangePasswordResponse(
    val data: Any?,
    val isBoom: Boolean,
    val isServer: Boolean,
    val output: ChangePasswordOutput?
)

data class ChangePasswordOutput(
    val statusCode: Int,
    val payload: ChangePasswordPayload,
    val headers: Map<String, String>?
)

data class ChangePasswordPayload(
    val statusCode: Int,
    val error: String,
    val message: String
)
