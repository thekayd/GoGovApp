package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kayodedaniel.gogovmobile.R

class EducationFormsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_education_forms)

        val btnBursaryApplication: Button = findViewById(R.id.btn_bursary_application)


        // Bursary Application Button Click
        btnBursaryApplication.setOnClickListener {
            val intent = Intent(this, BursaryApplicationActivity::class.java)
            startActivity(intent)
        }


    }
}
