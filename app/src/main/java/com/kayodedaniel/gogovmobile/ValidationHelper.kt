package com.kayodedaniel.gogovmobile

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.activities.PaymentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*
import android.util.Base64
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class ValidationHelper {
    companion object {
        // Validates South African ID number
        fun isValidSAID(idNumber: String): Boolean {
            if (idNumber.length != 13 || !idNumber.all { it.isDigit() }) {
                return false
            }

            try {
                // Extracts date components
                val year = idNumber.substring(0, 2).toInt()
                val month = idNumber.substring(2, 4).toInt()
                val day = idNumber.substring(4, 6).toInt()

                // Validates date
                if (month < 1 || month > 12 || day < 1 || day > 31) {
                    return false
                }

                // Validates citizenship digit (positions 10)
                val citizenship = idNumber[10].toString().toInt()
                if (citizenship != 0 && citizenship != 1) {
                    return false
                }

                // Luhn algorithm validation
                var sum = 0
                var alternate = false
                for (i in idNumber.length - 1 downTo 0) {
                    var n = idNumber[i].toString().toInt()
                    if (alternate) {
                        n *= 2
                        if (n > 9) {
                            n = n - 9
                        }
                    }
                    sum += n
                    alternate = !alternate
                }

                return sum % 10 == 0
            } catch (e: Exception) {
                return false
            }
        }

        // Validates South African phone number
        fun isValidSAPhoneNumber(phoneNumber: String): Boolean {
            val cleanNumber = phoneNumber.replace("\\s".toRegex(), "")
            // South African phone number patterns
            val patterns = listOf(
                "^\\+27[6-8][0-9]{8}$", // International format
                "^0[6-8][0-9]{8}$"      // Local format
            )
            return patterns.any { cleanNumber.matches(it.toRegex()) }
        }

        // Validates email address
        fun isValidEmail(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        // Validates postal code
        fun isValidPostalCode(postalCode: String): Boolean {
            return postalCode.matches("^\\d{4}$".toRegex())
        }

        // Validates name (alphabetic characters and spaces only)
        fun isValidName(name: String): Boolean {
            return name.matches("^[A-Za-z\\s'-]{2,50}$".toRegex())
        }

        // Validates address (minimum length and basic characters)
        fun isValidAddress(address: String): Boolean {
            return address.matches("^[A-Za-z0-9\\s,.-]{5,100}$".toRegex())
        }

        // Validates city name
        fun isValidCity(city: String): Boolean {
            return city.matches("^[A-Za-z\\s-]{2,50}$".toRegex())
        }

        // Validates age for vaccination (must be at least 12 years old)
        @RequiresApi(Build.VERSION_CODES.O)
        fun isValidAgeForVaccination(dateOfBirth: String): Boolean {
            try {
                val dob = LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_DATE)
                val age = Period.between(dob, LocalDate.now()).years
                return age >= 12
            } catch (e: Exception) {
                return false
            }
        }
    }
}