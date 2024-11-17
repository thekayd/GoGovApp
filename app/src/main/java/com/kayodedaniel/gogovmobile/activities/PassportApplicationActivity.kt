package com.kayodedaniel.gogovmobile.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*

class PassportApplicationActivity : AppCompatActivity() {
    private val TAG = "PassportApplication"  // Tag for logging

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/passport_applications"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    private lateinit var tilName: TextInputLayout
    private lateinit var tilSurname: TextInputLayout
    private lateinit var tilIdNumber: TextInputLayout
    private lateinit var spGender: Spinner
    private lateinit var spProvince: Spinner
    private lateinit var tilAddress: TextInputLayout
    private lateinit var tilCity: TextInputLayout
    private lateinit var tilPostcode: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPhoneNumber: TextInputLayout
    private lateinit var btnPickDob: Button
    private lateinit var cbNDA: CheckBox
    private lateinit var btnSubmit: Button

    private var selectedDob: String? = null
    private var passportPhotoUri: Uri? = null
    private var proofOfAddressUri: Uri? = null
    private var idDocumentUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity started")
        setContentView(R.layout.activity_passport_application)

        initializeViews()

        btnPickDob.setOnClickListener {
            Log.d(TAG, "Date picker button clicked")
            pickDateOfBirth()
        }

        findViewById<Button>(R.id.btnUploadPassportPhoto).setOnClickListener {
            Log.d(TAG, "Passport photo upload button clicked")
            pickFile(1)
        }
        findViewById<Button>(R.id.btnUploadProofOfAddress).setOnClickListener {
            Log.d(TAG, "Proof of address upload button clicked")
            pickFile(2)
        }
        findViewById<Button>(R.id.btnUploadIdDocument).setOnClickListener {
            Log.d(TAG, "ID document upload button clicked")
            pickFile(3)
        }

        btnSubmit.setOnClickListener {
            Log.d(TAG, "Submit button clicked")
            submitForm()
        }

        // Load the user's email from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("USER_EMAIL", "") ?: ""
        Log.d(TAG, "Loaded user email from SharedPreferences: $userEmail")
        tilEmail.editText?.setText(userEmail)
    }

    private fun initializeViews() {
        Log.d(TAG, "Initializing views")
        tilName = findViewById(R.id.tilName)
        tilSurname = findViewById(R.id.tilSurname)
        tilIdNumber = findViewById(R.id.tilIdNumber)
        spGender = findViewById(R.id.spGender)
        spProvince = findViewById(R.id.spProvince)
        tilAddress = findViewById(R.id.tilAddress)
        tilCity = findViewById(R.id.tilCity)
        tilPostcode = findViewById(R.id.tilPostcode)
        tilEmail = findViewById(R.id.tilEmail)
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber)
        btnPickDob = findViewById(R.id.btnPickDob)
        cbNDA = findViewById(R.id.cbNDA)
        btnSubmit = findViewById(R.id.btnSubmit)

        // Set up spinners
        Log.d(TAG, "Setting up spinners")
        ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spGender.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.province_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spProvince.adapter = adapter
        }
        Log.d(TAG, "Views initialized successfully")
    }

    private fun pickDateOfBirth() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Convert to ISO 8601 format
            selectedDob = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            btnPickDob.text = selectedDob // Display in button
        }, year, month, day)
        datePickerDialog.show()
        Log.d(TAG, "pickDateOfBirth: Date picker dialog shown")
    }

    private fun pickFile(requestCode: Int) {
        Log.d(TAG, "Initiating file picker with request code: $requestCode")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult - requestCode: $requestCode, resultCode: $resultCode")

        if (resultCode == RESULT_OK && data != null) {
            val fileUri = data.data
            Log.d(TAG, "File selected: $fileUri")

            when (requestCode) {
                1 -> {
                    passportPhotoUri = fileUri
                    findViewById<Button>(R.id.btnUploadPassportPhoto).text = "Passport Photo Uploaded"
                    Log.d(TAG, "Passport photo uploaded")
                }
                2 -> {
                    proofOfAddressUri = fileUri
                    findViewById<Button>(R.id.btnUploadProofOfAddress).text = "Proof of Address Uploaded"
                    Log.d(TAG, "Proof of address uploaded")
                }
                3 -> {
                    idDocumentUri = fileUri
                    findViewById<Button>(R.id.btnUploadIdDocument).text = "ID Document Uploaded"
                    Log.d(TAG, "ID document uploaded")
                }
            }
        } else {
            Log.w(TAG, "File selection cancelled or failed")
        }
    }

    private fun submitForm() {
        Log.d(TAG, "Starting form submission")

        // Get all form values
        val name = tilName.editText?.text.toString()
        val surname = tilSurname.editText?.text.toString()
        val idNumber = tilIdNumber.editText?.text.toString()
        val gender = spGender.selectedItem.toString()
        val province = spProvince.selectedItem.toString()
        val address = tilAddress.editText?.text.toString()
        val city = tilCity.editText?.text.toString()
        val postcode = tilPostcode.editText?.text.toString()
        val email = tilEmail.editText?.text.toString()
        val phoneNumber = tilPhoneNumber.editText?.text.toString()
        val dob = selectedDob ?: ""

        // Log all form values
        Log.d(TAG, """
            Form Values:
            Name: $name
            Surname: $surname
            ID Number: $idNumber
            Gender: $gender
            Province: $province
            Address: $address
            City: $city
            Postcode: $postcode
            Email: $email
            Phone: $phoneNumber
            DOB: $dob
            NDA Checked: ${cbNDA.isChecked}
        """.trimIndent())

        // Validate form
        if (name.isEmpty() || surname.isEmpty() || idNumber.isEmpty() || dob.isEmpty() ||
            address.isEmpty() || city.isEmpty() || postcode.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
            Log.w(TAG, "Form validation failed - empty fields detected")
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!cbNDA.isChecked) {
            Log.w(TAG, "Form validation failed - NDA not checked")
            Toast.makeText(this, "Please agree to the NDA", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Form validation passed, preparing JSON")

        val json = JSONObject().apply {
            put("name", name)
            put("surname", surname)
            put("id_number", idNumber)
            put("gender", gender)
            put("province", province)
            put("address", address)
            put("city", city)
            put("postcode", postcode)
            put("email", email)
            put("phone_number", phoneNumber)
            put("date_of_birth", dob)
            put("status", "Sent For Validation")
        }

        Log.d(TAG, "JSON prepared: ${json.toString()}")

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(supabaseUrl)
            .post(requestBody)
            .addHeader("apikey", supabaseKey)
            .addHeader("Content-Type", "application/json")
            .build()

        Log.d(TAG, "Starting network request to Supabase")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Executing network request")
                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Network request successful: ${response.code}")
                        Toast.makeText(this@PassportApplicationActivity, "Passport application submitted successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@PassportApplicationActivity, PaymentActivity::class.java)
                        startActivity(intent)
                    } else {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        Log.e(TAG, "Network request failed: ${response.code}, Error: $errorBody")
                        Toast.makeText(this@PassportApplicationActivity, "Failed to submit form: $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during network request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PassportApplicationActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}