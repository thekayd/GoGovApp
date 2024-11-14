// AdminReportActivity.kt
package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.kayodedaniel.gogovmobile.R
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AdminReportActivity : AppCompatActivity() {
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"
    private val client = OkHttpClient()

    private lateinit var reportTypeSpinner: Spinner
    private lateinit var dateRangeSpinner: Spinner
    private lateinit var generateReportButton: Button
    private lateinit var reportContentLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_report)

        initializeViews()
        setupSpinners()
        setupGenerateButton()
    }

    private fun initializeViews() {
        reportTypeSpinner = findViewById(R.id.reportTypeSpinner)
        dateRangeSpinner = findViewById(R.id.dateRangeSpinner)
        generateReportButton = findViewById(R.id.generateReportButton)
        reportContentLayout = findViewById(R.id.reportContentLayout)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupSpinners() {
        val reportTypes = arrayOf(
            "All Services Overview",
            "Bursary Applications",
            "Driver's License Applications",
            "Passport Applications",
            "Vaccination Applications",
            "Scheduled Appointments"
        )

        val dateRanges = arrayOf(
            "Last 7 Days",
            "Last 30 Days",
            "Last 3 Months",
            "Last 6 Months",
            "Last Year"
        )

        reportTypeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, reportTypes)
        dateRangeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dateRanges)
    }

    private fun setupGenerateButton() {
        generateReportButton.setOnClickListener {
            loadReport()
        }
    }

    private fun loadReport() {
        progressBar.visibility = View.VISIBLE
        reportContentLayout.removeAllViews()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dateRange = getDateRangeInMillis(dateRangeSpinner.selectedItem.toString())
                when (reportTypeSpinner.selectedItem.toString()) {
                    "All Services Overview" -> displayAllServicesReport(dateRange)
                    "Bursary Applications" -> displayServiceReport("bursary_applications", dateRange)
                    "Driver's License Applications" -> displayServiceReport("drivers_license_applications", dateRange)
                    "Passport Applications" -> displayServiceReport("passport_applications", dateRange)
                    "Vaccination Applications" -> displayServiceReport("vaccination_applications", dateRange)
                    "Scheduled Appointments" -> displayServiceReport("scheduled_appointments", dateRange)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminReportActivity, "Failed to load report: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private suspend fun displayAllServicesReport(dateRange: Long) {
        val bursaryData = getApplicationData("bursary_applications", dateRange)
        val driversLicenseData = getApplicationData("drivers_license_applications", dateRange)
        val passportData = getApplicationData("passport_applications", dateRange)
        val vaccinationData = getApplicationData("vaccination_applications", dateRange)
        val scheduledData = getApplicationData("scheduled_appointments", dateRange)

        withContext(Dispatchers.Main) {
            addReportSection("All Services Overview", """
                |Total Applications:
                |- Bursary Applications: ${bursaryData.count}
                |- Driver's License Applications: ${driversLicenseData.count}
                |- Passport Applications: ${passportData.count}
                |- Vaccination Appointments: ${vaccinationData.count}
                |- Scheduled Appointments: ${scheduledData.count}
                |
                |Total Applications: ${bursaryData.count + driversLicenseData.count +
                    passportData.count + vaccinationData.count + scheduledData.count}
                |
                |Status Breakdown:
                |- Pending: ${bursaryData.pending + driversLicenseData.pending +
                    passportData.pending + vaccinationData.pending + scheduledData.pending}
                |- Approved: ${bursaryData.approved + driversLicenseData.approved +
                    passportData.approved + vaccinationData.approved + scheduledData.approved}
                |- Rejected: ${bursaryData.rejected + driversLicenseData.rejected +
                    passportData.rejected + vaccinationData.rejected + scheduledData.rejected}
            """.trimMargin())

            addExportButton()
            progressBar.visibility = View.GONE
        }
    }

    private suspend fun displayServiceReport(endpoint: String, dateRange: Long) {
        val data = getApplicationData(endpoint, dateRange)

        withContext(Dispatchers.Main) {
            val serviceName = when(endpoint) {
                "bursary_applications" -> "Bursary Applications"
                "drivers_license_applications" -> "Driver's License Applications"
                "passport_applications" -> "Passport Applications"
                "vaccination_applications" -> "Vaccination Applications"
                "scheduled_appointments" -> "Scheduled Appointments"
                else -> endpoint
            }

            addReportSection(serviceName, """
                |Total Applications: ${data.count}
                |
                |Status Breakdown:
                |- Pending: ${data.pending}
                |- Approved: ${data.approved}
                |- Rejected: ${data.rejected}
                |
                |Processing Rate:
                |- Approval Rate: ${if (data.count > 0) (data.approved * 100.0 / data.count).toInt() else 0}%
                |- Rejection Rate: ${if (data.count > 0) (data.rejected * 100.0 / data.count).toInt() else 0}%
                |- Pending Rate: ${if (data.count > 0) (data.pending * 100.0 / data.count).toInt() else 0}%
            """.trimMargin())

            addExportButton()
            progressBar.visibility = View.GONE
        }
    }

    private fun addReportSection(title: String, content: String) {
        val sectionView = layoutInflater.inflate(R.layout.report_section, reportContentLayout, false)
        sectionView.findViewById<TextView>(R.id.sectionTitle).text = title
        sectionView.findViewById<TextView>(R.id.sectionContent).text = content
        reportContentLayout.addView(sectionView)
    }

    private suspend fun getApplicationData(endpoint: String, dateRange: Long): ApplicationData {
        val url = "$supabaseUrl/$endpoint?select=*"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Error: ${response.message}")

                val jsonArray = JSONArray(response.body?.string())
                var pending = 0
                var approved = 0
                var rejected = 0

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    when (item.optString("status", "pending").toLowerCase()) {
                        "pending" -> pending++
                        "approved" -> approved++
                        "rejected" -> rejected++
                    }
                }

                ApplicationData(
                    count = jsonArray.length(),
                    pending = pending,
                    approved = approved,
                    rejected = rejected
                )
            }
        }
    }

    private fun getDateRangeInMillis(range: String): Long {
        val calendar = Calendar.getInstance()
        when (range) {
            "Last 7 Days" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            "Last 30 Days" -> calendar.add(Calendar.DAY_OF_YEAR, -30)
            "Last 3 Months" -> calendar.add(Calendar.MONTH, -3)
            "Last 6 Months" -> calendar.add(Calendar.MONTH, -6)
            "Last Year" -> calendar.add(Calendar.YEAR, -1)
        }
        return calendar.timeInMillis
    }

    private fun addExportButton() {
        val exportButton = Button(this).apply {
            text = "Export Report as PDF"
            setOnClickListener {
                generateAndSharePDF()
            }
        }
        reportContentLayout.addView(exportButton)
    }

    private fun generateAndSharePDF() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create a simple text file with the report content
                val reportContent = StringBuilder()
                withContext(Dispatchers.Main) {
                    for (i in 0 until reportContentLayout.childCount) {
                        val view = reportContentLayout.getChildAt(i)
                        if (view is LinearLayout) {
                            val title = view.findViewById<TextView>(R.id.sectionTitle)?.text
                            val content = view.findViewById<TextView>(R.id.sectionContent)?.text
                            if (title != null) reportContent.append("$title\n\n")
                            if (content != null) reportContent.append("$content\n\n")
                        }
                    }
                }

                val file = File(cacheDir, "report_${System.currentTimeMillis()}.txt")
                FileOutputStream(file).use { it.write(reportContent.toString().toByteArray()) }

                val uri = FileProvider.getUriForFile(
                    this@AdminReportActivity,
                    "${packageName}.provider",
                    file
                )

                sharePDF(uri)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminReportActivity, "Failed to generate report", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sharePDF(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Report"))
    }

    data class ApplicationData(
        val count: Int,
        val pending: Int,
        val approved: Int,
        val rejected: Int
    )
}