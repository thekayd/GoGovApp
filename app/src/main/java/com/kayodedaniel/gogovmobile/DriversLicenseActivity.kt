package com.kayodedaniel.gogovmobile

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.activities.ApplicationProgressActivity
import com.kayodedaniel.gogovmobile.activities.PaymentActivity
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

class DriversLicenseActivity : AppCompatActivity() {

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/drivers_license_applications"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

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
    private var learnersLicenseUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers_license)

        initializeViews()

        btnPickDob.setOnClickListener {
            pickDateOfBirth()
        }

        findViewById<Button>(R.id.btnUploadIdDocument).setOnClickListener { pickFile(1) }
        findViewById<Button>(R.id.btnUploadPassportPhoto).setOnClickListener { pickFile(2) }
        findViewById<Button>(R.id.btnUploadProofOfAddress).setOnClickListener { pickFile(3) }
        findViewById<Button>(R.id.btnUploadEyeTestCertificate).setOnClickListener { pickFile(4) }
        findViewById<Button>(R.id.btnUploadLearnerLicense).setOnClickListener { pickFile(5) }
        btnSubmit.setOnClickListener {
            submitForm()
        }

        // Loads the user's email from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("USER_EMAIL", "") ?: ""
        etEmail.setText(userEmail)
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etIdNumber = findViewById(R.id.etIdNumber)
        spGender = findViewById(R.id.spGender)
        spProvince = findViewById(R.id.spProvince)
        etAddress = findViewById(R.id.etAddress)

        // Adds these TextInputLayout fields to your XML
        etCity = findViewById(R.id.etCity)
        etPostcode = findViewById(R.id.etPostcode)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)

        spLicenseCategory = findViewById(R.id.spLicenseCategory)
        spTestCenter = findViewById(R.id.spTestCenter)
        btnPickDob = findViewById(R.id.btnPickDob)
        cbNDA = findViewById(R.id.checkboxNda1)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    private fun pickDateOfBirth() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Converts to ISO 8601 format
            selectedDob = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            btnPickDob.text = selectedDob // Display in button
        }, year, month, day)
        datePickerDialog.show()
        Log.d(TAG, "pickDateOfBirth: Date picker dialog shown")
    }

    // Function to generate application ID
    private fun generateApplicationId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random().nextInt(1000)
        return "VAC$timestamp$random"
    }

    // retrieves base uri for image
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

    // allows for picking of files
    private fun pickFile(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, requestCode)
    }

    // activity result intent for each document
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val fileUri = data.data
            when (requestCode) {
                1 -> {
                    idDocumentUri = fileUri
                    findViewById<Button>(R.id.btnUploadIdDocument).text = "ID Document Uploaded"
                }
                2 -> {
                    passportPhotoUri = fileUri
                    findViewById<Button>(R.id.btnUploadPassportPhoto).text = "Passport Photo Uploaded"
                }
                3 -> {
                    proofOfAddressUri = fileUri
                    findViewById<Button>(R.id.btnUploadProofOfAddress).text = "Proof of Address Uploaded"
                }
                4 -> {
                    eyeTestCertificateUri = fileUri
                    findViewById<Button>(R.id.btnUploadEyeTestCertificate).text = "Eye Test Certificate Uploaded"
                }
                5 -> {
                    learnersLicenseUri = fileUri
                    findViewById<Button>(R.id.btnUploadLearnerLicense).text = "Learner License Uploaded"
                }
            }
        } else {
            Log.w(TAG, "File selection cancelled or failed")
        }
        }

    // validation of fields
    private fun validateFields(): Boolean {
        var isValid = true
        val errorMessages = mutableListOf<String>()

        // Validate name
        if (!ValidationHelper.isValidName(etName.text.toString())) {
            isValid = false
            etName.error = "Please enter a valid name"
            errorMessages.add("Invalid name format")
        }

        // Validate surname
        if (!ValidationHelper.isValidName(etSurname.text.toString())) {
            isValid = false
            etSurname.error = "Please enter a valid surname"
            errorMessages.add("Invalid surname format")
        }

        // Validate ID number
        if (!ValidationHelper.isValidSAID(etIdNumber.text.toString())) {
            isValid = false
            etIdNumber.error = "Please enter a valid South African ID number"
            errorMessages.add("Invalid ID number")
        }

        // Validate phone number
        if (!ValidationHelper.isValidSAPhoneNumber(etPhoneNumber.text.toString())) {
            isValid = false
            etPhoneNumber.error = "Please enter a valid South African phone number"
            errorMessages.add("Invalid phone number")
        }

        // Validate email
        if (!ValidationHelper.isValidEmail(etEmail.text.toString())) {
            isValid = false
            etEmail.error = "Please enter a valid email address"
            errorMessages.add("Invalid email address")
        }

        // Validate address
        if (!ValidationHelper.isValidAddress(etAddress.text.toString())) {
            isValid = false
            etAddress.error = "Please enter a valid address"
            errorMessages.add("Invalid address format")
        }

        // Validate city
        if (!ValidationHelper.isValidCity(etCity.text.toString())) {
            isValid = false
            etCity.error = "Please enter a valid city name"
            errorMessages.add("Invalid city name")
        }

        // Validate postal code
        if (!ValidationHelper.isValidPostalCode(etPostcode.text.toString())) {
            isValid = false
            etPostcode.error = "Please enter a valid 4-digit postal code"
            errorMessages.add("Invalid postal code")
        }

        // Validate date of birth
        if (selectedDob == null) {
            isValid = false
            btnPickDob.error = "Please select your date of birth"
            errorMessages.add("Date of birth is required")
        }

        // Validate document uploads
        if (idDocumentUri == null || passportPhotoUri == null || proofOfAddressUri == null ||
            eyeTestCertificateUri == null || learnersLicenseUri == null) {
            isValid = false
            errorMessages.add("Please upload all required documents")
        }

        // Validate NDA checkbox
        if (!cbNDA.isChecked) {
            isValid = false
            errorMessages.add("Please accept the terms and conditions")
        }

        // Displays all validation errors if any
        if (!isValid) {
            val errorMessage = errorMessages.joinToString("\n")
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }

        return isValid
    }

    // submits forms to database
    private fun submitForm() {

        if (!validateFields()) {
            return
        }

        val applicationId = generateApplicationId()
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val idNumber = etIdNumber.text.toString()
        val gender = spGender.selectedItem.toString()
        val province = spProvince.selectedItem.toString()
        val address = etAddress.text.toString()
        val city = etCity.text.toString()
        val postcode = etPostcode.text.toString()
        val email = etEmail.text.toString()
        val phoneNumber = etPhoneNumber.text.toString()
        val licenseCategory = spLicenseCategory.selectedItem.toString()
        val testCenter = spTestCenter.selectedItem.toString()
        val dob = selectedDob ?: ""

        val passportPhotoBase64 = getBase64FromUri(passportPhotoUri)
        val proofOfAddressBase64 = getBase64FromUri(proofOfAddressUri)
        val idDocumentBase64 = getBase64FromUri(idDocumentUri)
        val eyeTestCertificateBase64 = getBase64FromUri(eyeTestCertificateUri)
        val learnersLicenseBase64 = getBase64FromUri(learnersLicenseUri)

        // validations
        if (passportPhotoBase64 == null || proofOfAddressBase64 == null || idDocumentBase64 == null|| eyeTestCertificateBase64 == null|| learnersLicenseBase64 == null) {
            Toast.makeText(this, "Please upload all required documents", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isEmpty() || surname.isEmpty() || idNumber.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // uses json object and inputs fields in to database fields
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
            put("license_category", licenseCategory)
            put("test_center", testCenter)
            put("date_of_birth", dob)
            put("status", "Sent For Validation")
            put("passport_photo", passportPhotoBase64)
            put("proof_of_address", proofOfAddressBase64)
            put("id_document", idDocumentBase64)
            put("eye_test_certificate", eyeTestCertificateBase64)
            put("learners_license", learnersLicenseBase64)

        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // requests into supabase
        val request = Request.Builder()
            .url(supabaseUrl)
            .post(requestBody)
            .addHeader("apikey", supabaseKey)
            .addHeader("Content-Type", "application/json")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                // using data for payment
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DriversLicenseActivity, "Form submitted successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@DriversLicenseActivity, PaymentActivity::class.java).apply {
                            putExtra("application_id", applicationId)
                            putExtra("name", name)
                            putExtra("surname", surname)
                            putExtra("email", email)
                            putExtra("application_type", "drivers_license_applications")
                        }
                        startActivity(intent)
                    } else {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        Toast.makeText(this@DriversLicenseActivity, "Failed to submit form: $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DriversLicenseActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}