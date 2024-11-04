package com.kayodedaniel.gogovmobile.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.kayodedaniel.gogovmobile.R
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

class SignInActivity : AppCompatActivity() {


        private val client = OkHttpClient()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_sign_in)

            val emailInput = findViewById<EditText>(R.id.inputEmail)
            val passwordInput = findViewById<EditText>(R.id.InputPassword)
            val signInButton = findViewById<MaterialButton>(R.id.buttonSignIn)
            val signUpText = findViewById<TextView>(R.id.textCreateNewAccount)
            val adminLoginText = findViewById<TextView>(R.id.textAdminLogin)

            signInButton.setOnClickListener {
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    loginUser(email, password)
                } else {
                    Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }

            signUpText.setOnClickListener {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
                finish()
            }

            adminLoginText.setOnClickListener {
                val intent = Intent(this, AdminSignInActivity::class.java)
                startActivity(intent)
            }
        }

        private fun loginUser(email: String, password: String) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val json = JSONObject()
                    json.put("email", email)
                    json.put("password", password)

                    val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

                    val request = Request.Builder()
                        .url("https://bgckkkxjfnkwgjzlancs.supabase.co/auth/v1/token?grant_type=password")
                        .post(body)
                        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()

                        if (response.isSuccessful && responseBody != null) {
                            try {
                                val jsonResponse = JSONObject(responseBody)
                                val accessToken = jsonResponse.getString("access_token")
                                val userId = jsonResponse.getJSONObject("user")?.getString("id")

                                withContext(Dispatchers.Main) {
                                    // Save both email and access token to SharedPreferences
                                    saveUserDataToPreferences(email, accessToken, userId)

                                    Toast.makeText(this@SignInActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@SignInActivity, HomePageActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@SignInActivity,
                                        "Error processing login response",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } else {
                            val errorMessage = parseErrorMessage(responseBody ?: "Unknown error")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@SignInActivity,
                                    "Login failed: $errorMessage",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@SignInActivity,
                            "Login failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        private fun saveUserDataToPreferences(email: String, accessToken: String, userId: String?) {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("USER_EMAIL", email)
                putString("ACCESS_TOKEN", accessToken)
                userId?.let { putString("USER_ID", it) }
                apply()
            }
        }

        private fun parseErrorMessage(responseBody: String): String {
            return try {
                val jsonObject = JSONObject(responseBody)
                jsonObject.optString("error_description")
                    ?: jsonObject.optString("msg")
                    ?: jsonObject.optString("message")
                    ?: "An unknown error occurred"
            } catch (e: Exception) {
                "An unknown error occurred"
            }
        }
    }

