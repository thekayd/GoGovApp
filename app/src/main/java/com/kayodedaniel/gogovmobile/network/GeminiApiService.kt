package com.kayodedaniel.gogovmobile.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// generation key for chatbot - interface
interface GeminiApiService {
    @POST("models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}