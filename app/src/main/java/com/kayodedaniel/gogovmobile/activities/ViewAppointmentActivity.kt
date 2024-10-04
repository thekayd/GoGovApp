package com.kayodedaniel.gogovmobile.activities

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class ViewAppointmentActivity : AppCompatActivity() {

    private lateinit var appointmentDetailsTextView: TextView
    private val client = OkHttpClient()

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/scheduled_appointments?select=*"
    private val supabaseKey =  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_appointment)

        appointmentDetailsTextView = findViewById(R.id.tvAppointmentDetails)
        loadAppointmentDetails()
    }

    private fun loadAppointmentDetails() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val email = sharedPref.getString("USER_EMAIL", null)

        if (email != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = Request.Builder()
                        .url("$supabaseUrl&email=eq.$email")
                        .get()
                        .addHeader("apikey", supabaseKey)
                        .build()

                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()

                        if (response.isSuccessful && responseBody != null) {
                            val appointmentsArray = JSONArray(responseBody)
                            if (appointmentsArray.length() > 0) {
                                val appointment = appointmentsArray.getJSONObject(0)
                                val name = appointment.getString("name")
                                val surname = appointment.getString("surname")
                                val phone = appointment.getString("phone")
                                val date = appointment.getString("appointment_date")
                                val time = appointment.getString("appointment_time")
                                val status = appointment.getString("status")

                                withContext(Dispatchers.Main) {
                                    appointmentDetailsTextView.text = """
                                        Name: $name
                                        Surname: $surname
                                        Phone: $phone
                                        Appointment Date: $date
                                        Appointment Time: $time
                                        Status: $status
                                    """.trimIndent()
                                }
                            } else {
                                showNoAppointmentMessage()
                            }
                        } else {
                            showNoAppointmentMessage()
                        }
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ViewAppointmentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun showNoAppointmentMessage() {
        withContext(Dispatchers.Main) {
            appointmentDetailsTextView.text = "No scheduled appointments"
        }
    }
}
