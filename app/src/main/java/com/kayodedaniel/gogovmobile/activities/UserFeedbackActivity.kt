package com.kayodedaniel.gogovmobile.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
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

class UserFeedbackActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/user_feedback"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_feedback)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)
        val feedbackEditText = findViewById<EditText>(R.id.editTextFeedback)
        val submitButton = findViewById<Button>(R.id.btnSubmitFeedback)

        submitButton.setOnClickListener {
            val rating = ratingBar.rating
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val feedback = feedbackEditText.text.toString().trim()

            if (email.isNotEmpty() && phone.isNotEmpty() && feedback.isNotEmpty()) {
                sendFeedbackToSupabase(rating, email, phone, feedback)
            } else {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendFeedbackToSupabase(rating: Float, email: String, phone: String, feedback: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val json = JSONObject().apply {
                put("rating", rating)
                put("email", email)
                put("phone", phone)
                put("feedback", feedback)
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
                        Toast.makeText(this@UserFeedbackActivity, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UserFeedbackActivity, "Failed to send feedback", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}