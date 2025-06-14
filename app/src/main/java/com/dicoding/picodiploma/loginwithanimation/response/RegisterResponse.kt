package com.dicoding.picodiploma.loginwithanimation.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(

	// Jika sukses, hanya mengembalikan true
	@SerializedName("success")
	val success: Any? = null,

	// Jika gagal, mengembalikan error detail
	@SerializedName("data")
	val data: Any? = null, // Bisa null saat gagal

	@SerializedName("isBoom")
	val isBoom: Boolean? = null,

	@SerializedName("isServer")
	val isServer: Boolean? = null,

	@SerializedName("output")
	val output: OutputResponse? = null
)

data class OutputResponse(
	@SerializedName("statusCode")
	val statusCode: Int? = null,

	@SerializedName("payload")
	val payload: PayloadResponse? = null
)

data class PayloadResponse(
	@SerializedName("statusCode")
	val statusCode: Int? = null,

	@SerializedName("error")
	val error: String? = null,

	@SerializedName("message")
	val message: String? = null
)

