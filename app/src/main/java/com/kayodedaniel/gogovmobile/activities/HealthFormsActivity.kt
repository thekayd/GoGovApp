package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kayodedaniel.gogovmobile.R

class HealthFormsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_forms)

        //val btnMedicalAidApplication: Button = findViewById(R.id.btn_medical_aid_application)
        val btnVaccinationRegistration: Button = findViewById(R.id.btn_vaccination_registration)

        /* Medical Aid Application Button Click
        btnMedicalAidApplication.setOnClickListener {
            val intent = Intent(this, MedicalAidApplicationActivity::class.java)
            startActivity(intent)
        }*/

        // Vaccination Registration Button Click
        btnVaccinationRegistration.setOnClickListener {
            val intent = Intent(this, VaccinationRegistrationActivity::class.java)
            startActivity(intent)
        }
    }
}
