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

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/drivers_license_applications"
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

        val url = "$supabaseUrl?email=eq.$userEmail"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Authorization", "Bearer $supabaseKey")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                        val jsonArray = JSONArray(responseBody)
                        if (jsonArray.length() > 0) {
                            val applicationDetails = jsonArray.getJSONObject(0)
                            displayApplicationDetails(applicationDetails)
                        } else {
                            tvApplicationDetails.text = "No application found for this email."
                        }
                    } else {
                        Toast.makeText(this@ApplicationProgressActivity, "Failed to fetch application details", Toast.LENGTH_SHORT).show()
                    }
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

    private fun displayApplicationDetails(details: JSONObject) {
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
        formattedDetails.append("${details.optString("city", "N/A")}, ${details.optString("province", "N/A")}\n")
        formattedDetails.append("Postcode: ${details.optString("postcode", "N/A")}\n\n")
        formattedDetails.append("License Details:\n")
        formattedDetails.append("License Category: ${details.optString("license_category", "N/A")}\n")
        formattedDetails.append("Test Center: ${details.optString("test_center", "N/A")}\n")

        tvApplicationDetails.text = formattedDetails.toString()
    }
}