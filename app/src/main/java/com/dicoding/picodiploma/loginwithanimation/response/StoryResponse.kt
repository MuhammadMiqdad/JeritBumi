package com.dicoding.picodiploma.loginwithanimation.response

import java.io.Serializable

data class StoryResponse(
    val error: Boolean,
    val message: String,
    val listStory: List<ListStoryItem>
)

data class ListStoryItem(
    val photoUrl: String,
    val createdAt: String,
    val name: String,
    val description: String,
    val lon: Double? = null,
    val id: String,
    val lat: Double? = null
) : Serializable  // Make it implement Serializable
