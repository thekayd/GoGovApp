package com.kayodedaniel.gogovmobile.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.adapter.AppointmentAdapter
import com.kayodedaniel.gogovmobile.model.Appointment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class ViewAppointmentActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val client = OkHttpClient()

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/scheduled_appointments?select=*"
    private val supabaseKey =   "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_appointment)

        recyclerView = findViewById(R.id.recyclerViewAppointments)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadAppointments()
    }

    private fun loadAppointments() {
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
                            val appointments = mutableListOf<Appointment>()

                            for (i in 0 until appointmentsArray.length()) {
                                val appointmentJson = appointmentsArray.getJSONObject(i)
                                val appointment = Appointment(
                                    appointmentJson.getString("name"),
                                    appointmentJson.getString("surname"),
                                    appointmentJson.getString("appointment_date"),
                                    appointmentJson.getString("appointment_time"),
                                    appointmentJson.getString("status")
                                )
                                appointments.add(appointment)
                            }

                            withContext(Dispatchers.Main) {
                                recyclerView.adapter = AppointmentAdapter(appointments)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@ViewAppointmentActivity, "Failed to load appointments", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ViewAppointmentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "No user email found", Toast.LENGTH_SHORT).show()
        }
    }
}
