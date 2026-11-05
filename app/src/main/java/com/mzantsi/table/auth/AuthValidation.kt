package com.mzantsi.table.auth

import android.util.Patterns

object AuthValidation {

    const val MIN_PASSWORD_LENGTH = 8

    fun nameError(name: String): String? = when {
        name.isBlank() -> "Please enter your full name"
        name.trim().length < 2 -> "Your name needs at least 2 letters"
        else -> null
    }

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email address, like name@example.com"
        else -> null
    }

    fun missingPasswordParts(password: String): List<String> {
        val missing = mutableListOf<String>()
        if (password.length < MIN_PASSWORD_LENGTH) {
            val left = MIN_PASSWORD_LENGTH - password.length
            missing += "$left more character${if (left == 1) "" else "s"}"
        }
        if (password.none { it.isUpperCase() }) missing += "an uppercase letter (A-Z)"
        if (password.none { it.isLowerCase() }) missing += "a lowercase letter (a-z)"
        if (password.none { it.isDigit() }) missing += "a number (0-9)"
        if (password.none { !it.isLetterOrDigit() && !it.isWhitespace() }) missing += "a special character (like ! @ # ?)"
        return missing
    }

    fun isStrongPassword(password: String): Boolean =
        missingPasswordParts(password).isEmpty() && !hasEdgeSpaces(password)

    private fun hasEdgeSpaces(password: String) =
        password.isNotEmpty() && (password.first().isWhitespace() || password.last().isWhitespace())

    fun newPasswordError(password: String): String? {
        if (password.isEmpty()) return "Create a password"
        if (hasEdgeSpaces(password)) return "Remove the space at the start or end of your password"
        val missing = missingPasswordParts(password)
        return if (missing.isEmpty()) null else "Add " + joinNaturally(missing)
    }


    fun loginPasswordError(password: String): String? = when {
        password.isEmpty() -> "Enter your password"
        password.length < MIN_PASSWORD_LENGTH ->
            "Passwords are at least $MIN_PASSWORD_LENGTH characters. Check what you typed"
        else -> null
    }

    fun confirmPasswordError(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Type your password again to confirm it"
        confirm != password -> "Passwords don't match"
        else -> null
    }

    private fun joinNaturally(parts: List<String>): String = when (parts.size) {
        0 -> ""
        1 -> parts[0]
        else -> parts.dropLast(1).joinToString(", ") + " and " + parts.last()
    }
}
