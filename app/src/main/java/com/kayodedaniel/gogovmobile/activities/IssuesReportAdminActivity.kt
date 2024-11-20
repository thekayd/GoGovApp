package com.kayodedaniel.gogovmobile.activities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.utils.SMSNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class UserReport(
    val id: Int,
    val email: String,
    val phone: String,
    val category: String,
    val specific_problem: String
)

class ReportAdapter(
    private val reports: List<UserReport>,
    private val onResolveClick: (UserReport) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val categoryTextView: android.widget.TextView = itemView.findViewById(R.id.tvCategory)
        val emailTextView: android.widget.TextView = itemView.findViewById(R.id.tvEmail)
        val phoneTextView: android.widget.TextView = itemView.findViewById(R.id.tvPhone)
        val problemTextView: android.widget.TextView = itemView.findViewById(R.id.tvProblem)
        val resolveButton: android.widget.Button = itemView.findViewById(R.id.btnResolve)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ReportViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.categoryTextView.text = report.category
        holder.emailTextView.text = report.email
        holder.phoneTextView.text = report.phone
        holder.problemTextView.text = report.specific_problem

        holder.resolveButton.setOnClickListener {
            onResolveClick(report)
        }
    }

    override fun getItemCount() = reports.size
}

class IssuesReportAdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val client = OkHttpClient()
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/user_reports"
    private val supabaseKey =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issues_report_admin)

        recyclerView = findViewById(R.id.recyclerViewReports)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchReports()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchReports() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url(supabaseUrl)
                    .get()
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    val reports = parseReports(responseBody)
                    runOnUiThread {
                        val adapter = ReportAdapter(reports) { report ->
                            resolveReport(report)
                        }
                        recyclerView.adapter = adapter
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@IssuesReportAdminActivity,
                            "Failed to fetch reports", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("IssuesReportAdmin", "Error fetching reports", e)
                runOnUiThread {
                    Toast.makeText(
                        this@IssuesReportAdminActivity,
                        "Error: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun parseReports(responseBody: String): List<UserReport> {
        val gson = Gson()
        val listType = object : TypeToken<List<UserReport>>() {}.type
        return gson.fromJson(responseBody, listType)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun resolveReport(report: UserReport) {
        // Switch to main thread for SMS sending
        Handler(Looper.getMainLooper()).post {
            val smsManager = SMSNotificationManager(this)
            smsManager.sendStatusUpdateSMS(report.phone, report.category, "Resolved")

            // Proceed with deletion on IO thread
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val deleteRequest = Request.Builder()
                        .url("$supabaseUrl?id=eq.${report.id}")
                        .delete()
                        .addHeader("apikey", supabaseKey)
                        .addHeader("Content-Type", "application/json")
                        .build()

                    val deleteResponse = client.newCall(deleteRequest).execute()

                    runOnUiThread {
                        if (deleteResponse.isSuccessful) {
                            Toast.makeText(
                                this@IssuesReportAdminActivity,
                                "Report resolved and user notified",
                                Toast.LENGTH_SHORT
                            ).show()
                            fetchReports() // Refresh the list
                        } else {
                            Toast.makeText(
                                this@IssuesReportAdminActivity,
                                "Failed to delete report",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Log.e("IssuesReportAdmin", "Error resolving report", e)
                        Toast.makeText(
                            this@IssuesReportAdminActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}