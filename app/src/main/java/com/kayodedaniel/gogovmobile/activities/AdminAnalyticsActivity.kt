package com.kayodedaniel.gogovmobile.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.kayodedaniel.gogovmobile.R
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray


// Activity to display various analytics for admin users using charts
class AdminAnalyticsActivity : AppCompatActivity() {

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    // Chart components for different data categories
    private lateinit var bursaryBarChart: BarChart
    private lateinit var driversLicenseBarChart: BarChart
    private lateinit var passportBarChart: BarChart
    private lateinit var vaccinationBarChart: BarChart
    private lateinit var scheduledBarChart: BarChart
    private lateinit var feedbackLineChart: LineChart
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_analytics)

        bursaryBarChart = findViewById(R.id.bursaryBarChart)
        driversLicenseBarChart = findViewById(R.id.driversLicenseBarChart)
        passportBarChart = findViewById(R.id.passportBarChart)
        vaccinationBarChart = findViewById(R.id.vaccinationBarChart)
        scheduledBarChart = findViewById(R.id.scheduledBarChart)
        feedbackLineChart = findViewById(R.id.feedbackLineChart)

        // Loads data for each chart with a distinct color
        loadApplicationData("bursary_applications", bursaryBarChart, "Bursary Applications", Color.BLUE)
        loadApplicationData("drivers_license_applications", driversLicenseBarChart, "Driver's License Applications", Color.RED)
        loadApplicationData("passport_applications", passportBarChart, "Passport Applications", Color.GREEN)
        loadApplicationData("vaccination_applications", vaccinationBarChart, "Vaccination Appointments", Color.YELLOW)
        loadApplicationData("scheduled_appointments", scheduledBarChart, "Scheduled Appointments", Color.MAGENTA)
        loadFeedbackData()
    }

    // Function to load data for a specific category and display it in a bar chart
    private fun loadApplicationData(endpoint: String, barChart: BarChart, label: String, color: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetches application count from the API
                val count = getApplicationCount(endpoint)
                // Creates bar chart data entry with the count
                val entries = listOf(BarEntry(0f, count.toFloat()))
                val barDataSet = BarDataSet(entries, label).apply {
                    setColor(color)
                }
                val barData = BarData(barDataSet)
                withContext(Dispatchers.Main) {
                    barChart.data = barData
                    barChart.description.text = label // Setting the chart title
                    barChart.invalidate()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminAnalyticsActivity, "Failed to load data for $label", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Functions to load user feedback data and displays it in a line chart
    private fun loadFeedbackData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetches feedback counts from the API
                val feedbackCounts = getFeedbackCount()
                val entries = ArrayList<Entry>()
                // Adds each feedback count as a data point
                feedbackCounts.forEachIndexed { index, count ->
                    entries.add(Entry(index.toFloat(), count.toFloat()))
                }
                val lineDataSet = LineDataSet(entries, "User Feedback Over Time").apply {
                    color = Color.CYAN
                    valueTextColor = Color.BLACK
                }
                val lineData = LineData(lineDataSet)
                withContext(Dispatchers.Main) {
                    feedbackLineChart.data = lineData
                    feedbackLineChart.description.text = "User Feedback Over Time"
                    feedbackLineChart.invalidate()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminAnalyticsActivity, "Failed to load feedback data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to fetch application count from the specified API endpoint
    private fun getApplicationCount(endpoint: String): Int {
        val url = "$supabaseUrl/$endpoint?select=*"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Error: ${response.message}")
            val jsonArray = JSONArray(response.body?.string())
            return jsonArray.length()
        }
    }

    // Function to fetch feedback counts from the API
    private fun getFeedbackCount(): List<Int> {
        val url = "$supabaseUrl/user_feedback?select=*"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Error: ${response.message}")
            val jsonArray = JSONArray(response.body?.string())

            // Builds cumulative feedback counts list
            val feedbackCounts = mutableListOf<Int>()
            for (i in 0 until jsonArray.length()) {
                feedbackCounts.add(i + 1) // Cumulative count, incrementing by 1
            }
            return feedbackCounts
        }
    }
}