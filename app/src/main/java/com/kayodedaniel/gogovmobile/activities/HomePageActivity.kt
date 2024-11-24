package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.chatbotactivity.ChatBotActivity
import com.kayodedaniel.gogovmobile.utils.NotificationHelper

class HomePageActivity : AppCompatActivity() {
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Initializes NotificationHelper
        notificationHelper = NotificationHelper(this)

        // Checks notification permission when the app opens
        if (!notificationHelper.hasNotificationPermission()) {
            notificationHelper.requestNotificationPermission()
        }

        val transportationButton: Button = findViewById(R.id.btn_transportation)
        val healthButton: Button = findViewById(R.id.btn_health)
        val educationButton: Button = findViewById(R.id.btn_education)
        val homeAffairsButton: Button = findViewById(R.id.btn_home_affairs)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Sets Home as selected
        bottomNavigationView.selectedItemId = R.id.nav_home

        // Handles navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> true
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
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        // Button click listeners
        transportationButton.setOnClickListener {
            startActivity(Intent(this, TransportationFormsActivity::class.java))
        }

        healthButton.setOnClickListener {
            startActivity(Intent(this, HealthFormsActivity::class.java))
        }

        educationButton.setOnClickListener {
            startActivity(Intent(this, EducationFormsActivity::class.java))
        }

        homeAffairsButton.setOnClickListener {
            startActivity(Intent(this, HomeAffairsFormsActivity::class.java))
        }
    }
}