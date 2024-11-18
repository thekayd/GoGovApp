package com.kayodedaniel.gogovmobile.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.adapter.FeedbackAdapter
import com.kayodedaniel.gogovmobile.model.UserFeedback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class AdminUserFeedbackActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var feedbackAdapter: FeedbackAdapter
    private val client = OkHttpClient()
    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/user_feedback"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_feedback)

        recyclerView = findViewById(R.id.recyclerViewFeedback)
        recyclerView.layoutManager = LinearLayoutManager(this)
        feedbackAdapter = FeedbackAdapter(emptyList())
        recyclerView.adapter = feedbackAdapter

        fetchFeedback()
    }

    private fun fetchFeedback() {
        CoroutineScope(Dispatchers.IO).launch {
            val request = Request.Builder()
                .url(supabaseUrl)
                .get()
                .addHeader("apikey", supabaseKey)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val feedbackList = mutableListOf<UserFeedback>()
                    val jsonArray = JSONArray(response.body?.string() ?: "")
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val rating = if (jsonObject.isNull("rating")) 0.0 else jsonObject.getDouble("rating")
                        val feedback = UserFeedback(
                            id = jsonObject.getString("id"),
                            email = jsonObject.getString("email"),
                            phone = jsonObject.getString("phone"),
                            rating = rating.toFloat(),
                            feedbackText = jsonObject.getString("feedback")
                        )
                        feedbackList.add(feedback)
                    }
                    withContext(Dispatchers.Main) {
                        feedbackAdapter.updateFeedbackList(feedbackList)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminUserFeedbackActivity, "Failed to load feedback", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
