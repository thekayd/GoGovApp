package com.kayodedaniel.gogovmobile.model

// data class for schedule
data class Schedule(
    val id: String,
    val name: String,
    val surname: String,
    var appointmentDate: String,
    var appointmentTime: String,
    val status: String,
    val phone: String
)
