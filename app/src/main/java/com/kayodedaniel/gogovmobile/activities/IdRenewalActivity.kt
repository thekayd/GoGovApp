package com.kayodedaniel.gogovmobile.activities

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kayodedaniel.gogovmobile.R

class IdRenewalActivity : AppCompatActivity() {

    // same with here, could have a ID renewal section, intent for button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_id_renewal)

        val btnSubmitRenewal: Button = findViewById(R.id.btn_submit_renewal)

        btnSubmitRenewal.setOnClickListener {

        }
    }
}