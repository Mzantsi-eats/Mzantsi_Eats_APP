package com.mzantsi.table.auth

import android.util.Patterns

//valid email + password of 8+ chars with upper, lower, digit and special.
object AuthValidation {

    fun nameError(name: String): String? =
        if (name.trim().length < 2) "Please enter your full name" else null

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email address"
        else -> null
    }

    fun passwordChecks(password: String): List<Pair<String, Boolean>> = listOf(
        "At least 8 characters" to (password.length >= 8),
        "One uppercase letter" to password.any { it.isUpperCase() },
        "One lowercase letter" to password.any { it.isLowerCase() },
        "One number" to password.any { it.isDigit() },
        "One special character" to password.any { !it.isLetterOrDigit() && !it.isWhitespace() }
    )

    fun passwordError(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        passwordChecks(password).any { !it.second } -> "Password doesn't meet all the requirements below"
        else -> null
    }
}
