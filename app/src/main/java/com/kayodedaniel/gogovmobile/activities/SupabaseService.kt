package com.kayodedaniel.gogovmobile.activities

import com.kayodedaniel.gogovmobile.activities.DriverApplication
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

// using supabase servoces
interface SupabaseService {
    @Headers("Content-Type: application/json")
    @POST("rest/v1/drivers_license_applications")
    fun submitDriverApplication(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Body application: DriverApplication
    ): Call<Void>
}