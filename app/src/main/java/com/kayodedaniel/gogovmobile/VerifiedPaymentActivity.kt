package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class VerifiedPaymentActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var checkmark: ImageView
    private lateinit var statusText: TextView
    private lateinit var btnBackToHome: Button
    private lateinit var btnDownloadReceipt: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verified_payment)

        initializeViews()
        animateVerification()
        setupButtons()
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progressBar)
        checkmark = findViewById(R.id.checkmark)
        statusText = findViewById(R.id.statusText)
        btnBackToHome = findViewById(R.id.btnBackToHome)
        btnDownloadReceipt = findViewById(R.id.btnDownloadReceipt)
    }

    private fun animateVerification() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // Simulate processing
            progressBar.visibility = View.GONE
            checkmark.visibility = View.VISIBLE
            statusText.text = "Thank you for your payment, your payment will be verified soon."
            btnBackToHome.visibility = View.VISIBLE
            btnDownloadReceipt.visibility = View.VISIBLE
        }
    }

    private fun setupButtons() {
        btnBackToHome.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        btnDownloadReceipt.setOnClickListener {
            generateAndShareReceipt()
        }
    }

    private fun generateAndShareReceipt() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val receiptContent = """
                    Payment Receipt
                    ==============
                    Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
                    Amount: R 600.00
                    Status: Pending Verification
                    Reference: ${intent.getStringExtra("reference_number") ?: "N/A"}
                    
                    Thank you for your payment.
                    This receipt is automatically generated.
                """.trimIndent()

                val file = File(cacheDir, "receipt_${System.currentTimeMillis()}.txt")
                FileOutputStream(file).use { it.write(receiptContent.toByteArray()) }

                val uri = FileProvider.getUriForFile(
                    this@VerifiedPaymentActivity,
                    "${packageName}.provider",
                    file
                )

                withContext(Dispatchers.Main) {
                    shareReceipt(uri)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle error
                }
            }
        }
    }

    private fun shareReceipt(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Receipt"))
    }
}