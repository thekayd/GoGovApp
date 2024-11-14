package com.kayodedaniel.gogovmobile.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.chatbotactivity.ChatBotActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ScheduleAppointmentActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var dateTextView: TextView
    private lateinit var timeSpinner: Spinner
    private lateinit var scheduleButton: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    private val client = OkHttpClient()
    private val TAG = "ScheduleAppointmentActivity"

    private val supabaseUrl =
        "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/scheduled_appointments"
    private val supabaseKey =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_appointment)

        initializeViews()
        setupListeners()
        loadUserEmail()
        setupBottomNavigation()
        setupTimeSpinner() // Setup time dropdown menu
    }

    private fun initializeViews() {
        nameEditText = findViewById(R.id.etName)
        surnameEditText = findViewById(R.id.etSurname)
        emailEditText = findViewById(R.id.etEmail)
        phoneEditText = findViewById(R.id.etPhone)
        dateTextView = findViewById(R.id.tvSelectedDate)
        timeSpinner = findViewById(R.id.spinnerTimeSlot) // Update time view to Spinner
        scheduleButton = findViewById(R.id.btnScheduleAppointment)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
    }

    private fun setupListeners() {
        dateTextView.setOnClickListener { showDatePicker() }
        scheduleButton.setOnClickListener { validateAndScheduleAppointment() }
    }

    private fun loadUserEmail() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("USER_EMAIL", null)
        emailEditText.setText(userEmail)
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.selectedItemId = R.id.nav_schedule
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePageActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_schedule -> true

                R.id.nav_view_appointment -> {
                    startActivity(Intent(this, ViewAppointmentActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_chat_bot -> {
                    startActivity(Intent(this, ChatBotActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true}
                else -> false
            }
        }
    }

    private fun setupTimeSpinner() {
        // Create time slots in 30-minute intervals from 10:00 AM to 4:00 PM
        val timeSlots = mutableListOf<String>()
        val startHour = 10
        val endHour = 16

        for (hour in startHour..endHour) {
            val timeFormat = String.format("%02d:%02d", hour, 0)
            val timeFormatHalf = String.format("%02d:%02d", hour, 30)
            timeSlots.add("$timeFormat")
            if (hour != endHour) timeSlots.add("$timeFormatHalf")
        }

        // Set up ArrayAdapter for Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeSpinner.adapter = adapter
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateTextView.text = dateFormat.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateAndScheduleAppointment() {
        val name = nameEditText.text.toString()
        val surname = surnameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phone = phoneEditText.text.toString()
        val selectedDate = dateTextView.text.toString()
        val selectedTime = timeSpinner.selectedItem?.toString() ?: ""

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || phone.isEmpty() || selectedDate == "Select Date" || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        } else {
            saveAppointment(name, surname, email, phone, selectedDate, selectedTime)
        }
    }

    private fun saveAppointment(
        name: String,
        surname: String,
        email: String,
        phone: String,
        date: String,
        time: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val json = JSONObject().apply {
                    put("email", email)
                    put("name", name)
                    put("surname", surname)
                    put("phone", phone)
                    put("appointment_date", date)
                    put("appointment_time", time)
                    put("reason", "yes")
                    put("status", "yes")
                    put("created_at", currentTimestamp)
                    put("updated_at", currentTimestamp)
                }

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(supabaseUrl)
                    .post(body)
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@ScheduleAppointmentActivity,
                                "Appointment scheduled successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            val errorBody = response.body?.string() ?: "No error body"
                            Log.e(TAG, "Failed to schedule appointment: ${response.code}")
                            Log.e(TAG, "Error body: $errorBody")
                            Toast.makeText(
                                this@ScheduleAppointmentActivity,
                                "Failed to schedule appointment: ${response.code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "IOException occurred: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ScheduleAppointmentActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception occurred: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ScheduleAppointmentActivity,
                        "An error occurred: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
