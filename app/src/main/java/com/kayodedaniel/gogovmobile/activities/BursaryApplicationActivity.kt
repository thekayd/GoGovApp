package com.kayodedaniel.gogovmobile.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kayodedaniel.gogovmobile.R

class BursaryApplicationActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etIdNumber: EditText
    private lateinit var etCourse: EditText
    private lateinit var spProvince: Spinner
    private lateinit var spGrade: Spinner
    private lateinit var btnUploadDocuments: Button
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bursary_application)

        // Initialize views
        etName = findViewById(R.id.et_name)
        etSurname = findViewById(R.id.et_surname)
        etIdNumber = findViewById(R.id.et_id_number)
        etCourse = findViewById(R.id.et_course)
        spProvince = findViewById(R.id.sp_province)
        spGrade = findViewById(R.id.sp_grade)
        btnUploadDocuments = findViewById(R.id.btn_upload_documents)
        btnSubmit = findViewById(R.id.btn_submit)

        // Handle document upload
        btnUploadDocuments.setOnClickListener {
            // Simulate document upload logic
            Toast.makeText(this, "Upload your bursary documents.", Toast.LENGTH_LONG).show()
        }

        // Submit the form
        btnSubmit.setOnClickListener {
            submitForm()
        }
    }

    private fun submitForm() {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val idNumber = etIdNumber.text.toString().trim()
        val course = etCourse.text.toString().trim()

        if (name.isNotEmpty() && surname.isNotEmpty() && idNumber.isNotEmpty() && course.isNotEmpty()) {
            Toast.makeText(this, "Bursary application submitted successfully!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_LONG).show()
        }
    }
}
