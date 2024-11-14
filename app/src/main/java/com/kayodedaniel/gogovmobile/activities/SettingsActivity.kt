package com.kayodedaniel.gogovmobile.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.chatbotactivity.ChatBotActivity
import com.kayodedaniel.gogovmobile.databinding.ActivitySettingsAcitivityBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsAcitivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_acitivity)

        binding = ActivitySettingsAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set Home as selected
        bottomNavigationView.selectedItemId = R.id.settings

        // Set up click listener for the Edit Profile text
        binding.textViewEditProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.textViewUserFeedback.setOnClickListener {
            val intent = Intent(this, UserFeedbackActivity::class.java)
            startActivity(intent)
        }
        binding.textTermsAndConditions.setOnClickListener {
            val intent = Intent(this, TermsAndConditions::class.java)
            startActivity(intent)
        }
        binding.textView2.setOnClickListener {
            val intent = Intent(this, FAQActivity::class.java)
            startActivity(intent)
        }
        val logoutTextView = findViewById<TextView>(R.id.textViewLogout)
        logoutTextView.setOnClickListener {
            logoutUser()
        }

        // Handle navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePageActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_schedule -> {
                    startActivity(Intent(this, ScheduleAppointmentActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_view_appointment -> {
                    startActivity(Intent(this, ViewAppointmentActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_chat_bot -> {
                    startActivity(Intent(this, ChatBotActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.settings -> true // Stay on SettingsActivity
                else -> false
            }
        }
    }

    private fun logoutUser() {
        // Create a confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout Confirmation")
        builder.setMessage("Logging out will clear all your session data. You will need to log in with your email and password next time. Do you want to continue?")
        builder.setPositiveButton("Yes") { _, _ ->
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
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        // Show the confirmation dialog
        builder.show()
    }
}
