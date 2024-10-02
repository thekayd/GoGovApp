package com.kayodedaniel.gogovmobile.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import java.util.Calendar

class VaccinationRegistrationActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etIdNumber: EditText
    private lateinit var spVaccinationType: Spinner
    private lateinit var spVaccinationCenter: Spinner
    private lateinit var btnSelectDate: Button
    private lateinit var btnSubmit: Button
    private lateinit var selectedDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vaccination_registration)

        // Initialize views
        etName = findViewById(R.id.et_name)
        etIdNumber = findViewById(R.id.et_id_number)
        spVaccinationType = findViewById(R.id.sp_vaccination_type)
        spVaccinationCenter = findViewById(R.id.sp_vaccination_center)
        btnSelectDate = findViewById(R.id.btn_select_date)
        btnSubmit = findViewById(R.id.btn_submit)

        // Date Picker for selecting vaccination date
        btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Submit form data
        btnSubmit.setOnClickListener {
            submitForm()
        }
    }

    // Function to show DatePickerDialog
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            btnSelectDate.text = selectedDate
        }, year, month, day)

        datePickerDialog.show()
    }

    // Function to handle form submission
    private fun submitForm() {
        val name = etName.text.toString().trim()
        val idNumber = etIdNumber.text.toString().trim()
        val vaccinationType = spVaccinationType.selectedItem.toString()
        val vaccinationCenter = spVaccinationCenter.selectedItem.toString()

        if (name.isNotEmpty() && idNumber.isNotEmpty() && this::selectedDate.isInitialized) {
            // Simulate form submission
            Toast.makeText(this, "Vaccination registration successful!", Toast.LENGTH_LONG).show()
            // You can add code here to send the data to a server or save locally
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
        }
    }
}
