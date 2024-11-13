package com.kayodedaniel.gogovmobile.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.kayodedaniel.gogovmobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class AdminReportActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"


    private lateinit var totalUsersTextView: TextView
    private lateinit var activeUsersTextView: TextView
    private lateinit var totalApplicationsTextView: TextView
    private lateinit var approvedApplicationsTextView: TextView
    private lateinit var rejectedApplicationsTextView: TextView
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_report)

        // Initialize Views
        totalUsersTextView = findViewById(R.id.textViewTotalUsers)
        activeUsersTextView = findViewById(R.id.textViewActiveUsers)
        totalApplicationsTextView = findViewById(R.id.textViewTotalApplications)
        approvedApplicationsTextView = findViewById(R.id.textViewApprovedApplications)
        rejectedApplicationsTextView = findViewById(R.id.textViewRejectedApplications)
        lineChart = findViewById(R.id.lineChart)

        fetchReportData()
    }

    private fun fetchReportData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usersResponse = fetchSupabaseData("/users")
                val applicationsResponse = fetchSupabaseData("/applications")

                if (usersResponse != null && applicationsResponse != null) {
                    val totalUsers = usersResponse.length()
                    var activeUsers = 0
                    for (i in 0 until usersResponse.length()) {
                        if (usersResponse.getJSONObject(i).optBoolean("is_active", false)) {
                            activeUsers++
                        }
                    }

                    val totalApplications = applicationsResponse.length()
                    var approvedApplications = 0
                    var rejectedApplications = 0
                    for (i in 0 until applicationsResponse.length()) {
                        when (applicationsResponse.getJSONObject(i).optString("status")) {
                            "Approved" -> approvedApplications++
                            "Rejected" -> rejectedApplications++
                        }
                    }

                    val trendData = buildTrendData(applicationsResponse)

                    withContext(Dispatchers.Main) {
                        totalUsersTextView.text = "Total Users: $totalUsers"
                        activeUsersTextView.text = "Active Users: $activeUsers"
                        totalApplicationsTextView.text = "Total Applications: $totalApplications"
                        approvedApplicationsTextView.text = "Approved Applications: $approvedApplications"
                        rejectedApplicationsTextView.text = "Rejected Applications: $rejectedApplications"
                        setUpLineChart(trendData)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminReportActivity, "Failed to load report data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun fetchSupabaseData(endpoint: String): JSONArray? {
        val request = Request.Builder()
            .url("$supabaseUrl$endpoint")
            .get()
            .addHeader("apikey", supabaseKey)
            .build()

        return client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                JSONArray(response.body?.string())
            } else {
                null
            }
        }
    }

    private fun buildTrendData(applications: JSONArray): List<Entry> {
        val monthlyCounts = mutableMapOf<String, Int>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        for (i in 0 until applications.length()) {
            val applicationDate = applications.getJSONObject(i).optString("created_at")
            val date = dateFormat.parse(applicationDate) ?: continue
            val monthYear = monthYearFormat.format(date)
            monthlyCounts[monthYear] = monthlyCounts.getOrDefault(monthYear, 0) + 1
        }

        return monthlyCounts.entries.sortedBy { it.key }.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.value.toFloat())
        }
    }

    private fun setUpLineChart(data: List<Entry>) {
        val lineDataSet = LineDataSet(data, "Applications Over Time")
        lineDataSet.color = resources.getColor(R.color.purple_500)
        lineDataSet.valueTextColor = resources.getColor(R.color.black)

        lineChart.data = LineData(lineDataSet)
        lineChart.invalidate() // Refresh chart
    }
}
