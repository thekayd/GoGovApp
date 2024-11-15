package com.kayodedaniel.gogovmobile.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
    private lateinit var btnSubmit: Button

    private var selectedDob: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bursary_application)

        initializeViews()

        btnPickDob.setOnClickListener {
            pickDateOfBirth()
        }

        btnSubmit.setOnClickListener {
            submitForm()
        }

        // Load the user's email from SharedPreferences
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
        btnSubmit = findViewById(R.id.btnSubmit)
    }

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

    private fun submitForm() {
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

        if (name.isEmpty() || surname.isEmpty() || idNumber.isEmpty() || dob.isEmpty() || email.isEmpty()) {
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
                        val intent = Intent(this@BursaryApplicationActivity, PaymentActivity::class.java)
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