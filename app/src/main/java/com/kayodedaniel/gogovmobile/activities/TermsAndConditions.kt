package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import java.io.BufferedReader
import java.io.InputStreamReader

class TermsAndConditions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions) // Sets the content view for the activity

        val termsTextView = findViewById<TextView>(R.id.text_terms_conditions) // Finds the TextView for displaying terms

        // Reads the file from res/raw
        val termsText: String = readTextFileFromRawResource(R.raw.terms_conditions) // Gets terms text from raw resource
        termsTextView.text = termsText // Sets the text of the TextView to the terms text

        val acceptButton = findViewById<Button>(R.id.button_accept_terms) // Finds the accept button
        acceptButton.setOnClickListener {
            showToast("T&C's Accepted") // Shows a toast message indicating acceptance of terms
            val intent = Intent(this, SettingsActivity::class.java) // Creates intent to navigate to SettingsActivity
            startActivity(intent) // Starts the SettingsActivity
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show() // Display a toast message
    }

    // Function to read the file from the raw resource
    private fun readTextFileFromRawResource(resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId) // Open the raw resource file
        val reader = BufferedReader(InputStreamReader(inputStream)) // Create a BufferedReader to read the file
        val stringBuilder = StringBuilder() // StringBuilder to accumulate the text
        var line: String?
        try {
            while ((reader.readLine().also { line = it }) != null) { // Read each line of the file
                stringBuilder.append(line) // Append the line to the StringBuilder
                stringBuilder.append("\n") // Add a newline character after each line
            }
            reader.close() // Close the reader
        } catch (e: Exception) {
            e.printStackTrace() // Print the stack trace in case of an exception
        }
        return stringBuilder.toString() // Return the accumulated text
    }
}