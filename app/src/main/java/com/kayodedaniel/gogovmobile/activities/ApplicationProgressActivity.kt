package com.kayodedaniel.gogovmobile.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class ApplicationProgressActivity : AppCompatActivity() {

    private lateinit var tvApplicationDetails: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: Button

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_progress)

        tvApplicationDetails = findViewById(R.id.tvApplicationDetails)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        fetchApplicationDetails()
    }

    private fun fetchApplicationDetails() {
        progressBar.visibility = View.VISIBLE

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("USER_EMAIL", "") ?: ""

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                // Fetch driver's license application
                val driverLicenseRequest = Request.Builder()
                    .url("$supabaseUrl/rest/v1/drivers_license_applications?email=eq.$userEmail")
                    .get()
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Authorization", "Bearer $supabaseKey")
                    .build()
                val driverLicenseResponse = client.newCall(driverLicenseRequest).execute()
                val driverLicenseResponseBody = driverLicenseResponse.body?.string()

                // Fetch vaccination application
                val vaccinationRequest = Request.Builder()
                    .url("$supabaseUrl/rest/v1/vaccination_applications?email=eq.$userEmail")
                    .get()
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Authorization", "Bearer $supabaseKey")
                    .build()
                val vaccinationResponse = client.newCall(vaccinationRequest).execute()
                val vaccinationResponseBody = vaccinationResponse.body?.string()

                withContext(Dispatchers.Main) {
                    val applicationDetails = StringBuilder()

                    if (!driverLicenseResponseBody.isNullOrEmpty()) {
                        val driverLicenseJson = JSONArray(driverLicenseResponseBody)
                        if (driverLicenseJson.length() > 0) {
                            applicationDetails.append("Driver's License Application:\n")
                            applicationDetails.append(formatApplicationDetails(driverLicenseJson.getJSONObject(0), isDriverLicense = true))
                            applicationDetails.append("\n\n")
                        }
                    }

                    if (!vaccinationResponseBody.isNullOrEmpty()) {
                        val vaccinationJson = JSONArray(vaccinationResponseBody)
                        if (vaccinationJson.length() > 0) {
                            applicationDetails.append("Vaccination Application:\n")
                            applicationDetails.append(formatApplicationDetails(vaccinationJson.getJSONObject(0), isDriverLicense = false))
                        }
                    }

                    if (applicationDetails.isEmpty()) {
                        applicationDetails.append("No applications found for this email.")
                    }

                    tvApplicationDetails.text = applicationDetails.toString()
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ApplicationProgressActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun formatApplicationDetails(details: JSONObject, isDriverLicense: Boolean): String {
        val formattedDetails = StringBuilder()
        formattedDetails.append("Application Status: ${details.optString("status", "N/A")}\n\n")
        formattedDetails.append("Personal Details:\n")
        formattedDetails.append("Name: ${details.optString("name", "N/A")}\n")
        formattedDetails.append("Surname: ${details.optString("surname", "N/A")}\n")
        formattedDetails.append("ID Number: ${details.optString("id_number", "N/A")}\n")
        formattedDetails.append("Gender: ${details.optString("gender", "N/A")}\n")
        formattedDetails.append("Date of Birth: ${details.optString("date_of_birth", "N/A")}\n\n")
        formattedDetails.append("Contact Details:\n")
        formattedDetails.append("Email: ${details.optString("email", "N/A")}\n")
        formattedDetails.append("Phone Number: ${details.optString("phone_number", "N/A")}\n\n")
        formattedDetails.append("Address:\n")
        formattedDetails.append("${details.optString("address", "N/A")}\n")
        formattedDetails.append("${details.optString("city", "N/A")}")
        if (isDriverLicense) {
            formattedDetails.append(", ${details.optString("province", "N/A")}")
        }
        formattedDetails.append("\nPostcode: ${details.optString("postcode", "N/A")}\n\n")

        if (isDriverLicense) {
            formattedDetails.append("License Details:\n")
            formattedDetails.append("License Category: ${details.optString("license_category", "N/A")}\n")
            formattedDetails.append("Test Center: ${details.optString("test_center", "N/A")}\n")
        } else {
            formattedDetails.append("Vaccination Details:\n")
            formattedDetails.append("Vaccine Type: ${details.optString("vaccine_type", "N/A")}\n")
            formattedDetails.append("Vaccination Center: ${details.optString("vaccination_center", "N/A")}\n")
        }

        return formattedDetails.toString()
    }
}