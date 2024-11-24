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
        // Loads email from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("USER_EMAIL", "")
        emailInput.setText(userEmail)
    }

    private fun setupClickListeners() {
        // allows for updating users
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

    // validates users
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

    // gets access token from user preference email
    private fun getUserAccessToken(): String {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("ACCESS_TOKEN", "") ?: ""

        // If token is empty, redirects to login
        if (token.isEmpty()) {
            runOnUiThread {
                Toast.makeText(this, "Session expired. Please login again", Toast.LENGTH_LONG).show()
                redirectToLogin()
            }
        }
        return token
    }

    // redirect login function
    private fun redirectToLogin() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun updateUserProfile() {
        showLoading(true)

        // Gets token before making request
        val accessToken = getUserAccessToken()
        if (accessToken.isEmpty()) {
            return // The getUserAccessToken function will handle the redirect
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Only proceeds with password update if a new password is provided
                if (passwordInput.text.toString().isNotEmpty()) {
                    //  updates the password first
                    val passwordJson = JSONObject().apply {
                        put("password", passwordInput.text.toString())
                    }

                    val passwordBody = passwordJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

                    val passwordRequest = Request.Builder()
                        .url("https://bgckkkxjfnkwgjzlancs.supabase.co/auth/v1/user")
                        .put(passwordBody)
                        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()

                    // parses to supabase
                    client.newCall(passwordRequest).execute().use { response ->
                        val responseBody = response.body?.string() ?: "Unknown error"

                        if (!response.isSuccessful) {
                            when (response.code) {
                                401 -> {
                                    // Unauthorized - token expired or invalid
                                    withContext(Dispatchers.Main) {
                                        showLoading(false)
                                        Toast.makeText(
                                            this@ProfileActivity,
                                            "Session expired. Please login again",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        redirectToLogin()
                                    }
                                    return@launch
                                }
                                else -> {
                                    withContext(Dispatchers.Main) {
                                        showLoading(false)
                                        Toast.makeText(
                                            this@ProfileActivity,
                                            "Failed to update password: ${parseErrorMessage(responseBody)}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    return@launch
                                }
                            }
                        }
                    }

                    // If password update was successful, it shows success message
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(
                            this@ProfileActivity,
                            "Password updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Clear password fields
                        passwordInput.setText("")
                        confirmPasswordInput.setText("")

                        finish()
                    }
                }

            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@ProfileActivity,
                        "Update failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    // progress bar loading function
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        updateButton.isEnabled = !show
    }

    // error message parsing
    private fun parseErrorMessage(responseBody: String): String {
        return try {
            val jsonObject = JSONObject(responseBody)
            jsonObject.optString("msg", "An unknown error occurred")
        } catch (e: Exception) {
            "An unknown error occurred"
        }
    }

    // saves email preferences
    private fun saveEmailToPreferences(email: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_EMAIL", email)
            apply()
        }
    }

}