package com.kayodedaniel.gogovmobile.activities
import android.util.Log
import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"
    private const val EMAIL = "mini7rush@gmail.com" // Replace with your email
    private const val PASSWORD = "cntqypdiusmbomao" // Replace with your email password

    fun sendVerificationEmail(toEmail: String, verificationCode: String) {
        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", SMTP_HOST)
                put("mail.smtp.port", SMTP_PORT)
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(EMAIL, PASSWORD)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(EMAIL, "GoGov Mobile"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                subject = "Verify Your Email Address"
                setText(
                    """
                    Hello,
                    
                    Thank you for signing up on GoGov Mobile. Please use the following verification code to complete your signup:
                    
                    Verification Code: $verificationCode
                    
                    Best regards,
                    GoGov Mobile Team
                    """.trimIndent()
                )
            }

            Transport.send(message)
            Log.d("EmailSender", "Verification email sent successfully to $toEmail")
        } catch (e: Exception) {
            Log.e("EmailSender", "Failed to send email: ${e.message}")
        }
    }
}
