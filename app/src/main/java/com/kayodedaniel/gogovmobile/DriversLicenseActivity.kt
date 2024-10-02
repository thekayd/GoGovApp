package com.kayodedaniel.gogovmobile

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class DriversLicenseActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etIdNumber: EditText
    private lateinit var spGender: Spinner
    private lateinit var spProvince: Spinner
    private lateinit var etAddress: EditText
    private lateinit var etCity: EditText
    private lateinit var etPostcode: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var spLicenseCategory: Spinner
    private lateinit var spTestCenter: Spinner
    private lateinit var btnPickDob: Button
    private lateinit var cbNDA: CheckBox
    private lateinit var btnSubmit: Button

    private var selectedDob: String? = null
    private var idDocumentUri: Uri? = null
    private var passportPhotoUri: Uri? = null
    private var proofOfAddressUri: Uri? = null
    private var eyeTestCertificateUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers_license)

        // Initialize views
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etIdNumber = findViewById(R.id.etIdNumber)
        spGender = findViewById(R.id.spGender)
        spProvince = findViewById(R.id.spProvince)
        etAddress = findViewById(R.id.etAddress)
        etCity = findViewById(R.id.etCity)
        etPostcode = findViewById(R.id.etPostcode)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        spLicenseCategory = findViewById(R.id.spLicenseCategory)
        spTestCenter = findViewById(R.id.spTestCenter)
        btnPickDob = findViewById(R.id.btnPickDob)
        cbNDA = findViewById(R.id.cbNDA)
        btnSubmit = findViewById(R.id.btnSubmit)

        // Date Picker for Date of Birth
        btnPickDob.setOnClickListener {
            pickDateOfBirth()
        }

        // File upload buttons
        findViewById<Button>(R.id.btnUploadIdDocument).setOnClickListener {
            pickFile(1)
        }
        findViewById<Button>(R.id.btnUploadPassportPhoto).setOnClickListener {
            pickFile(2)
        }
        findViewById<Button>(R.id.btnUploadProofOfAddress).setOnClickListener {
            pickFile(3)
        }
        findViewById<Button>(R.id.btnUploadEyeTestCertificate).setOnClickListener {
            pickFile(4)
        }

        // Submit Button Click Listener
        btnSubmit.setOnClickListener {
            submitForm()
        }
    }

    // Function to pick Date of Birth
    private fun pickDateOfBirth() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDob = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            btnPickDob.text = selectedDob
        }, year, month, day)
        datePickerDialog.show()
    }

    // Function to pick a file (like ID Document, Passport Photo, etc.)
    private fun pickFile(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val fileUri = data.data
            when (requestCode) {
                1 -> { // ID Document
                    idDocumentUri = fileUri
                    findViewById<Button>(R.id.btnUploadIdDocument).text = "ID Document Uploaded"
                }
                2 -> { // Passport Photo
                    passportPhotoUri = fileUri
                    findViewById<Button>(R.id.btnUploadPassportPhoto).text = "Passport Photo Uploaded"
                }
                3 -> { // Proof of Address
                    proofOfAddressUri = fileUri
                    findViewById<Button>(R.id.btnUploadProofOfAddress).text = "Proof of Address Uploaded"
                }
                4 -> { // Eye Test Certificate
                    eyeTestCertificateUri = fileUri
                    findViewById<Button>(R.id.btnUploadEyeTestCertificate).text = "Eye Test Certificate Uploaded"
                }
            }
        }
    }

    // Function to validate and submit the form
    private fun submitForm() {
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val idNumber = etIdNumber.text.toString()
        val email = etEmail.text.toString()
        val phoneNumber = etPhoneNumber.text.toString()

        // Add additional validations as required (e.g. email format, required fields)

        // Check if NDA is agreed
        if (!cbNDA.isChecked) {
            Toast.makeText(this, "You must agree to the NDA", Toast.LENGTH_SHORT).show()
            return
        }

        // Assuming all data is valid, proceed with form submission
        // Here, you can collect all data and send it to your backend
        Toast.makeText(this, "Form Submitted", Toast.LENGTH_SHORT).show()

        // Code to send data to the backend (not implemented here)
    }
}
