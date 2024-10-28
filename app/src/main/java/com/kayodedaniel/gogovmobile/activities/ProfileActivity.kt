package com.kayodedaniel.gogovmobile.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ProfileActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var updateButton: Button
    private lateinit var backButton: Button
    private lateinit var progressBar: ProgressBar

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.edit_email)
        passwordInput = findViewById(R.id.change_password)
        confirmPasswordInput = findViewById(R.id.edit_confirm_password)
        updateButton = findViewById(R.id.btn_update_account)
        backButton = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.ProgressBar)
    }

    private fun loadUserData() {
        // Load email from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("USER_EMAIL", "")
        emailInput.setText(userEmail)
    }

    private fun setupClickListeners() {
        updateButton.setOnClickListener {
            if (validateInputs()) {
                updateUserProfile()
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }

    private fun validateInputs(): Boolean {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        if (email.isEmpty()) {
            emailInput.error = "Email cannot be empty"
            return false
        }

        if (password.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (password != confirmPassword) {
                confirmPasswordInput.error = "Passwords do not match"
                return false
            }
            if (password.length < 6) {
                passwordInput.error = "Password must be at least 6 characters"
                return false
            }
        }

        return true
    }

    private fun updateUserProfile() {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First update the user data in Supabase
                val json = JSONObject().apply {
                    put("email", emailInput.text.toString().trim())
                    if (passwordInput.text.toString().isNotEmpty()) {
                        put("password", passwordInput.text.toString())
                    }
                }

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("https://bgckkkxjfnkwgjzlancs.supabase.co/auth/v1/user")
                    .put(body)
                    .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo")
                    .addHeader("Content-Type", "application/json")
                    // You'll need to add the user's access token here
                    .addHeader("Authorization", "Bearer ${getUserAccessToken()}")
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: "Unknown error"

                    withContext(Dispatchers.Main) {
                        showLoading(false)

                        if (response.isSuccessful) {
                            // Update SharedPreferences with new email
                            saveEmailToPreferences(emailInput.text.toString().trim())
                            Toast.makeText(this@ProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            val errorMessage = parseErrorMessage(responseBody)
                            Toast.makeText(this@ProfileActivity, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }

            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@ProfileActivity, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        updateButton.isEnabled = !show
    }

    private fun parseErrorMessage(responseBody: String): String {
        return try {
            val jsonObject = JSONObject(responseBody)
            jsonObject.optString("msg", "An unknown error occurred")
        } catch (e: Exception) {
            "An unknown error occurred"
        }
    }

    private fun saveEmailToPreferences(email: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_EMAIL", email)
            apply()
        }
    }

    private fun getUserAccessToken(): String {
        // Implement this method to retrieve the user's access token from your storage
        // This could be SharedPreferences or another secure storage solution
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("ACCESS_TOKEN", "") ?: ""
    }
}