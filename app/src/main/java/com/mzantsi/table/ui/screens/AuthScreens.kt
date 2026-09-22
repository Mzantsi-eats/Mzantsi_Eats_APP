package com.mzantsi.table.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiRed

@Composable
fun AuthScreen(
    onAuthenticated: (name: String, email: String, password: String) -> Unit
) {
    var isRegisterTab by remember { mutableStateOf(true) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        Text("The Mzantsi Table", style = MaterialTheme.typography.headlineMedium)
        Text("Where South Africa Eats Together", color = MzantsiGreen)
        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth()) {
            SegmentedTab("Register", isRegisterTab, Modifier.weight(1f)) {
                isRegisterTab = true
                errorMessage = null
            }
            SegmentedTab("Login", !isRegisterTab, Modifier.weight(1f)) {
                isRegisterTab = false
                errorMessage = null
            }
        }
        Spacer(Modifier.height(24.dp))

        if (isRegisterTab) {
            OutlinedTextField(
                value = fullName, onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null && fullName.isBlank()
            )
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null && !email.contains("@")
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null && password.length < 6
        )

        errorMessage?.let { msg ->
            Spacer(Modifier.height(8.dp))
            Text(msg, color = MzantsiRed, style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                val validationError = validate(isRegisterTab, fullName, email, password)
                if (validationError != null) {
                    errorMessage = validationError
                } else {
                    errorMessage = null
                    onAuthenticated(
                        fullName.ifBlank { "User" },
                        email.trim(),
                        password
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MzantsiGreen),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegisterTab) "Register" else "Login")
        }
        Spacer(Modifier.height(12.dp))
        Text("or", modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                onAuthenticated("Google User", "user@gmail.com", "google_oauth")
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue with Google")
        }
    }
}

private fun validate(
    isRegister: Boolean,
    name: String,
    email: String,
    password: String
): String? {
    if (isRegister && name.isBlank()) return "Please enter your full name"
    if (!email.contains("@") || !email.contains(".")) return "Please enter a valid email address"
    if (password.length < 6) return "Password must be at least 6 characters"
    if (isRegister && password.none { it.isDigit() }) return "Password must contain a number"
    return null
}

@Composable
private fun SegmentedTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = if (selected) ButtonDefaults.buttonColors(containerColor = MzantsiGreen)
    else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    Button(onClick = onClick, colors = colors, shape = RoundedCornerShape(20.dp), modifier = modifier.padding(4.dp)) {
        Text(label)
    }
}