package com.kayodedaniel.gogovmobile.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ApplicationProgressActivity : AppCompatActivity() {

    private lateinit var tvFormName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvDetails: TextView
    private lateinit var btnBack: Button

    private val supabaseUrl =
        "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/drivers_license_applications" // Replace with your actual endpoint
    private val supabaseKey =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sRSJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_progress)

        // Initialize views
        tvFormName = findViewById(R.id.tvFormName)
        tvStatus = findViewById(R.id.tvStatus)
        tvDetails = findViewById(R.id.tvDetails)
        btnBack = findViewById(R.id.btnBack)

        // Fetch and display application details
        fetchApplicationDetails()

        // Back button listener
        btnBack.setOnClickListener {
            finish() // Close this activity and return to the previous one
        }
    }

    private fun fetchApplicationDetails() {
        // Get user ID from shared preferences or intent
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        // Validate user ID
        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Build the request URL to get application details for the user
        val requestUrl = "$supabaseUrl?select=*&user_id=eq.$userId"

        // Build OkHttp3 request
        val request = Request.Builder()
            .url(requestUrl)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $supabaseKey")
            .build()

        // Execute request asynchronously
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@ApplicationProgressActivity,
                        "Failed to fetch data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "")

                    runOnUiThread {
                        // Assuming the application form has fields like 'form_name', 'status', and 'details'
                        tvFormName.text = jsonResponse.optString("form_name", "N/A")
                        tvStatus.text = jsonResponse.optString("status", "N/A")
                        tvDetails.text = jsonResponse.optString("details", "N/A")
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@ApplicationProgressActivity,
                            "Failed to fetch application details.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }
}
