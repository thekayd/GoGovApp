package com.kayodedaniel.gogovmobile.network

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)