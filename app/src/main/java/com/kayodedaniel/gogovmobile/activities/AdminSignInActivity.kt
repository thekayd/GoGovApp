package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.utils.PasswordUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AdminSignInActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_sign_in)

        val emailInput = findViewById<EditText>(R.id.inputAdminEmail)
        val passwordInput = findViewById<EditText>(R.id.inputAdminPassword)
        val signInButton = findViewById<MaterialButton>(R.id.buttonAdminSignIn)

        signInButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            } else if (!isValidPassword(password)) {
                Toast.makeText(
                    this,
                    "Password must be at least 8 characters, include a number and an uppercase letter",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                authenticateAdmin(email, password)
            }
        }
    }

    private fun sanitizeInput(input: String): String {
        return input.replace(Regex("[^A-Za-z0-9@._-]"), "") // Allow only safe characters
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 &&
                Regex(".*[A-Z].*").containsMatchIn(password) &&
                Regex(".*[0-9].*").containsMatchIn(password)
    }

    private fun authenticateAdmin(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject()
                json.put("email", email)
                json.put("password", password)

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/admin_profile")
                    .get()
                    .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo")
                    .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo")
                    .addHeader("Prefer", "return=representation")
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonArray = org.json.JSONArray(responseBody)

                        val adminExists = checkAdminCredentials(jsonArray, email, password)

                        withContext(Dispatchers.Main) {
                            if (adminExists) {
                                Toast.makeText(this@AdminSignInActivity, "Admin Login successful!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@AdminSignInActivity, AdminDashboardActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@AdminSignInActivity, "Invalid admin credentials", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AdminSignInActivity, "Authentication failed", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminSignInActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkAdminCredentials(jsonArray: org.json.JSONArray, email: String, password: String): Boolean {
        for (i in 0 until jsonArray.length()) {
            val adminObject = jsonArray.getJSONObject(i)
            // Use BCrypt to verify password
            if (adminObject.getString("email") == email &&
                PasswordUtils.checkPassword(password, adminObject.getString("password"))) {
                return true
            }
        }
        return false
    }
}