package com.kayodedaniel.gogovmobile.model

// data class for appointment
data class Appointment(
    val id: String,
    val name: String,
    val surname: String,
    val appointment_date: String,
    val appointment_time: String,
    val status: String
)
