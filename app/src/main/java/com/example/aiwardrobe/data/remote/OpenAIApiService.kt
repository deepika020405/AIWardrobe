package com.example.aiwardrobe.data.remote

import com.example.aiwardrobe.model.OllamaRequest
import com.example.aiwardrobe.model.OllamaResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApiService {

    @POST("api/chat")
    suspend fun analyzeImage(
        @Header("Authorization") authorization: String,
        @Body request: OllamaRequest
    ): OllamaResponse
}
