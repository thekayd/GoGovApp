package com.kayodedaniel.gogovmobile.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import android.util.Patterns
import java.text.SimpleDateFormat

class BursaryApplicationActivity : AppCompatActivity() {

    private val supabaseUrl = "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/bursary_applications"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etIdNumber: EditText
    private lateinit var spGender: Spinner
    private lateinit var btnPickDob: Button
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etAddress: EditText
    private lateinit var etCity: EditText
    private lateinit var spProvince: Spinner
    private lateinit var etPostcode: EditText
    private lateinit var etInstitutionName: EditText
    private lateinit var etCourseOfStudy: EditText
    private lateinit var etStudyYear: EditText
    private lateinit var etTotalCourseDuration: EditText
    private lateinit var etAnnualTuitionFee: EditText
    private lateinit var etOtherFundingSources: EditText
    private lateinit var etAcademicAchievements: EditText
    private lateinit var etFinancialNeedStatement: EditText
    private lateinit var btnUploadID: Button
    private lateinit var btnUploadTranscript: Button
    private lateinit var btnUploadFinancialStatement: Button
    private lateinit var btnUploadAcceptanceLetter: Button
    private lateinit var btnSubmit: Button

    private var selectedDob: String? = null
    private var idDocumentUri: Uri? = null
    private var transcriptUri: Uri? = null
    private var financialStatementUri: Uri? = null
    private var acceptanceLetterUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bursary_application)

        initializeViews()

        btnPickDob.setOnClickListener {
            pickDateOfBirth()
        }

        btnUploadID.setOnClickListener {
            pickFile(1)
        }

        btnUploadTranscript.setOnClickListener {
            pickFile(2)
        }

        btnUploadFinancialStatement.setOnClickListener {
            pickFile(3)
        }

        btnUploadAcceptanceLetter.setOnClickListener {
            pickFile(4)
        }

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
        btnPickDob = findViewById(R.id.btnPickDob)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etAddress = findViewById(R.id.etAddress)
        etCity = findViewById(R.id.etCity)
        spProvince = findViewById(R.id.spProvince)
        etPostcode = findViewById(R.id.etPostcode)
        etInstitutionName = findViewById(R.id.etInstitutionName)
        etCourseOfStudy = findViewById(R.id.etCourseOfStudy)
        etStudyYear = findViewById(R.id.etStudyYear)
        etTotalCourseDuration = findViewById(R.id.etTotalCourseDuration)
        etAnnualTuitionFee = findViewById(R.id.etAnnualTuitionFee)
        etOtherFundingSources = findViewById(R.id.etOtherFundingSources)
        etAcademicAchievements = findViewById(R.id.etAcademicAchievements)
        etFinancialNeedStatement = findViewById(R.id.etFinancialNeedStatement)
        btnUploadID = findViewById(R.id.btnUploadID)
        btnUploadTranscript = findViewById(R.id.btnUploadTranscript)
        btnUploadFinancialStatement = findViewById(R.id.btnUploadFinancialStatement)
        btnUploadAcceptanceLetter = findViewById(R.id.btnUploadAcceptanceLetter)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    private fun setupValidations() {
        // Names Validation
        etName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateName()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Surname Validation
        etSurname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateSurname()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ID Number Validation
        etIdNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateSouthAfricanIDNumber()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Phone Number Validation
        etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateSouthAfricanPhoneNumber()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Email Validation
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateEmail()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Postcode Validation
        etPostcode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validatePostcode()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Validates the name
    private fun validateName(): Boolean {
        val name = etName.text.toString().trim()
        return if (name.length < 2) {
            etName.error = "Name must be at least 2 characters long"
            false
        } else if (!name.matches(Regex("^[a-zA-Z\\s]*$"))) {
            etName.error = "Name can only contain letters"
            false
        } else {
            etName.error = null
            true
        }
    }

    // method to validate surname
    private fun validateSurname(): Boolean {
        val surname = etSurname.text.toString().trim()
        return if (surname.length < 2) {
            etSurname.error = "Surname must be at least 2 characters long"
            false
        } else if (!surname.matches(Regex("^[a-zA-Z\\s]*$"))) {
            etSurname.error = "Surname can only contain letters"
            false
        } else {
            etSurname.error = null
            true
        }
    }

    // method to validate SA number
    private fun validateSouthAfricanIDNumber(): Boolean {
        val idNumber = etIdNumber.text.toString().trim()
        val idRegex = """^\d{13}$""".toRegex()

        return if (!idRegex.matches(idNumber)) {
            etIdNumber.error = "Invalid South African ID Number"
            false
        } else {
            // Additional validation: Check birth date and checksum
            try {
                val year = idNumber.substring(0, 2).toInt()
                val month = idNumber.substring(2, 4).toInt()
                val day = idNumber.substring(4, 6).toInt()
                val currentYear = Calendar.getInstance().get(Calendar.YEAR) % 100

                val fullYear = if (year <= currentYear) 2000 + year else 1900 + year

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateFormat.isLenient = false
                dateFormat.parse("$fullYear-$month-$day")

                etIdNumber.error = null
                true
            } catch (e: Exception) {
                etIdNumber.error = "Invalid South African ID Number"
                false
            }
        }
    }

    private fun validateSouthAfricanPhoneNumber(): Boolean {
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val phoneRegex = """^((\+27|0)[1-9][0-9]{8})$""".toRegex()

        return if (!phoneRegex.matches(phoneNumber)) {
            etPhoneNumber.error = "Invalid South African phone number"
            false
        } else {
            etPhoneNumber.error = null
            true
        }
    }

    private fun validateEmail(): Boolean {
        val email = etEmail.text.toString().trim()
        return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email address"
            false
        } else {
            etEmail.error = null
            true
        }
    }

    private fun validatePostcode(): Boolean {
        val postcode = etPostcode.text.toString().trim()
        val postcodeRegex = """^\d{4}$""".toRegex()

        return if (!postcodeRegex.matches(postcode)) {
            etPostcode.error = "Invalid postcode (must be 4 digits)"
            false
        } else {
            etPostcode.error = null
            true
        }
    }

     // Validation of fields
    private fun validateAllFields(): Boolean {
        return validateName() &&
                validateSurname() &&
                validateSouthAfricanIDNumber() &&
                validateSouthAfricanPhoneNumber() &&
                validateEmail() &&
                validatePostcode() &&
                selectedDob != null &&
                idDocumentUri != null &&
                transcriptUri != null &&
                financialStatementUri != null &&
                acceptanceLetterUri != null
    }

    // Displays a DatePicker dialog
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

    //Launches an intent to pick a file
    private fun pickFile(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) // Processes the selected file based on the request code
        if (resultCode == RESULT_OK && data != null) {
            val fileUri = data.data
            when (requestCode) {
                1 -> {
                    idDocumentUri = fileUri
                    btnUploadID.text = "ID Uploaded"
                }
                2 -> {
                    transcriptUri = fileUri
                    btnUploadTranscript.text = "Transcript Uploaded"
                }
                3 -> {
                    financialStatementUri = fileUri
                    btnUploadFinancialStatement.text = "Financial Statement Uploaded"
                }
                4 -> {
                    acceptanceLetterUri = fileUri
                    btnUploadAcceptanceLetter.text = "Acceptance Letter Uploaded"
                }
            }
        }
    }

    // Function to generate application ID
    private fun generateApplicationId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random().nextInt(1000)
        return "VAC$timestamp$random"
    }


    // changes the image photo into a uri base64
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

    // Submit data to Supabase using the below method
    private fun submitForm() {
        // Validates all fields before submission
        if (!validateAllFields()) {
            Toast.makeText(this, "Please correct all errors before submitting", Toast.LENGTH_SHORT).show()
            return
        }

        val applicationId = generateApplicationId()
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val idNumber = etIdNumber.text.toString()
        val gender = spGender.selectedItem.toString()
        val dob = selectedDob ?: ""
        val email = etEmail.text.toString()
        val phoneNumber = etPhoneNumber.text.toString()
        val address = etAddress.text.toString()
        val city = etCity.text.toString()
        val province = spProvince.selectedItem.toString()
        val postcode = etPostcode.text.toString()
        val institutionName = etInstitutionName.text.toString()
        val courseOfStudy = etCourseOfStudy.text.toString()
        val studyYear = etStudyYear.text.toString()
        val totalCourseDuration = etTotalCourseDuration.text.toString()
        val annualTuitionFee = etAnnualTuitionFee.text.toString()
        val otherFundingSources = etOtherFundingSources.text.toString()
        val academicAchievements = etAcademicAchievements.text.toString()
        val financialNeedStatement = etFinancialNeedStatement.text.toString()
        val idDocumentBase64 = getBase64FromUri(idDocumentUri)
        val transcriptBase64 = getBase64FromUri(transcriptUri)
        val financialStatementBase64 = getBase64FromUri(financialStatementUri)
        val acceptanceLetterBase64 = getBase64FromUri(acceptanceLetterUri)

        if (name.isEmpty() || surname.isEmpty() || idNumber.isEmpty() || dob.isEmpty() || email.isEmpty() || idDocumentBase64 == null || transcriptBase64 == null || financialStatementBase64 == null || acceptanceLetterBase64 == null) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("name", name)
            put("surname", surname)
            put("id_number", idNumber)
            put("gender", gender)
            put("date_of_birth", dob)
            put("email", email)
            put("phone_number", phoneNumber)
            put("address", address)
            put("city", city)
            put("province", province)
            put("postcode", postcode)
            put("institution_name", institutionName)
            put("course_of_study", courseOfStudy)
            put("study_year", studyYear)
            put("total_course_duration", totalCourseDuration)
            put("annual_tuition_fee", annualTuitionFee)
            put("other_funding_sources", otherFundingSources)
            put("academic_achievements", academicAchievements)
            put("financial_need_statement", financialNeedStatement)
            put("id_document", idDocumentBase64)
            put("transcript", transcriptBase64)
            put("financial_statement", financialStatementBase64)
            put("acceptance_letter", acceptanceLetterBase64)
            put("status", "Submitted")
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

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

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@BursaryApplicationActivity, "Bursary application submitted successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@BursaryApplicationActivity, PaymentActivity::class.java).apply {
                            putExtra("application_id", applicationId)
                            putExtra("name", name)
                            putExtra("surname", surname)
                            putExtra("email", email)
                            putExtra("application_type", "bursary_applications")
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        Toast.makeText(this@BursaryApplicationActivity, "Failed to submit form: $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BursaryApplicationActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}