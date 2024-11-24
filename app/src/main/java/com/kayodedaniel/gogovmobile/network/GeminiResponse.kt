package com.kayodedaniel.gogovmobile.network

// data class for gemini responses
data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)