
package com.kayodedaniel.gogovmobile

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Properties
import java.util.regex.Pattern

class SignUpSecurityTest {

    private val passwordPattern = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +
                "(?=.*[a-z])" +
                "(?=.*[A-Z])" +
                "(?=.*[@#\$%^&+=])" +
                "(?=\\S+\$)" +
                ".{8,}" +
                "\$"
    )


    private fun loadTestPassword(): String {
        val properties = Properties()
        val file = File("local.properties")
        if (file.exists()) {
            properties.load(file.inputStream())
        }
        return properties.getProperty("test.password", "Password@123")
    }
    @SuppressWarnings("all")
    @Test
    fun testPasswordStrength() {
        val testPassword = loadTestPassword()
        assertTrue("Password does not meet security requirements", passwordPattern.matcher(testPassword).matches())
    }
}
