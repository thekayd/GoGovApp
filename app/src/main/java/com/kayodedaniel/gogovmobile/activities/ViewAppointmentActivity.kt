package com.kayodedaniel.gogovmobile.activities

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.adapter.AppointmentAdapter
import com.kayodedaniel.gogovmobile.model.Appointment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ViewAppointmentActivity : AppCompatActivity(), AppointmentAdapter.OnAppointmentClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentAdapter
    private val client = OkHttpClient()

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/scheduled_appointments?select=*"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_appointment)

        recyclerView = findViewById(R.id.recyclerViewAppointments)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initializes the adapter with an empty list
        adapter = AppointmentAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        loadAppointments()
    }

    // loads the appoitment details for admin
    fun loadAppointments() {
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

                    Log.d(TAG, "Sending request to: ${request.url}")

                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()

                        if (response.isSuccessful && responseBody != null) {
                            Log.d(TAG, "Received successful response: $responseBody")
                            val appointmentsArray = JSONArray(responseBody)
                            val appointments = mutableListOf<Appointment>()

                            // receiving array list for the json
                            for (i in 0 until appointmentsArray.length()) {
                                val appointmentJson = appointmentsArray.getJSONObject(i)
                                val appointment = Appointment(
                                    appointmentJson.getString("id"),
                                    appointmentJson.getString("name"),
                                    appointmentJson.getString("surname"),
                                    appointmentJson.getString("appointment_date"),
                                    appointmentJson.getString("appointment_time"),
                                    appointmentJson.getString("status")
                                )
                                appointments.add(appointment)
                            }

                            Log.d(TAG, "Parsed ${appointments.size} appointments")

                            withContext(Dispatchers.Main) {
                                adapter.updateAppointments(appointments)
                                Log.d(TAG, "Updated adapter with new appointments")
                            }
                        } else {
                            Log.e(TAG, "Failed to load appointments. Response code: ${response.code}. Body: $responseBody")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@ViewAppointmentActivity, "Failed to load appointments. Error: ${response.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception occurred while loading appointments", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ViewAppointmentActivity, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Log.e(TAG, "User email is null")
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
        }
    }

    // activiating fragment for update
    override fun onUpdateClick(appointment: Appointment) {
        val updateFragment = UpdateAppointmentFragment(appointment)
        updateFragment.show(supportFragmentManager, "UpdateAppointmentFragment")
    }

    // cancels users appointments
    override fun onCancelClick(appointment: Appointment) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment?")
            .setPositiveButton("Yes") { dialog, _ ->
                cancelAppointment(appointment)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // cancels users appointment method
    private fun cancelAppointment(appointment: Appointment) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("status", "Canceled")
                }

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("$supabaseUrl&id=eq.${appointment.id}")
                    .patch(body)
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            adapter.removeAppointment(appointment)
                            Toast.makeText(this@ViewAppointmentActivity, "Appointment canceled", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ViewAppointmentActivity, "Failed to cancel appointment", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ViewAppointmentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// fragment class for updating
class UpdateAppointmentFragment(private val appointment: Appointment) : BottomSheetDialogFragment() {

    private lateinit var dateTextView: TextView
    private lateinit var timeSpinner: Spinner
    private lateinit var updateButton: Button

    private val client = OkHttpClient()

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/scheduled_appointments"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_update_appointment, container, false)

        dateTextView = view.findViewById(R.id.tvSelectedDateUpdate)
        timeSpinner = view.findViewById(R.id.spinnerTimeSlotUpdate)
        updateButton = view.findViewById(R.id.btnUpdateAppointment)

        dateTextView.text = appointment.appointment_date
        setupTimeSpinner()

        dateTextView.setOnClickListener { showDatePicker() }

        updateButton.setOnClickListener {
            val selectedDate = dateTextView.text.toString()
            val selectedTime = timeSpinner.selectedItem?.toString() ?: ""

            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(context, "Please select a valid date and time", Toast.LENGTH_SHORT).show()
            } else {
                updateAppointment(selectedDate, selectedTime)
            }
        }

        return view
    }

    private fun setupTimeSpinner() {
        val timeSlots = mutableListOf<String>()
        val startHour = 10
        val endHour = 16

        for (hour in startHour..endHour) {
            val timeFormat = String.format("%02d:%02d", hour, 0)
            val timeFormatHalf = String.format("%02d:%02d", hour, 30)
            timeSlots.add(timeFormat)
            if (hour != endHour) timeSlots.add(timeFormatHalf)
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeSlots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeSpinner.adapter = adapter

        // Set the selected time
        val selectedTimeIndex = timeSlots.indexOf(appointment.appointment_time)
        if (selectedTimeIndex != -1) {
            timeSpinner.setSelection(selectedTimeIndex)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time)

            dateTextView.text = selectedDate
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun updateAppointment(date: String, time: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("appointment_date", date)
                    put("appointment_time", time)
                } // puts the json strings and adding them intot he database, changing the original values to updated values

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("$supabaseUrl?id=eq.${appointment.id}")
                    .patch(body)
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Content-Type", "application/json")
                    .build()

                // intents and toasts for success
                client.newCall(request).execute().use { response ->
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Appointment updated", Toast.LENGTH_SHORT).show()
                            dismiss()
                            (activity as? ViewAppointmentActivity)?.loadAppointments()
                        } else {
                            Toast.makeText(requireContext(), "Failed to update appointment", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}