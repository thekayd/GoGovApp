package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var verificationCode: String
    private lateinit var email: String // Passes this from SignUpActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)

        // Retrieves the verification code and email from SignUpActivity
        verificationCode = intent.getStringExtra("VERIFICATION_CODE") ?: ""
        email = intent.getStringExtra("EMAIL") ?: ""

        val codeInput = findViewById<EditText>(R.id.inputVerificationCode)
        val verifyButton = findViewById<Button>(R.id.buttonVerify)
        val resendButton = findViewById<TextView>(R.id.buttonResend)

        verifyButton.setOnClickListener {
            val enteredCode = codeInput.text.toString().trim()
            if (enteredCode == verificationCode) {
                Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show()
                navigateToSignIn()
            } else {
                Toast.makeText(this, "Invalid Verification Code", Toast.LENGTH_SHORT).show()
            }
        }

        resendButton.setOnClickListener {
            resendVerificationCode()
        }
    }

    // navigates to sign up
    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun resendVerificationCode() {
        // Generates a new verification code
        verificationCode = (100000..999999).random().toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                EmailSender.sendVerificationEmail(this@VerifyEmailActivity, email, verificationCode)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@VerifyEmailActivity, "Verification code resent to $email", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@VerifyEmailActivity, "Failed to resend verification code: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
