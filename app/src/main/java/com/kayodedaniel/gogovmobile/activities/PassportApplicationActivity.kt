package com.kayodedaniel.gogovmobile.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kayodedaniel.gogovmobile.R


class PassportApplicationActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etIdNumber: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerProvince: Spinner
    private lateinit var etAddress: EditText
    private lateinit var etCity: EditText
    private lateinit var etPostcode: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnUploadPassportPhoto: Button
    private lateinit var btnUploadProofOfAddress: Button
    private lateinit var btnUploadIdDocument: Button
    private lateinit var btnSubmitPassportApplication: Button
    private lateinit var checkboxNda: CheckBox

    private val REQUEST_CODE_UPLOAD = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passport_application)

        // Initialize views
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etIdNumber = findViewById(R.id.etIdNumber)
        spinnerGender = findViewById(R.id.spinnerGender)
        spinnerProvince = findViewById(R.id.spinnerProvince)
        etAddress = findViewById(R.id.etAddress)
        etCity = findViewById(R.id.etCity)
        etPostcode = findViewById(R.id.etPostcode)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        btnUploadPassportPhoto = findViewById(R.id.btnUploadPassportPhoto)
        btnUploadProofOfAddress = findViewById(R.id.btnUploadProofOfAddress)
        btnUploadIdDocument = findViewById(R.id.btnUploadIdDocument)
        checkboxNda = findViewById(R.id.checkboxNda)
        btnSubmitPassportApplication = findViewById(R.id.btnSubmitPassportApplication)

        // Handle document uploads
        btnUploadPassportPhoto.setOnClickListener {
            openFileChooser()
        }
        btnUploadProofOfAddress.setOnClickListener {
            openFileChooser()
        }
        btnUploadIdDocument.setOnClickListener {
            openFileChooser()
        }

        // Populate Gender and Province spinners
        populateSpinners()

        // Handle form submission
        btnSubmitPassportApplication.setOnClickListener {
            if (validateForm()) {
                val intent = Intent(this, TestDateSelectionActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_CODE_UPLOAD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPLOAD && resultCode == Activity.RESULT_OK) {
            val selectedFile: Uri? = data?.data
            // Handle the selected file here (e.g., display file name or upload to the server)
        }
    }

    private fun populateSpinners() {
        // Gender options
        val genderOptions = arrayOf("Male", "Female", "Other")
        spinnerGender.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)

        // Province options
        val provinceOptions = arrayOf(
            "Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal", "Limpopo", "Mpumalanga",
            "Northern Cape", "North West", "Western Cape"
        )
        spinnerProvince.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, provinceOptions)
    }

    private fun validateForm(): Boolean {
        if (etName.text.isEmpty() || etSurname.text.isEmpty() || etIdNumber.text.isEmpty() ||
            etAddress.text.isEmpty() || etCity.text.isEmpty() || etPostcode.text.isEmpty() ||
            etEmail.text.isEmpty() || etPhoneNumber.text.isEmpty() || !checkboxNda.isChecked) {
            Toast.makeText(this, "Please fill out all fields and agree to the NDA", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
