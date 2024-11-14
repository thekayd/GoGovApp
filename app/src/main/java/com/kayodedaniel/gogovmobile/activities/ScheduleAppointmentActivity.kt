package com.kayodedaniel.gogovmobile.activities

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        initializeViews()
        setupListeners()
        loadUserEmail()
        createNotificationChannel()
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.i("NotificationPermission", "Notification permission granted.")
            } else {
                Log.e("NotificationPermission", "Notification permission denied.")
            }
        }
    }
    private fun showAppointmentNotification(name: String, date: String, time: String) {
        val notificationId = 1
        val appointmentDetails = "Appointment for $name on $date at $time"

        try {
            // First check if notifications are enabled for the app
            val notificationManager = NotificationManagerCompat.from(this)
            if (!notificationManager.areNotificationsEnabled()) {
                Log.e("Notification", "Notifications are disabled for the app")
                Toast.makeText(
                    this,
                    "Please enable notifications in system settings to receive appointment reminders",
                    Toast.LENGTH_LONG
                ).show()
                // Optionally open notification settings
                val intent = Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
                return
            }

            // Check for POST_NOTIFICATIONS permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e("Notification", "POST_NOTIFICATIONS permission not granted")
                    // Request the permission
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1
                    )
                    return
                }
            }

            // Create the notification
            val builder = NotificationCompat.Builder(this, "appointment_channel")
                .setSmallIcon(R.drawable.ic_notification) // Make sure this exists
                .setContentTitle("Appointment Scheduled")
                .setContentText(appointmentDetails)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            // Create a PendingIntent for when the notification is tapped
            val intent = Intent(this, ViewAppointmentActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)

            // Show the notification
            notificationManager.notify(notificationId, builder.build())
            Log.d("Notification", "Notification sent successfully")

        } catch (e: Exception) {
            Log.e("Notification", "Error showing notification: ${e.message}", e)
            Toast.makeText(
                this,
                "Failed to show notification: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    // Update createNotificationChannel to include logging
    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "appointment_channel",
                    "Appointment Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for appointment notifications"
                    enableLights(true)
                    enableVibration(true)
                }

                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d("Notification", "Notification channel created successfully")
            }
        } catch (e: Exception) {
            Log.e("Notification", "Error creating notification channel: ${e.message}", e)
        }
    }
    private fun scheduleReminder(name: String, surname: String, date: String, time: String) {
        // For Android 12+ check for SCHEDULE_EXACT_ALARM permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
                // Request permission from the user
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        // Alarm scheduling code remains the same
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val appointmentDateTime = dateFormat.parse("$date $time")?.time ?: return
        val reminderTime = appointmentDateTime - 24 * 60 * 60 * 1000  // 24 hours before

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("name", name)
            putExtra("surname", surname)
            putExtra("appointment_time", time)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
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
                            scheduleReminder(name, surname, date, time)
                            showAppointmentNotification(name, date, time)
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
