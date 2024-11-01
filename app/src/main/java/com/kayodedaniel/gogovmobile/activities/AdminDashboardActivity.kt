// AdminDashboardActivity.kt
package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.kayodedaniel.gogovmobile.R

class AdminDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        findViewById<CardView>(R.id.cardApplications).setOnClickListener {
            startActivity(Intent(this, ViewApplicationActivity::class.java))
        }

        findViewById<CardView>(R.id.cardLogout).setOnClickListener {
            finish() // Logs out the admin by closing the activity
        }
        // Implement the functionality for approving applications and viewing analytics here
    }
}
