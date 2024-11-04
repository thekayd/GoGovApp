package com.kayodedaniel.gogovmobile.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.model.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class UpdateScheduleFragment(private val schedule: Schedule) : BottomSheetDialogFragment() {

    private lateinit var dateTextView: TextView
    private lateinit var timeSpinner: Spinner
    private lateinit var updateButton: Button
    private val client = OkHttpClient()
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/scheduled_appointments"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_update_schedule, container, false)
        dateTextView = view.findViewById(R.id.tvSelectedDate)
        timeSpinner = view.findViewById(R.id.spinnerTime)
        updateButton = view.findViewById(R.id.btnUpdate)

        // Set initial values
        dateTextView.text = schedule.appointmentDate
        setupTimeSpinner()

        dateTextView.setOnClickListener { showDatePicker() }
        updateButton.setOnClickListener { updateSchedule() }

        return view
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            dateTextView.text = String.format("%d-%02d-%02d", year, month + 1, day)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupTimeSpinner() {
        val times = arrayOf("10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00")
        timeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, times)
    }

    private fun updateSchedule() {
        val date = dateTextView.text.toString()
        val time = timeSpinner.selectedItem.toString()

        // Construct JSON body with updated date and time
        val json = JSONObject().apply {
            put("appointment_date", date)
            put("appointment_time", time)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val request = Request.Builder()
                .url("$supabaseUrl?id=eq.${schedule.id}")
                .patch(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .addHeader("apikey", supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Schedule updated successfully", Toast.LENGTH_SHORT).show()
                        (activity as? ScheduleAdminActivity)?.loadSchedules() // Refresh schedules in activity
                        dismiss()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to update schedule", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
