package com.kayodedaniel.gogovmobile.utils

import org.mindrot.jbcrypt.BCrypt

class PasswordUtils {
    companion object {
        // Hash a password using BCrypt
        fun hashPassword(password: String): String {
            // Generate a salt and hash the password
            return BCrypt.hashpw(password, BCrypt.gensalt(12))
        }

        // Verify a password against a stored hash
        fun checkPassword(candidatePassword: String, storedHash: String): Boolean {
            return BCrypt.checkpw(candidatePassword, storedHash)
        }
    }
}