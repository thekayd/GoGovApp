package com.kayodedaniel.gogovmobile.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ReportActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/user_reports"
    private val supabaseKey =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)
        val categorySpinner = findViewById<Spinner>(R.id.spinnerReportCategory)
        val specificProblemEditText = findViewById<EditText>(R.id.editTextSpecificProblem)
        val submitButton = findViewById<Button>(R.id.btnSubmitReport)

        // Populate spinner with report categories
        val categories =
            arrayOf("Form Submission Issue", "Status Tracking Issue", "Technical Problem", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        submitButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val category = categorySpinner.selectedItem.toString()
            val specificProblem = specificProblemEditText.text.toString().trim()

            if (email.isNotEmpty() && phone.isNotEmpty() && specificProblem.isNotEmpty()) {
                saveReportToSupabase(email, phone, category, specificProblem)
            } else {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveReportToSupabase(
        email: String,
        phone: String,
        category: String,
        specificProblem: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val json = JSONObject().apply {
                put("email", email)
                // Keep the original phone number exactly as entered
                put("phone", phone)
                put("category", category)
                put("specific_problem", specificProblem)
            }

            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(supabaseUrl)
                .post(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(
                            this@ReportActivity,
                            "Report submitted successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@ReportActivity,
                            "Failed to submit report",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}