package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.activities.ViewAppointmentActivity
import com.kayodedaniel.gogovmobile.chatbotactivity.ChatBotActivity

class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val transportationButton: Button = findViewById(R.id.btn_transportation)
        val healthButton: Button = findViewById(R.id.btn_health)
        val educationButton: Button = findViewById(R.id.btn_education)
        val homeAffairsButton: Button = findViewById(R.id.btn_home_affairs)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set Home as selected
        bottomNavigationView.selectedItemId = R.id.nav_home

        // Handle navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> true // Stay on this activity
                R.id.nav_schedule -> {
                    // Navigate to ScheduleAppointmentActivity
                    startActivity(Intent(this, ScheduleAppointmentActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_view_appointment -> {
                    // Navigate to ViewAppointmentActivity
                    startActivity(Intent(this, ViewAppointmentActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_chat_bot -> {
                    // Navigate to ViewAppointmentActivity
                    startActivity(Intent(this, ChatBotActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        // Transportation Button Click
        transportationButton.setOnClickListener {
            val intent = Intent(this, TransportationFormsActivity::class.java)
            startActivity(intent)
        }

        // Health Button Click
        healthButton.setOnClickListener {
            val intent = Intent(this, HealthFormsActivity::class.java)
            startActivity(intent)
        }

        // Education Button Click
        educationButton.setOnClickListener {
            val intent = Intent(this, EducationFormsActivity::class.java)
            startActivity(intent)
        }

        // Home Affairs Button Click
        homeAffairsButton.setOnClickListener {
            val intent = Intent(this, HomeAffairsFormsActivity::class.java)
            startActivity(intent)
        }
    }
}
