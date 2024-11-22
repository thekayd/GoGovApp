
package com.kayodedaniel.gogovmobile

import org.junit.Assert.assertTrue
import org.junit.Test
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
    @SuppressWarnings("all")
    @Test
    fun testPasswordStrength() {
        val testPassword = "Password@123"
        assertTrue("Password does not meet security requirements", passwordPattern.matcher(testPassword).matches())
    }
}
