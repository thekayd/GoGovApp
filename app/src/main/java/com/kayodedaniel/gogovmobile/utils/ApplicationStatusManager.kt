package com.kayodedaniel.gogovmobile.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
// import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.time.Instant

class ApplicationStatusManager(private val context: Context) {
    private val TAG = "ApplicationStatusManager"
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co"
    private val supabaseKey =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateApplicationStatus(
        category: String,
        applicationId: String,
        newStatus: String,
        userEmail: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val statusUpdated = updateStatusInSupabase(category, applicationId, newStatus)
                if (!statusUpdated) {
                    throw IOException("Failed to update application status")
                }


                CoroutineScope(Dispatchers.Main).launch {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating status", e)
                CoroutineScope(Dispatchers.Main).launch {
                    onError(e.message ?: "Unknown error occurred")
                }
            }
        }
    }

    // updates supabase status for application
    private fun updateStatusInSupabase(
        category: String,
        applicationId: String,
        newStatus: String
    ): Boolean {
        try {
            val updateBody = JSONObject().apply {
                put("status", newStatus)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/${category}_applications?id=eq.$applicationId")
                .patch(updateBody.toString().toRequestBody(JSON))
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer $supabaseKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(
                        TAG,
                        "Status update failed: ${response.code} - ${response.body?.string()}"
                    )
                }
                return response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating status in Supabase", e)
            return false
        }
    }

}