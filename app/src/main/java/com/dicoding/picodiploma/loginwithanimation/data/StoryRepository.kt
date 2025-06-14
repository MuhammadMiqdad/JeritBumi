package com.dicoding.picodiploma.loginwithanimation.data


import com.dicoding.picodiploma.loginwithanimation.response.StoryResponse
import com.dicoding.picodiploma.network.ApiService


class StoryRepository(private val apiService: ApiService) {

    suspend fun getStories(): StoryResponse {
        return apiService.getStories()
    }
}
