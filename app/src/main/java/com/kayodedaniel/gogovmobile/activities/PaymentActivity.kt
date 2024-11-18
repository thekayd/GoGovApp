package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
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
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"
    private val client = OkHttpClient()

    private lateinit var tilCardNumber: TextInputLayout
    private lateinit var tilCardHolder: TextInputLayout
    private lateinit var tilExpiry: TextInputLayout
    private lateinit var tilCvv: TextInputLayout
    private lateinit var btnPay: Button
    private lateinit var tvAmount: TextView
    private lateinit var notificationHelper: NotificationHelper

    private var paymentAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        notificationHelper = NotificationHelper(this)
        initializeViews()
        setupValidation()
        setupPayButton()
        setPaymentAmount()
    }

    private fun initializeViews() {
        tilCardNumber = findViewById(R.id.tilCardNumber)
        tilCardHolder = findViewById(R.id.tilCardHolder)
        tilExpiry = findViewById(R.id.tilExpiry)
        tilCvv = findViewById(R.id.tilCvv)
        btnPay = findViewById(R.id.btnPay)
        tvAmount = findViewById(R.id.tvAmount)
    }

    private fun setPaymentAmount() {
        // Get the calling activity's class name
        val callingActivity = when {
            isTaskRoot -> "Unknown" // If this is the root activity
            else -> this.javaClass.name
        }

        // Set amount based on the calling activity
        paymentAmount = when {
            callingActivity.contains("VaccinationRegistrationActivity") -> 600.00
            callingActivity.contains("PassportApplicationActivity") -> 600.00
            callingActivity.contains("BursaryApplicationActivity") -> 250.00
            callingActivity.contains("DriversLicenseActivity") -> 200.00
            else -> 200.00
        }

        // Update the TextView
        tvAmount.text = String.format("R %.2f", paymentAmount)
    }

    private fun setupValidation() {
        // Card Number Validation
        tilCardNumber.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s?.length != 16) {
                    tilCardNumber.error = "Card number must be 16 digits"
                } else {
                    tilCardNumber.error = null
                }
            }
        })

        // Expiry Date Validation
        tilExpiry.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                if (text.length == 2 && !text.contains("/")) {
                    tilExpiry.editText?.setText("$text/")
                    tilExpiry.editText?.setSelection(3)
                }
            }
            override fun afterTextChanged(s: Editable?) {
                if (!isValidExpiry(s.toString())) {
                    tilExpiry.error = "Invalid expiry date (MM/YY)"
                } else {
                    tilExpiry.error = null
                }
            }
        })

        // CVV Validation
        tilCvv.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s?.length != 3) {
                    tilCvv.error = "CVV must be 3 digits"
                } else {
                    tilCvv.error = null
                }
            }
        })
    }

    private fun setupPayButton() {
        btnPay.setOnClickListener {
            if (validateInputs()) {
                processPayment()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (tilCardNumber.editText?.text?.length != 16) {
            tilCardNumber.error = "Invalid card number"
            isValid = false
        }

        if (tilCardHolder.editText?.text.isNullOrBlank()) {
            tilCardHolder.error = "Card holder name required"
            isValid = false
        }

        if (!isValidExpiry(tilExpiry.editText?.text.toString())) {
            tilExpiry.error = "Invalid expiry date"
            isValid = false
        }

        if (tilCvv.editText?.text?.length != 3) {
            tilCvv.error = "Invalid CVV"
            isValid = false
        }

        return isValid
    }

    private fun isValidExpiry(expiry: String): Boolean {
        if (!expiry.matches(Regex("^(0[1-9]|1[0-2])/([0-9]{2})\$"))) return false

        val parts = expiry.split("/")
        val month = parts[0].toInt()
        val year = 2000 + parts[1].toInt()

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        return when {
            year < currentYear -> false
            year == currentYear && month < currentMonth -> false
            else -> true
        }
    }

    private fun processPayment() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val paymentData = JSONObject().apply {
                    put("amount", paymentAmount)
                    put("card_number", tilCardNumber.editText?.text.toString())
                    put("card_holder", tilCardHolder.editText?.text.toString())
                    put("expiry_date", tilExpiry.editText?.text.toString())
                    put("cvv", tilCvv.editText?.text.toString())
                    put("reference_number", generateReferenceNumber())
                    put("payment_status", "pending")
                }

                val request = Request.Builder()
                    .url("$supabaseUrl/payments")
                    .post(paymentData.toString().toRequestBody("application/json".toMediaType()))
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            notificationHelper.showPaymentProcessingNotification()
                            startActivity(Intent(this@PaymentActivity, VerifiedPaymentActivity::class.java))
                            finish()
                        }
                    } else {
                        throw Exception("Payment failed: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PaymentActivity, "Payment failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun generateReferenceNumber(): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val random = Random().nextInt(1000).toString().padStart(3, '0')
        return "PAY${dateFormat.format(Date())}$random"
    }
}