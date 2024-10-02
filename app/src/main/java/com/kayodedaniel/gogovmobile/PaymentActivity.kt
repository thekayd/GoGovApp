package com.kayodedaniel.gogovmobile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PaymentActivity : AppCompatActivity() {

    private lateinit var etCardNumber: EditText
    private lateinit var etExpiryDate: EditText
    private lateinit var etCvv: EditText
    private lateinit var etNameOnCard: EditText
    private lateinit var btnSubmitPayment: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        etCardNumber = findViewById(R.id.etCardNumber)
        etExpiryDate = findViewById(R.id.etExpiryDate)
        etCvv = findViewById(R.id.etCvv)
        etNameOnCard = findViewById(R.id.etNameOnCard)
        btnSubmitPayment = findViewById(R.id.btnSubmitPayment)

        // Handle payment submission
        btnSubmitPayment.setOnClickListener {
            submitPayment()
        }
    }

    private fun submitPayment() {
        val cardNumber = etCardNumber.text.toString()
        val expiryDate = etExpiryDate.text.toString()
        val cvv = etCvv.text.toString()
        val nameOnCard = etNameOnCard.text.toString()

        // Perform basic validation
        if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty() || nameOnCard.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Handle payment logic (e.g., send card details to payment gateway)
        // For this example, we'll just show a success message
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()

        // After successful payment, redirect to a confirmation page or home page
        // finish() can be used to close the activity or you can navigate elsewhere.
    }
}
