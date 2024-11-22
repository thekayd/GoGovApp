// ReportRepository.kt
package com.kayodedaniel.gogovmobile.repository

import com.kayodedaniel.gogovmobile.data.ReportData
import com.kayodedaniel.gogovmobile.interfaces.ReportOperations
import com.kayodedaniel.gogovmobile.network.BaseApiHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

class ReportRepository(private val apiHandler: BaseApiHandler) : ReportOperations {
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/user_reports"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    override suspend fun submitReport(reportData: ReportData) {
        val json = JSONObject().apply {
            put("email", reportData.email)
            put("phone", reportData.phone)
            put("category", reportData.category)
            put("specific_problem", reportData.specificProblem)
        }

        val request = apiHandler.createRequest(json.toString(), supabaseUrl, supabaseKey)

        withContext(Dispatchers.IO) {
            OkHttpClient().newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to submit report")
                }
            }
        }
    }

    override fun validateReport(reportData: ReportData): Boolean {
        return reportData.email.isNotEmpty() &&
                reportData.phone.isNotEmpty() &&
                reportData.specificProblem.isNotEmpty()
    }
}