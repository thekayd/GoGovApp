package com.kayodedaniel.gogovmobile.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kayodedaniel.gogovmobile.activities.PaymentActivity
import com.kayodedaniel.gogovmobile.R
import java.util.Calendar

class TestDateSelectionActivity : AppCompatActivity() {

    private lateinit var btnPickTestDate: Button
    private lateinit var tvSelectedDate: TextView
    private lateinit var rgTimeSlots: RadioGroup
    private lateinit var btnNextStep: Button
    private var selectedDate: String? = null
    private var selectedTimeSlot: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_date_selection)

        btnPickTestDate = findViewById(R.id.btnPickTestDate)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        rgTimeSlots = findViewById(R.id.rgTimeSlots)
        btnNextStep = findViewById(R.id.btnNextStep)

        // Date picker dialog
        btnPickTestDate.setOnClickListener {
            pickTestDate()
        }

        // Populates available time slots
        addAvailableTimeSlots()

        // Handle Next Step button click
        btnNextStep.setOnClickListener {
            if (selectedDate == null || selectedTimeSlot == null) {
                Toast.makeText(this, "Please select a date and time slot", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, PaymentActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun pickTestDate() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            tvSelectedDate.text = "Selected Date: $selectedDate"
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun addAvailableTimeSlots() {
        val timeSlots = arrayOf("09:00 AM", "11:00 AM", "01:00 PM", "03:00 PM")

        for (time in timeSlots) {
            val radioButton = RadioButton(this)
            radioButton.text = time
            radioButton.setOnClickListener {
                selectedTimeSlot = time
            }
            rgTimeSlots.addView(radioButton)
        }
    }
}
