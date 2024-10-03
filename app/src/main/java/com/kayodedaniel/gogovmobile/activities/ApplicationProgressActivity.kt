package com.kayodedaniel.gogovmobile

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ApplicationProgressActivity : AppCompatActivity() {

    private lateinit var tvFormName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvDetails: TextView
    private lateinit var btnBack: Button

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/Drivers_License_Application" // Replace with your actual endpoint
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo" // Add your Supabase API Key here
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
        // Get user ID or any unique identifier for the application from shared preferences or intent
        val userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("user_id", null)

        // Validate user ID
        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Build the request URL to get application details for the logged-in user
        val requestUrl = "$supabaseUrl?user_id=eq.$userId" // Assuming you have a user_id field

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
                    Toast.makeText(this@ApplicationProgressActivity, "Failed to fetch data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        parseAndDisplayApplicationData(it)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ApplicationProgressActivity, "Failed to fetch application details", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun parseAndDisplayApplicationData(responseBody: String) {
        val jsonArray = JSONObject(responseBody).getJSONArray("data")
        if (jsonArray.length() > 0) {
            val application = jsonArray.getJSONObject(0)
            val formName = "Driver's License"
            val status = application.getString("status")
            val details = """
                Name: ${application.getString("name")}
                Surname: ${application.getString("surname")}
                ID Number: ${application.getString("id_number")}
                Gender: ${application.getString("gender")}
                Province: ${application.getString("province")}
                Address: ${application.getString("address")}
                City: ${application.getString("city")}
                Postcode: ${application.getString("postcode")}
                Email: ${application.getString("email")}
                Phone Number: ${application.getString("phone_number")}
                License Category: ${application.getString("license_category")}
                Test Center: ${application.getString("test_center")}
                Date of Birth: ${application.getString("date_of_birth")}
            """.trimIndent()

            runOnUiThread {
                tvFormName.text = formName
                tvStatus.text = status
                tvDetails.text = details
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "No application found for this user.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
