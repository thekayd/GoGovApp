package com.kayodedaniel.gogovmobile

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.activities.ApplicationActivity
import io.github.jan.supabase.SupabaseClient
import kotlinx.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*

class DriversLicenseActivity : AppCompatActivity() {

    // Hardcoded Supabase URL and API Key for testing
    private val supabaseUrl =
        "https://bgckkkxjfnkwgjzlancs.supabase.co/rest/v1/drivers_license_applications" // Use the correct endpoint for your table
    private val supabaseKey =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnY2tra3hqZm5rd2dqemxhbmNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjcwOTQ4NDYsImV4cCI6MjA0MjY3MDg0Nn0.J63JbMamOasx251uRzmP8Z2WcrkgYBbzueFCb2B3eGo"

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
        initializeViews()

        // Date Picker for Date of Birth
        btnPickDob.setOnClickListener {
            pickDateOfBirth()
        }

        // File upload buttons
        findViewById<Button>(R.id.btnUploadIdDocument).setOnClickListener { pickFile(1) }
        findViewById<Button>(R.id.btnUploadPassportPhoto).setOnClickListener { pickFile(2) }
        findViewById<Button>(R.id.btnUploadProofOfAddress).setOnClickListener { pickFile(3) }
        findViewById<Button>(R.id.btnUploadEyeTestCertificate).setOnClickListener { pickFile(4) }

        // Submit Button Click Listener
        btnSubmit.setOnClickListener {
            submitForm()
        }
    }

    private fun initializeViews() {
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
    }

    // Function to pick Date of Birth
    private fun pickDateOfBirth() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDob = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                btnPickDob.text = selectedDob
            }, year, month, day)
        datePickerDialog.show()
    }

    private fun pickFile(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // Use a more specific MIME type if possible
        startActivityForResult(intent, requestCode)
    }

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
            }
        }
    }

    // Function to validate and submit the form
    private fun submitForm() {
        // Collect data from form inputs
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

        // Validate required fields
        if (name.isEmpty() || surname.isEmpty() || idNumber.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create JSON body
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
        }

        // Convert JSON to request body
        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Build OkHttp3 request
        val request = Request.Builder()
            .url(supabaseUrl)  // Use your actual endpoint
            .post(requestBody)
            .addHeader("apikey", supabaseKey)  // Your Supabase API Key
            .addHeader("Content-Type", "application/json")
            .build()

        // Execute request asynchronously
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DriversLicenseActivity, "Failed to submit form: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@DriversLicenseActivity, "Form submitted successfully!", Toast.LENGTH_SHORT).show()
                        // Navigate to Progress Activity
                        val intent = Intent(this@DriversLicenseActivity, ApplicationActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DriversLicenseActivity, "Failed to submit form: $responseBody", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
