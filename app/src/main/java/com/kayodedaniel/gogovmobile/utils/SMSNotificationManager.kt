package com.kayodedaniel.gogovmobile.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SMSNotificationManager(private val context: Context) {
    private val TAG = "SMSNotificationManager"
    private val SEND_SMS_PERMISSION_REQUEST_CODE = 123


    fun formatPhoneNumber(phoneNumber: String): String {
        try {
            // Remove any spaces, hyphens, or other characters
            val cleaned = phoneNumber.replace(Regex("[^0-9]"), "")

            Log.d(TAG, "Original phone number: $phoneNumber")
            Log.d(TAG, "Cleaned phone number: $cleaned")

            // If number starts with 0, replace with +27
            val formattedNumber = if (cleaned.startsWith("0")) {
                "+27${cleaned.substring(1)}"
            } else {
                // If it doesn't start with +27, add it
                if (!cleaned.startsWith("27")) "+27$cleaned" else "+$cleaned"
            }

            Log.d(TAG, "Formatted phone number: $formattedNumber")
            return formattedNumber
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting phone number", e)
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendStatusUpdateSMS(phoneNumber: String, category: String, newStatus: String) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.SEND_SMS),
                SEND_SMS_PERMISSION_REQUEST_CODE
            )
            return
        }
        try {
            Log.d(TAG, "Attempting to send SMS")
            Log.d(TAG, "Phone Number: $phoneNumber")
            Log.d(TAG, "Category: $category")
            Log.d(TAG, "New Status: $newStatus")

            val formattedNumber = formatPhoneNumber(phoneNumber)
            val message = createStatusMessage(category, newStatus)

            Log.d(TAG, "Message to be sent: $message")

            val smsManager = SmsManager.getDefault()

            // If message is long, split it
            val messageParts = smsManager.divideMessage(message)

            smsManager.sendMultipartTextMessage(
                formattedNumber,
                null,
                messageParts,
                null,
                null
            )

            Log.d(TAG, "SMS sent successfully")

            // Log success
            Toast.makeText(
                context,
                "SMS notification sent successfully",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS", e)
            Toast.makeText(
                context,
                "Failed to send SMS: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createStatusMessage(category: String, newStatus: String): String {
        val formattedCategory = category.replace("_", " ").capitalize()
        return when (newStatus) {
            "In Progress" -> {
                val estimatedProcessingDate = LocalDate.now().plusDays(14)
                val formattedDate = estimatedProcessingDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                """
                GoGov Notification:
                Your $formattedCategory application status has been updated to: $newStatus.

                Estimated Processing Time: 14 days
                Expected Completion Date: $formattedDate

                We will keep you updated on the progress of your application.
                Log in to GoGov Mobile App for more details.
                
                For support: support@gogov.co.za
                """.trimIndent()
            }
            else -> {
                """
                GoGov Notification:
                Your $formattedCategory application status has been updated to: $newStatus.
                
                Log in to GoGov Mobile App for more details.
                
                For support: support@gogov.co.za
                """.trimIndent()
            }
        }
    }

    fun sendInProgressReminder(phoneNumber: String, category: String, daysSinceInProgress: Int) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.SEND_SMS),
                SEND_SMS_PERMISSION_REQUEST_CODE
            )
            return
        }

        try {
            val formattedNumber = formatPhoneNumber(phoneNumber)
            val formattedCategory = category.replace("_", " ").capitalize()

            val message = """
                GoGov Reminder:
                Your $formattedCategory application is still in progress.

                We are continuing to process your application. If you have any questions, please contact our support team.

                Log in to GoGov Mobile App for more details.
                
                For support: support@gogov.co.za
            """.trimIndent()

            val smsManager = SmsManager.getDefault()
            val messageParts = smsManager.divideMessage(message)

            smsManager.sendMultipartTextMessage(
                formattedNumber,
                null,
                messageParts,
                null,
                null
            )

            Log.d(TAG, "In-Progress Reminder SMS sent successfully")
            Toast.makeText(
                context,
                "In-Progress Reminder SMS sent successfully",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send In-Progress Reminder SMS", e)
            Toast.makeText(
                context,
                "Failed to send In-Progress Reminder SMS: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Handle permission request result in your Activity
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            SEND_SMS_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission was granted, you can send SMS now
                    Toast.makeText(
                        context,
                        "SMS permission granted",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Permission denied
                    Toast.makeText(
                        context,
                        "SMS permission denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }
}