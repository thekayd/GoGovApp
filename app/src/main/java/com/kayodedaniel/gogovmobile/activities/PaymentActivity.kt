package com.kayodedaniel.gogovmobile.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {
    private lateinit var tvApplicationId: TextView
    private lateinit var btnCopyId: ImageButton
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvApplicationType: TextView
    private lateinit var tvAmount: TextView
    private lateinit var btnPay: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Initialize views
        initializeViews()

        // Get application details from intent
        val applicationId = intent.getStringExtra("application_id") ?: "Unknown"
        val name = intent.getStringExtra("name") ?: "Unknown"
        val surname = intent.getStringExtra("surname") ?: ""
        val email = intent.getStringExtra("email") ?: "Unknown"
        val applicationType = intent.getStringExtra("application_type") ?: "Unknown"

        // Calculate amount based on application type
        val amount = when (applicationType) {
            "passport_applications" -> 600
            "drivers_license_applications" -> 250
            "vaccination_applications" -> 350
            "bursary_applications" -> 300
            else -> 200
        }

        // Set values to views
        tvApplicationId.text = "APP-$applicationId"
        tvName.text = "$name $surname"
        tvEmail.text = email
        tvApplicationType.text = applicationType.replace("_", " ").capitalize()
        tvAmount.text = "R$amount"

        // Setup copy button
        btnCopyId.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Application ID", "APP-$applicationId")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Application ID copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        // Setup pay button
        btnPay.setOnClickListener {
            // Create the payment URL based on application type
            val paymentUrl = "https://paystack.com/pay/${applicationType.toLowerCase()}"

            // Open URL in browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
            startActivity(intent)
            finish()
        }
    }

    private fun initializeViews() {
        tvApplicationId = findViewById(R.id.tvApplicationId)
        btnCopyId = findViewById(R.id.btnCopyId)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvApplicationType = findViewById(R.id.tvApplicationType)
        tvAmount = findViewById(R.id.tvAmount)
        btnPay = findViewById(R.id.btnPay)
    }
}