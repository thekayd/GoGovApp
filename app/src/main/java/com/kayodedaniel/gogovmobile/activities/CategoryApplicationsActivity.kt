package com.kayodedaniel.gogovmobile.activities

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.utils.ApplicationStatusManager
// import com.kayodedaniel.gogovmobile.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class CategoryApplicationsActivity : AppCompatActivity() {

    private val statusManager by lazy { ApplicationStatusManager(this) }
    // private val notificationHelper by lazy { NotificationHelper(this) }
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_applications)

        val category = intent.getStringExtra("category") ?: ""
        fetchApplications(category)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showStatusDialog(
        category: String,
        applicationId: String,
        userEmail: String,
        newStatus: String
    ) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Status Change")
            .setMessage("Are you sure you want to change the status to $newStatus?")
            .setPositiveButton("Yes") { _, _ ->
                statusManager.updateApplicationStatus(
                    category = category,
                    applicationId = applicationId,
                    newStatus = newStatus,
                    userEmail = userEmail,
                    onSuccess = {
                        /* notificationHelper.showNotification(
                            "Application Status Updated",
                            "Your $category application status has been changed to $newStatus"
                        ) */
                        // Refresh the applications list
                        fetchApplications(category)
                    }
                ) { error ->
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchApplications(category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$supabaseUrl/rest/v1/${category}_applications")
                    .get()
                    .addHeader("apikey", supabaseKey)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CategoryApplicationsActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val applicationsArray = JSONArray(responseBody)
                withContext(Dispatchers.Main) {
                    displayApplications(applicationsArray, category)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CategoryApplicationsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayApplications(applications: JSONArray, category: String) {
        val container = findViewById<LinearLayout>(R.id.applicationsContainer)
        container.removeAllViews()

        for (i in 0 until applications.length()) {
            val application = applications.getJSONObject(i)
            val view = LayoutInflater.from(this).inflate(R.layout.application_card, container, false)

            view.findViewById<TextView>(R.id.textApplicantName).text = application.optString("name", "N/A")
            view.findViewById<TextView>(R.id.textApplicationStatus).text = application.optString("status", "N/A")

            val detailsText = formatApplicationDetails(application, category)
            view.findViewById<TextView>(R.id.textApplicationDetails).text = detailsText

            val buttonsContainer = view.findViewById<LinearLayout>(R.id.statusButtonsContainer)
            buttonsContainer.removeAllViews()

            val applicationId = application.optString("id")

            val approveButton = Button(this).apply {
                text = "Approve"
                setOnClickListener {
                    showStatusDialog(
                        category,
                        applicationId,
                        application.optString("email"),
                        "Approved"
                    )
                }
            }
            buttonsContainer.addView(approveButton)

            val rejectButton = Button(this).apply {
                text = "Reject"
                setOnClickListener {
                    showStatusDialog(
                        category,
                        applicationId,
                        application.optString("email"),
                        "Rejected"
                    )
                }
            }
            buttonsContainer.addView(rejectButton)

            val inProgressButton = Button(this).apply {
                text = "In Progress"
                setOnClickListener {
                    showStatusDialog(
                        category,
                        applicationId,
                        application.optString("email"),
                        "In Progress"
                    )
                }
            }
            buttonsContainer.addView(inProgressButton)
            container.addView(view)
        }
    }

    private fun formatApplicationDetails(details: JSONObject, applicationType: String): String {
        val formattedDetails = StringBuilder()

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

        when (applicationType) {
            "drivers_license" -> {
                formattedDetails.append("License Details:\n")
                formattedDetails.append("License Category: ${details.optString("license_category", "N/A")}\n")
                formattedDetails.append("Test Center: ${details.optString("test_center", "N/A")}\n")
            }
            "vaccination" -> {
                formattedDetails.append("Vaccination Details:\n")
                formattedDetails.append("Vaccine Type: ${details.optString("vaccine_type", "N/A")}\n")
                formattedDetails.append("Vaccination Center: ${details.optString("vaccination_center", "N/A")}\n")
            }
            "passport" -> {
                formattedDetails.append("Passport Details:\n")
                formattedDetails.append("Passport Type: ${details.optString("passport_type", "N/A")}\n")
                formattedDetails.append("Processing Center: ${details.optString("processing_center", "N/A")}\n")
            }
            "bursary" -> {
                formattedDetails.append("Bursary Details:\n")
                formattedDetails.append("Institution Name: ${details.optString("institution_name", "N/A")}\n")
                formattedDetails.append("Course of Study: ${details.optString("course_of_study", "N/A")}\n")
                formattedDetails.append("Study Year: ${details.optString("study_year", "N/A")}\n")
                formattedDetails.append("Total Course Duration: ${details.optString("total_course_duration", "N/A")} years\n")
                formattedDetails.append("Annual Tuition Fee: R${details.optString("annual_tuition_fee", "N/A")}\n")
                formattedDetails.append("Other Funding Sources: ${details.optString("other_funding_sources", "N/A")}\n")
            }
        }

        return formattedDetails.toString()
    }
}
