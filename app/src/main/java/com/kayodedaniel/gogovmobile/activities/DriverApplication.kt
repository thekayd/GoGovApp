package com.kayodedaniel.gogovmobile.activities

data class DriverApplication(
    val user_id: String, // This should be the authenticated user ID from Supabase Auth
    val name: String,
    val surname: String,
    val id_number: String,
    val gender: String,
    val province: String,
    val address: String,
    val city: String,
    val postcode: String,
    val email: String,
    val phone_number: String,
    val license_category: String,
    val test_center: String,
    val date_of_birth: String,
    val id_document: String,
    val passport_photo: String,
    val proof_of_address: String,
    val eye_test_certificate: String
)