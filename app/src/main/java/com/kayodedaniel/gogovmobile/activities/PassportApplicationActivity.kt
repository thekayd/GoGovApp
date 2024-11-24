package com.kayodedaniel.gogovmobile.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import android.util.Base64
import android.util.Patterns
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    private lateinit var spPassportType: Spinner
    private lateinit var spProcessingCenter: Spinner

    private var selectedDob: String? = null
    private var passportPhotoUri: Uri? = null
    private var proofOfAddressUri: Uri? = null
    private var idDocumentUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.O)
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
        spPassportType = findViewById(R.id.spPassportType)
        spProcessingCenter = findViewById(R.id.spProcessingCenter)

        // Set up spinners
        Log.d(TAG, "Setting up spinners")
        ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spGender.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.province_options,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spProvince.adapter = adapter
        }
        Log.d(TAG, "Views initialized successfully")
        ArrayAdapter.createFromResource(
            this,
            R.array.passport_types,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spPassportType.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.processing_centers,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spProcessingCenter.adapter = adapter
        }
    }
    private fun validateSouthAfricanID(idNumber: String): Boolean {
        // South African ID number must be 13 digits
        if (!idNumber.matches(Regex("^\\d{13}$"))) {
            tilIdNumber.error = "ID number must be 13 digits"
            return false
        }

        try {
            // Extract date of birth from ID number (YYMMDD)
            val year = idNumber.substring(0, 2).toInt()
            val month = idNumber.substring(2, 4).toInt()
            val day = idNumber.substring(4, 6).toInt()

            // Validate month
            if (month < 1 || month > 12) {
                tilIdNumber.error = "Invalid month in ID number"
                return false
            }

            // Validate day
            if (day < 1 || day > 31) {
                tilIdNumber.error = "Invalid day in ID number"
                return false
            }

            // Validate citizenship digit (positions 10-10)
            val citizenship = idNumber[10].toString().toInt()
            if (citizenship != 0 && citizenship != 1) {
                tilIdNumber.error = "Invalid citizenship digit"
                return false
            }

            // Luhn algorithm validation
            var sum = 0
            var alternate = false
            for (i in idNumber.length - 1 downTo 0) {
                var n = idNumber[i].toString().toInt()
                if (alternate) {
                    n *= 2
                    if (n > 9) {
                        n = n % 10 + 1
                    }
                }
                sum += n
                alternate = !alternate
            }

            if (sum % 10 != 0) {
                tilIdNumber.error = "Invalid ID number checksum"
                return false
            }

            tilIdNumber.error = null
            return true
        } catch (e: Exception) {
            tilIdNumber.error = "Invalid ID number format"
            return false
        }
    }

    private fun validateName(name: String): Boolean {
        if (name.length < 2) {
            tilName.error = "Name must be at least 2 characters"
            return false
        }
        if (!name.matches(Regex("^[a-zA-Z\\s'-]+$"))) {
            tilName.error = "Name contains invalid characters"
            return false
        }
        tilName.error = null
        return true
    }

    private fun validateSurname(surname: String): Boolean {
        if (surname.length < 2) {
            tilSurname.error = "Surname must be at least 2 characters"
            return false
        }
        if (!surname.matches(Regex("^[a-zA-Z\\s'-]+$"))) {
            tilSurname.error = "Surname contains invalid characters"
            return false
        }
        tilSurname.error = null
        return true
    }

    private fun validateAddress(address: String): Boolean {
        if (address.length < 5) {
            tilAddress.error = "Address must be at least 5 characters"
            return false
        }
        if (!address.matches(Regex("^[a-zA-Z0-9\\s,'-]+$"))) {
            tilAddress.error = "Address contains invalid characters"
            return false
        }
        tilAddress.error = null
        return true
    }

    private fun validateCity(city: String): Boolean {
        if (city.length < 2) {
            tilCity.error = "City must be at least 2 characters"
            return false
        }
        if (!city.matches(Regex("^[a-zA-Z\\s'-]+$"))) {
            tilCity.error = "City contains invalid characters"
            return false
        }
        tilCity.error = null
        return true
    }

    private fun validatePostcode(postcode: String): Boolean {
        // South African postal codes are 4 digits
        if (!postcode.matches(Regex("^\\d{4}$"))) {
            tilPostcode.error = "Postcode must be 4 digits"
            return false
        }
        tilPostcode.error = null
        return true
    }

    private fun validateEmail(email: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Invalid email address"
            return false
        }
        tilEmail.error = null
        return true
    }

    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        // South African phone numbers: optional +27, then 10 digits
        if (!phoneNumber.matches(Regex("^(\\+27|0)\\d{9}$"))) {
            tilPhoneNumber.error = "Invalid phone number format (e.g., 0123456789 or +27123456789)"
            return false
        }
        tilPhoneNumber.error = null
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateDateOfBirth(dob: String): Boolean {
        if (dob.isEmpty()) {
            Toast.makeText(this, "Please select a date of birth", Toast.LENGTH_SHORT).show()
            return false
        }

        try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val birthDate = LocalDate.parse(dob, formatter)
            val now = LocalDate.now()

            // Check if person is at least 16 years old
            if (birthDate.plusYears(16).isAfter(now)) {
                Toast.makeText(this, "Applicant must be at least 16 years old", Toast.LENGTH_SHORT).show()
                return false
            }

            // Check if person is not more than 100 years old
            if (birthDate.plusYears(100).isBefore(now)) {
                Toast.makeText(this, "Invalid date of birth", Toast.LENGTH_SHORT).show()
                return false
            }

            return true
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
            return false
        }
    }
    private fun getBase64FromUri(uri: Uri?): String? {
        if (uri == null) return null

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            return if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else null
        } catch (e: Exception) {
            Log.e(TAG, "Error converting file to Base64", e)
            return null
        }
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

    // Function to generate application ID
    private fun generateApplicationId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random().nextInt(1000)
        return "VAC$timestamp$random"
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitForm() {
        Log.d(TAG, "Starting form submission")

        // Get all form values
        val applicationId = generateApplicationId()
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
        val passportPhotoBase64 = getBase64FromUri(passportPhotoUri)
        val proofOfAddressBase64 = getBase64FromUri(proofOfAddressUri)
        val idDocumentBase64 = getBase64FromUri(idDocumentUri)

        if (passportPhotoBase64 == null || proofOfAddressBase64 == null || idDocumentBase64 == null) {
            Toast.makeText(this, "Please upload all required documents", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate all fields
        if (!validateName(name) ||
            !validateSurname(surname) ||
            !validateSouthAfricanID(idNumber) ||
            !validateAddress(address) ||
            !validateCity(city) ||
            !validatePostcode(postcode) ||
            !validateEmail(email) ||
            !validatePhoneNumber(phoneNumber) ||
            !validateDateOfBirth(dob)
        ) {
            Toast.makeText(this, "Please correct the errors in the form", Toast.LENGTH_LONG).show()
            return
        }

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
            put("passport_photo", passportPhotoBase64)
            put("proof_of_address", proofOfAddressBase64)
            put("id_document", idDocumentBase64)
            put("passport_type", spPassportType.selectedItem.toString())
            put("processing_center", spProcessingCenter.selectedItem.toString())
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
                        val intent = Intent(this@PassportApplicationActivity, PaymentActivity::class.java).apply {
                            putExtra("application_id", applicationId)
                            putExtra("name", name)
                            putExtra("surname", surname)
                            putExtra("email", email)
                            putExtra("application_type", "passport_applications")
                        }
                        startActivity(intent)
                        finish()
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