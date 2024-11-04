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

    init {
        // initializeFirebase()
    }

    /* private fun initializeFirebase() {
        try {
            val stream: InputStream = context.assets.open("service-account.json")
            val credentials = GoogleCredentials.fromStream(stream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

            com.google.firebase.FirebaseApp.initializeApp(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    } */

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

                /* val notificationCreated = createNotificationRecord(category, applicationId, userEmail, newStatus)
                if (!notificationCreated) {
                    Log.e(TAG, "Failed to create notification record")
                }

                val fcmToken = getUserFCMToken(userEmail)
                if (fcmToken != null) {
                    sendFCMNotification(
                        fcmToken,
                        "Application Status Updated",
                        "Your $category application status has been changed to $newStatus"
                    )
                } */

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

    /* @RequiresApi(Build.VERSION_CODES.O)
private fun createNotificationRecord(
    category: String,
    applicationId: String,
    userEmail: String,
    newStatus: String
): Boolean {
    try {
        val notificationBody = JSONObject().apply {
            put("user_email", userEmail)
            put("message", "Your $category application status has been updated to: $newStatus")
            put("application_type", category)
            put("application_id", applicationId)
            put("status", "unread")
            put("created_at", Instant.now().toString())
        }

        val request = Request.Builder()
            .url("$supabaseUrl/rest/v1/notifications")
            .post(notificationBody.toString().toRequestBody(JSON))
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $supabaseKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
             .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to create notification: ${response.code} - ${response.body?.string()}")
                }
                return response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification record", e)
            return false
        }
    }

    private fun getUserFCMToken(userEmail: String): String? {
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/users?email=eq.$userEmail&select=fcm_token")
                .get()
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer $supabaseKey")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to get FCM token: ${response.code}")
                    return null
                }

                val responseBody = response.body?.string()
                val jsonArray = JSONArray(responseBody)
                return if (jsonArray.length() > 0) {
                    jsonArray.getJSONObject(0).getString("fcm_token")
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            return null
        }
    }

    private fun sendFCMNotification(token: String, title: String, messageBody: String): Boolean {
        try {
            val notificationPayload = JSONObject().apply {
                put("token", token)
                put("title", title)
                put("body", messageBody)
                put("click_action", "MAIN_ACTIVITY")
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/rpc/send_notification")  // Your backend endpoint
                .post(notificationPayload.toString().toRequestBody(JSON))
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer $supabaseKey")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to send notification: ${response.code}")
                    return false
                }
                Log.d(TAG, "Successfully sent notification request")
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
            return false
        }
    }
}
 */
}