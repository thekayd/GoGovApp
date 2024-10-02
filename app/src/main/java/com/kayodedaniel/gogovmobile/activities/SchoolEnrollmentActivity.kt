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

class SchoolEnrollmentActivity : AppCompatActivity() {

    private lateinit var etStudentName: EditText
    private lateinit var etStudentSurname: EditText
    private lateinit var etStudentIdNumber: EditText
    private lateinit var spGrade: Spinner
    private lateinit var etSchoolName: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnUploadDocuments: Button
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_enrollment)

        // Initialize views
        etStudentName = findViewById(R.id.et_student_name)
        etStudentSurname = findViewById(R.id.et_student_surname)
        etStudentIdNumber = findViewById(R.id.et_student_id_number)
        spGrade = findViewById(R.id.sp_grade)
        etSchoolName = findViewById(R.id.et_school_name)
        etAddress = findViewById(R.id.et_address)
        btnUploadDocuments = findViewById(R.id.btn_upload_documents)
        btnSubmit = findViewById(R.id.btn_submit)

        // Handle document upload
        btnUploadDocuments.setOnClickListener {
            // Simulate document upload logic
            Toast.makeText(this, "Upload your school enrollment documents.", Toast.LENGTH_LONG).show()
        }

        // Submit the form
        btnSubmit.setOnClickListener {
            submitForm()
        }
    }

    private fun submitForm() {
        val name = etStudentName.text.toString().trim()
        val surname = etStudentSurname.text.toString().trim()
        val idNumber = etStudentIdNumber.text.toString().trim()
        val schoolName = etSchoolName.text.toString().trim()
        val address = etAddress.text.toString().trim()

        if (name.isNotEmpty() && surname.isNotEmpty() && idNumber.isNotEmpty() && schoolName.isNotEmpty() && address.isNotEmpty()) {
            Toast.makeText(this, "School enrollment submitted successfully!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_LONG).show()
        }
    }
}
