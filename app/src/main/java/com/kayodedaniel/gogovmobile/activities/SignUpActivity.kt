package com.kayodedaniel.gogovmobile.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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

class SignUpActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val emailInput = findViewById<EditText>(R.id.inputEmail)
        val passwordInput = findViewById<EditText>(R.id.InputPassword)
        val confirmPasswordInput = findViewById<EditText>(R.id.InputConfirmPassword)
        val signUpButton = findViewById<Button>(R.id.buttonSignUp)
        val signInText = findViewById<TextView>(R.id.textSignIn)

        signInText.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        signUpButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (password == confirmPassword) {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    createUser(email, password)
                } else {
                    Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val signUpJson = JSONObject()
                signUpJson.put("email", email)
                signUpJson.put("password", password)

                val signUpBody = signUpJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val signUpRequest = Request.Builder()
                    .url("https://bgckkkxjfnkwgjzlancs.supabase.co/auth/v1/signup")
                    .post(signUpBody)
                    .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo")
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(signUpRequest).execute().use { response ->
                    val responseBody = response.body?.string() ?: "Unknown error"
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(responseBody)
                        val userId = jsonResponse.getJSONObject("user").getString("id")
                        val accessToken = jsonResponse.getString("access_token")

                        createProfile(userId, accessToken)

                        // Generate verification code
                        val verificationCode = (100000..999999).random().toString()

                        // Send verification email
                        EmailSender.sendVerificationEmail(email, verificationCode)

                        withContext(Dispatchers.Main) {
                            saveUserDataToPreferences(email, accessToken, userId)
                            Toast.makeText(this@SignUpActivity, "Sign-up successful! Please check your email to confirm your account.", Toast.LENGTH_LONG).show()
                            navigateToVerification(verificationCode, email)

                        }
                    } else {
                        Log.e("SignUpActivity", "Error response: $responseBody")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SignUpActivity, "Sign-Up failed", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignUpActivity, "Sign-up failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToVerification(verificationCode: String, email: String) {
        val intent = Intent(this, VerifyEmailActivity::class.java)
        intent.putExtra("VERIFICATION_CODE", verificationCode)
        intent.putExtra("EMAIL", email) // Pass the email as well
        startActivity(intent)
        finish()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createProfile(userId: String, accessToken: String) {
        val profileJson = JSONObject().apply {
            put("id", userId)
            put("name", "")
            put("profile_image_url", "")
            put("updated_at", java.time.OffsetDateTime.now().toString())
        }

        val profileBody = profileJson.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val createProfileRequest = Request.Builder()
            .url("https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/profiles")
            .post(profileBody)
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .build()

        client.newCall(createProfileRequest).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("SignUpActivity", "Failed to create profile: ${response.body?.string()}")
            }
        }
    }

    private fun saveUserDataToPreferences(email: String, accessToken: String, userId: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_EMAIL", email)
            putString("ACCESS_TOKEN", accessToken)
            putString("USER_ID", userId)
            apply()
        }
    }

    private fun getUserAccessToken(): String {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("ACCESS_TOKEN", "") ?: ""
    }
}
