package com.kayodedaniel.gogovmobile.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.data.ReportData
import com.kayodedaniel.gogovmobile.network.SupabaseApiHandler
import com.kayodedaniel.gogovmobile.repository.ReportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ReportActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var specificProblemEditText: EditText
    private lateinit var submitButton: Button

    // Repository instance using dependency injection
    private val reportRepository: ReportRepository = ReportRepository(SupabaseApiHandler())


    // Coroutine scope for handling async operations
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        initializeViews()
        setupSpinner()
        setupSubmitButton()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.editTextEmail)
        phoneEditText = findViewById(R.id.editTextPhone)
        categorySpinner = findViewById(R.id.spinnerReportCategory)
        specificProblemEditText = findViewById(R.id.editTextSpecificProblem)
        submitButton = findViewById(R.id.btnSubmitReport)
    }

    private fun setupSpinner() {
        val categories = arrayOf("Form Submission Issue", "Status Tracking Issue", "Technical Problem", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    // submit button for report
    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            val reportData = ReportData(
                email = emailEditText.text.toString().trim(),
                phone = phoneEditText.text.toString().trim(),
                category = categorySpinner.selectedItem.toString(),
                specificProblem = specificProblemEditText.text.toString().trim()
            )

            if (reportRepository.validateReport(reportData)) {
                submitReport(reportData)
            } else {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitReport(reportData: ReportData) {
        // Using coroutines for async operation
        coroutineScope.launch {
            try {
                // Show loading indicator
                submitButton.isEnabled = false

                // Executes report submission in background
                reportRepository.submitReport(reportData)

                // Handles success
                Toast.makeText(
                    this@ReportActivity,
                    "Report submitted successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()

            } catch (e: Exception) {
                // Handles error
                Toast.makeText(
                    this@ReportActivity,
                    "Failed to submit report",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                submitButton.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cleans up coroutines when activity is destroyed
    }
}
