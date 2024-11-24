package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.kayodedaniel.gogovmobile.R

class ViewApplicationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_application)

        // card views for viewing application  on click
        findViewById<CardView>(R.id.cardDriversLicense).setOnClickListener {
            val intent = Intent(this, CategoryApplicationsActivity::class.java)
            intent.putExtra("category", "drivers_license")
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardPassport).setOnClickListener {
            val intent = Intent(this, CategoryApplicationsActivity::class.java)
            intent.putExtra("category", "passport")
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardBursary).setOnClickListener {
            val intent = Intent(this, CategoryApplicationsActivity::class.java)
            intent.putExtra("category", "bursary")
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardVaccination).setOnClickListener {
            val intent = Intent(this, CategoryApplicationsActivity::class.java)
            intent.putExtra("category", "vaccination")
            startActivity(intent)
        }
    }
}
