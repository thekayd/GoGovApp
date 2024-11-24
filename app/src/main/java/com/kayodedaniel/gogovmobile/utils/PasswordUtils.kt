package com.kayodedaniel.gogovmobile.utils

import org.mindrot.jbcrypt.BCrypt

class PasswordUtils {
    companion object {
        // Hashes a password using BCrypt
        fun hashPassword(password: String): String {
            // Generates a salt and hash the password
            return BCrypt.hashpw(password, BCrypt.gensalt(12))
        }

        // Verifies a password against a stored hash
        fun checkPassword(candidatePassword: String, storedHash: String): Boolean {
            return BCrypt.checkpw(candidatePassword, storedHash)
        }
    }
}