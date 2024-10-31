package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
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
    }
}