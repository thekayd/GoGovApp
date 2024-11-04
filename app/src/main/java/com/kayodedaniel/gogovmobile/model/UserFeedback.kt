package com.kayodedaniel.gogovmobile.model

data class UserFeedback(
    val id: String,
    val email: String,
    val phone: String,
    val rating: Float,
    val feedbackText: String
)
