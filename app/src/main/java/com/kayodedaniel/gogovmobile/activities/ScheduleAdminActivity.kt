package com.kayodedaniel.gogovmobile.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.adapter.ScheduleAdapter
import com.kayodedaniel.gogovmobile.model.Schedule
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_admin)

        recyclerView = findViewById(R.id.recyclerViewSchedules)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ScheduleAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        loadSchedules()
    }

    fun loadSchedules() {
        CoroutineScope(Dispatchers.IO).launch {
            val request = Request.Builder()
                .url(supabaseUrl)
                .get()
                .addHeader("apikey", supabaseKey)
                .build()

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
                            scheduleJson.getString("status")
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

    override fun onApproveClick(schedule: Schedule) {
        updateScheduleStatus(schedule, "Approved")
    }

    override fun onDeclineClick(schedule: Schedule) {
        updateScheduleStatus(schedule, "Declined")
    }

    override fun onRescheduleClick(schedule: Schedule) {
        val fragment = UpdateScheduleFragment(schedule)
        fragment.show(supportFragmentManager, "UpdateScheduleFragment")
    }

    private fun updateScheduleStatus(schedule: Schedule, newStatus: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val json = JSONObject().apply {
                put("status", newStatus)
            }

            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$supabaseUrl&id=eq.${schedule.id}")
                .patch(body)
                .addHeader("apikey", supabaseKey)
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful) {
                    loadSchedules()
                }
            }
        }
    }
}
