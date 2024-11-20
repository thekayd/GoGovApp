package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.adapter.FAQAdapter
import java.io.*

class FAQActivity : AppCompatActivity() {
    private val TAG = "FAQActivity"
    private lateinit var faqAdapter: FAQAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faqactivity)

        try {
            // Read FAQs from raw resource
            val faqText = readTextFileFromRawResource(R.raw.faq)
            val faqList = parseFAQs(faqText)

            // Setup RecyclerView
            recyclerView = findViewById(R.id.recycler_view_faqs)
            faqAdapter = FAQAdapter(faqList)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = faqAdapter
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FAQ content: ${e.message}", e)
        }

        // Download Guide Button
        val downloadGuideButton = findViewById<Button>(R.id.button_download_guide)
        downloadGuideButton.setOnClickListener {
            downloadPortalGuide()
        }

        // Continue Button
        val acceptButton = findViewById<Button>(R.id.button_continue)
        acceptButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun downloadPortalGuide() {
        try {
            // Copy PDF from raw resources to app's cache directory
            val inputStream = resources.openRawResource(R.raw.portal_guide)
            val file = File(cacheDir, "portal_guide.pdf")
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(TAG, "PDF successfully copied to cacheDir: ${file.absolutePath}")

            // Ensure file exists before proceeding
            if (!file.exists()) {
                Log.e(TAG, "PDF file not found: ${file.absolutePath}")
                showToast("File not found.")
                return
            }

            // Start PDFViewerActivity and pass the file path
            val intent = Intent(this, PDFViewerActivity::class.java)
            intent.putExtra("PDF_PATH", file.absolutePath)
            startActivity(intent)

        } catch (e: Exception) {
            Log.e(TAG, "Error downloading portal guide: ${e.message}", e)
            showToast("Error downloading guide: ${e.message ?: "Unknown error"}")
        }
    }


    private fun parseFAQs(faqText: String): List<FAQItem> {
        val faqList = mutableListOf<FAQItem>()
        val lines = faqText.split("\n")
        var currentQuestion: String? = null

        for ((index, line) in lines.withIndex()) {
            try {
                when {
                    line.startsWith("** ") && line.endsWith(" **") -> {
                        currentQuestion = line.trim('*', ' ')
                    }
                    line.isNotBlank() && currentQuestion != null -> {
                        faqList.add(FAQItem(currentQuestion, line.trim()))
                        currentQuestion = null
                    }
                    else -> {
                        Log.w(TAG, "Unexpected or empty line at index $index: $line")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing FAQ at line $index: ${e.message}", e)
            }
        }
        return faqList
    }

    private fun readTextFileFromRawResource(resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        try {
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading text file from resource: ${e.message}", e)
        } finally {
            try {
                reader.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing reader: ${e.message}", e)
            }
        }
        return stringBuilder.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}

// FAQ Item Data Class
data class FAQItem(val question: String, val answer: String)
