package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kayodedaniel.gogovmobile.DriversLicenseActivity
import com.kayodedaniel.gogovmobile.R

class TransportationFormsActivity : AppCompatActivity() {

    // transportation intent forms
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transportation_forms)

        val driversLicenseButton: Button = findViewById(R.id.btn_drivers_license)

        driversLicenseButton.setOnClickListener {
            val intent = Intent(this, DriversLicenseActivity::class.java)
            startActivity(intent)
        }
    }
}