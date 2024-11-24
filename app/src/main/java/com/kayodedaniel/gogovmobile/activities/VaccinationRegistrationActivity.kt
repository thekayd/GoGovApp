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
import androidx.annotation.RequiresApi
import com.kayodedaniel.gogovmobile.ValidationHelper

class VaccinationRegistrationActivity : AppCompatActivity() {

    private val TAG = "VaccinationRegistration"

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/vaccination_applications"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etIdNumber: EditText
    private lateinit var spGender: Spinner
    private lateinit var etAddress: EditText
    private lateinit var etCity: EditText
    private lateinit var etPostcode: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var spVaccineType: Spinner
    private lateinit var spVaccinationCenter: Spinner
    private lateinit var btnPickDob: Button
    private lateinit var cbConsent: CheckBox
    private lateinit var btnSubmit: Button
    private lateinit var btnUploadID: Button
    private lateinit var btnUploadProofOfResidence: Button
    private lateinit var btnUploadMedicalRecords: Button

    private var selectedDob: String? = null
    private var idDocumentUri: Uri? = null
    private var proofOfResidenceUri: Uri? = null
    private var medicalRecordsStatementUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vaccination_registration)

        Log.d(TAG, "onCreate: Initializing VaccinationRegistrationActivity")

        initializeViews()

        btnPickDob.setOnClickListener {
            pickDateOfBirth()
        }

        btnUploadID.setOnClickListener {
            pickFile(1)
        }

        btnUploadProofOfResidence.setOnClickListener {
            pickFile(2)
        }

        btnUploadMedicalRecords.setOnClickListener {
            pickFile(3)
        }

        btnSubmit.setOnClickListener {
            submitForm()
        }

        // Loads the user's email from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("USER_EMAIL", "") ?: ""
        etEmail.setText(userEmail)
        Log.d(TAG, "onCreate: User email loaded: $userEmail")
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etNameInput)
        etSurname = findViewById(R.id.etSurnameInput)
        etIdNumber = findViewById(R.id.etIdNumberInput)
        spGender = findViewById(R.id.spGender)
        etAddress = findViewById(R.id.etAddressInput)
        etCity = findViewById(R.id.etCityInput)
        etPostcode = findViewById(R.id.etPostcodeInput)
        etEmail = findViewById(R.id.etEmailInput)
        etPhoneNumber = findViewById(R.id.etPhoneNumberInput)
        spVaccineType = findViewById(R.id.spVaccineType)
        spVaccinationCenter = findViewById(R.id.spVaccinationCenter)
        btnPickDob = findViewById(R.id.btnPickDob)
        btnUploadID = findViewById(R.id.btnUploadID)
        btnUploadProofOfResidence = findViewById(R.id.btnUploadProofOfResidence)
        btnUploadMedicalRecords = findViewById(R.id.btnUploadMedicalRecords)
        cbConsent = findViewById(R.id.cbConsent)
        btnSubmit = findViewById(R.id.btnSubmit)


        Log.d(TAG, "initializeViews: Views initialized")
    }

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
                1 -> {
                    idDocumentUri = fileUri
                    btnUploadID.text = "ID Uploaded"
                }
                2 -> {
                    proofOfResidenceUri = fileUri
                    btnUploadProofOfResidence.text = "Proof Uploaded"
                }
                3 -> {
                    medicalRecordsStatementUri = fileUri
                    btnUploadMedicalRecords.text = "Records Uploaded"
                }
            }
        }
    }

    private fun getBase64FromUri(uri: Uri?): String? {
        if (uri == null) return null

        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
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
        } else {
            if (!ValidationHelper.isValidAgeForVaccination(selectedDob!!)) {
                isValid = false
                errorMessages.add("You must be at least 12 years old to register for vaccination")
            }
        }

        // Validate vaccine type selection
        if (spVaccineType.selectedItemPosition == 0) {
            isValid = false
            errorMessages.add("Please select a vaccine type")
        }

        // Validate vaccination center selection
        if (spVaccinationCenter.selectedItemPosition == 0) {
            isValid = false
            errorMessages.add("Please select a vaccination center")
        }

        // Validate document uploads
        if (idDocumentUri == null) {
            isValid = false
            errorMessages.add("Please upload your ID document")
        }
        if (proofOfResidenceUri == null) {
            isValid = false
            errorMessages.add("Please upload proof of residence")
        }
        if (medicalRecordsStatementUri == null) {
            isValid = false
            errorMessages.add("Please upload medical records")
        }

        // Validate consent
        if (!cbConsent.isChecked) {
            isValid = false
            errorMessages.add("Please provide consent to continue")
        }

        // Displays all validation errors if any
        if (!isValid) {
            val errorMessage = errorMessages.joinToString("\n")
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            Log.w(TAG, "Form validation failed: $errorMessage")
        }

        return isValid
    }

    // generates app id for user payment
    private fun generateApplicationId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random().nextInt(1000)
        return "VAC$timestamp$random"
    }

    // submits form for user
    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitForm() {
        Log.d(TAG, "submitForm: Attempting to submit form")

        if (!validateFields()) {
            return
        }

        val applicationId = generateApplicationId()
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val idNumber = etIdNumber.text.toString()
        val gender = spGender.selectedItem.toString()
        val address = etAddress.text.toString()
        val city = etCity.text.toString()
        val postcode = etPostcode.text.toString()
        val email = etEmail.text.toString()
        val phoneNumber = etPhoneNumber.text.toString()
        val vaccineType = spVaccineType.selectedItem.toString()
        val vaccinationCenter = spVaccinationCenter.selectedItem.toString()
        val dob = selectedDob ?: ""
        val idDocumentBase64 = getBase64FromUri(idDocumentUri)
        val proofOfResidenceBase64 = getBase64FromUri(proofOfResidenceUri)
        val medicalRecordsBase64 = getBase64FromUri(medicalRecordsStatementUri)

        if (name.isEmpty() || surname.isEmpty() || idNumber.isEmpty() || dob.isEmpty() || !cbConsent.isChecked) {
            Log.w(TAG, "submitForm: Form validation failed")
            Toast.makeText(this, "Please fill in all required fields and give consent", Toast.LENGTH_SHORT).show()
            return
        }
        if (idDocumentBase64 == null || proofOfResidenceBase64 == null || medicalRecordsBase64 == null) {
            Toast.makeText(this, "Error processing documents. Please try uploading them again.", Toast.LENGTH_SHORT).show()
            return
        }

        // puts the application details into supabase for saving
        val json = JSONObject().apply {
           // put("application_id", applicationId)
            put("name", name)
            put("surname", surname)
            put("id_number", idNumber)
            put("gender", gender)
            put("address", address)
            put("city", city)
            put("postcode", postcode)
            put("email", email)
            put("phone_number", phoneNumber)
            put("vaccine_type", vaccineType)
            put("vaccination_center", vaccinationCenter)
            put("date_of_birth", dob)
            put("status", "Registered")
            put("id_document", idDocumentBase64)
            put("proof_of_residence", proofOfResidenceBase64)
            put("medical_records", medicalRecordsBase64)
        }

        Log.d(TAG, "submitForm: Prepared JSON data: $json")

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(supabaseUrl)
            .post(requestBody)
            .addHeader("apikey", supabaseKey)
            .addHeader("Content-Type", "application/json")
            .build()

        Log.d(TAG, "submitForm: Sending request to Supabase")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                // request on suoabase url and saving information and using intent where needed
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "submitForm: Registration successful")
                        Toast.makeText(this@VaccinationRegistrationActivity, "Vaccination registration submitted successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@VaccinationRegistrationActivity, PaymentActivity::class.java).apply {
                            putExtra("application_id", applicationId)
                            putExtra("name", name)
                            putExtra("surname", surname)
                            putExtra("email", email)
                            putExtra("application_type", "vaccination_applications")
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        Log.e(TAG, "submitForm: Registration failed. Error: $errorBody")
                        Toast.makeText(this@VaccinationRegistrationActivity, "Failed to submit registration: $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "submitForm: Exception occurred", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@VaccinationRegistrationActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}