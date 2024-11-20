package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.adapter.FAQAdapter
import java.io.BufferedReader
import java.io.InputStreamReader

class FAQActivity : AppCompatActivity() {
    private lateinit var faqAdapter: FAQAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faqactivity)

        // Read FAQs from raw resource
        val faqText = readTextFileFromRawResource(R.raw.faq)
        val faqList = parseFAQs(faqText)

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_view_faqs)
        faqAdapter = FAQAdapter(faqList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = faqAdapter

        // Continue Button
        val acceptButton = findViewById<Button>(R.id.button_continue)
        acceptButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun parseFAQs(faqText: String): List<FAQItem> {
        val faqList = mutableListOf<FAQItem>()
        val lines = faqText.split("\n")
        var currentQuestion: String? = null

        for (line in lines) {
            when {
                line.startsWith("** ") && line.endsWith(" **") -> {
                    currentQuestion = line.trim('*', ' ')
                }
                line.isNotBlank() && currentQuestion != null -> {
                    faqList.add(FAQItem(currentQuestion, line.trim()))
                    currentQuestion = null
                }
            }
        }
        return faqList
    }

    private fun readTextFileFromRawResource(resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        try {
            while ((reader.readLine().also { line = it }) != null) {
                stringBuilder.append(line)
                stringBuilder.append("\n")
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}

// FAQ Item Data Class
data class FAQItem(val question: String, val answer: String)