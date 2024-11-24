package com.kayodedaniel.gogovmobile.activities

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.adapter.ScheduleAdapter
import com.kayodedaniel.gogovmobile.model.Schedule
import com.kayodedaniel.gogovmobile.utils.SMSNotificationManager
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

class ScheduleAdminActivity : AppCompatActivity(), ScheduleAdapter.OnScheduleClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private val client = OkHttpClient()
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/scheduled_appointments?select=*"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    private val smsManager by lazy { SMSNotificationManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_admin)

        recyclerView = findViewById(R.id.recyclerViewSchedules)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ScheduleAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        loadSchedules()
    }
    // shows dialog for calander
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showStatusDialog(
        schedule: Schedule,
        newStatus: String
    ) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Status Change")
            .setMessage("Are you sure you want to change the status to $newStatus?")
            .setPositiveButton("Yes") { _, _ ->
                updateScheduleStatus(schedule, newStatus)
            }
            .setNegativeButton("No", null)
            .show()
    }

    //loads all the schedules for admin
    fun loadSchedules() {
        CoroutineScope(Dispatchers.IO).launch {
            val request = Request.Builder()
                .url(supabaseUrl)
                .get()
                .addHeader("apikey", supabaseKey)
                .build()

            // call the requests and gets the json string for each item
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    val schedulesArray = JSONArray(responseBody)
                    val schedules = mutableListOf<Schedule>()
                    for (i in 0 until schedulesArray.length()) {
                        val scheduleJson = schedulesArray.getJSONObject(i)
                        val schedule = Schedule(
                            scheduleJson.getString("id"),
                            scheduleJson.getString("name"),
                            scheduleJson.getString("surname"),
                            scheduleJson.getString("appointment_date"),
                            scheduleJson.getString("appointment_time"),
                            scheduleJson.getString("status"),
                            scheduleJson.optString("phone")
                        )
                        schedules.add(schedule)
                    }
                    withContext(Dispatchers.Main) {
                        adapter.updateSchedules(schedules)
                    }
                }
            }
        }
    }

    // approval method
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onApproveClick(schedule: Schedule) {
        updateScheduleStatus(schedule, "Approved")
    }

    // decline method
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDeclineClick(schedule: Schedule) {
        updateScheduleStatus(schedule, "Declined")
    }

    //scheduling method
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRescheduleClick(schedule: Schedule) {
        val fragment = UpdateScheduleFragment(schedule) { updatedSchedule ->
            updateScheduleStatus(updatedSchedule, "Rescheduled")
        }
        fragment.show(supportFragmentManager, "UpdateScheduleFragment")
    }


    // method for updating schedules for later dates
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateScheduleStatus(schedule: Schedule, newStatus: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val json = JSONObject().apply {
                put("status", newStatus)
                if (newStatus == "Rescheduled") {
                    put("appointment_date", schedule.appointmentDate)
                    put("appointment_time", schedule.appointmentTime)
                }
            }

            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$supabaseUrl&id=eq.${schedule.id}")
                .patch(body)
                .addHeader("apikey", supabaseKey)
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        if (newStatus == "Rescheduled") {
                            sendRescheduleSMS(
                                schedule.phone,
                                schedule.appointmentDate,
                                schedule.appointmentTime
                            )
                        } else {
                            sendStatusUpdateSMS(schedule.phone, newStatus)
                        }
                        loadSchedules()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ScheduleAdminActivity,
                            "Failed to update status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    // method for using phone number to send verified sms messages
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendRescheduleSMS(phoneNumber: String, date: String, time: String) {
        val formattedPhone = smsManager.formatPhoneNumber(phoneNumber)
        val message = "Your appointment has been rescheduled to $date at $time. Thank you for using GoGov."
        smsManager.sendStatusUpdateSMS(formattedPhone, "Appointment Reschedule", message)
    }

    // method for sending updated appointment sms messages
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendStatusUpdateSMS(phoneNumber: String, newStatus: String) {
        val formattedPhone = smsManager.formatPhoneNumber(phoneNumber)
        val message = when (newStatus) {
            "Approved" -> "Your appointment has been approved. Thank you for using GoGov."
            "Declined" -> "Unfortunately, your appointment has been declined. Please contact support."
            else -> "Your appointment status has been updated."
        }
        smsManager.sendStatusUpdateSMS(formattedPhone, "Appointment", message)
    }

}
