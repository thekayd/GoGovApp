package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.kayodedaniel.gogovmobile.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.createSupabaseClient
import com.kayodedaniel.gogovmobile.R
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.providers.builtin.Email
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

        // Get references to the input fields
        val emailInput = findViewById<EditText>(R.id.inputEmail)
        val passwordInput = findViewById<EditText>(R.id.InputPassword)
        val signInButton = findViewById<MaterialButton>(R.id.buttonSignIn)
        val signUpText = findViewById<TextView>(R.id.textCreateNewAccount)

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
            finish() // Optional: Close Sign In Activity
        }

    }



    private fun loginUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create the JSON body
                val json = JSONObject()
                json.put("email", email)
                json.put("password", password)

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

                // Build the request for login
                val request = Request.Builder()
                    .url("https://bgckkkxjfnkwgjzlancs.supabase.co/auth/v1/token?grant_type=password")
                    .post(body)
                    .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo") // Add your Supabase API Key here
                    .addHeader("Content-Type", "application/json")
                    .build()

                // Execute the request
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SignInActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to MainActivity
                            val intent = Intent(this@SignInActivity, HomePageActivity::class.java)
                            startActivity(intent)
                            finish() // Close this activity to prevent going back to login
                        }
                    } else {
                        // Handle the error and show why login failed
                        val errorBody = response.body?.string() ?: "Unknown error"
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SignInActivity, "Login failed: $errorBody", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: IOException) {
                // Handle network errors
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignInActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
