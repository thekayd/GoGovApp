package com.kayodedaniel.gogovmobile.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.databinding.ActivitySettingsAcitivityBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsAcitivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_acitivity)


        binding = ActivitySettingsAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listener for the Edit Profile text
        binding.textViewEditProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        val logoutTextView = findViewById<TextView>(R.id.textViewLogout)
        logoutTextView.setOnClickListener {
            logoutUser()
        }
    }
    private fun logoutUser() {
        // Clear user session data
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear() // Clears all stored data, including tokens and user details
            apply()
        }

        // Redirect to SignInActivity
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close the current activity
    }
}