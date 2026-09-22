package com.mzantsi.table.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiRed

@Composable
fun AuthScreen(
    onRegister: (name: String, email: String, password: String, onResult: (String?) -> Unit) -> Unit,
    onLogin: (email: String, password: String, onResult: (String?) -> Unit) -> Unit,
    onContinueWithGoogle: (onResult: (String?) -> Unit) -> Unit,
    onAuthenticated: () -> Unit
) {
    var isRegisterTab by remember { mutableStateOf(true) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun switchTab(register: Boolean) {
        isRegisterTab = register
        errorMessage = null
    }

    fun submit() {
        errorMessage = null

        if (isRegisterTab) {
            onRegister(fullName, email, password) { error ->
                if (error == null) {
                    errorMessage = null
                    onAuthenticated()
                }
                else {
                    errorMessage = error
                }
            }
        } else {
            onLogin(email, password) { error ->
                if (error == null) {
                    errorMessage = null
                    onAuthenticated()
                }
                else {
                    errorMessage = error
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        Text("The Mzantsi Table", style = MaterialTheme.typography.headlineMedium)
        Text("Where South Africa Eats Together", color = MzantsiGreen)
        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth()) {
            SegmentedTab("Register", isRegisterTab, Modifier.weight(1f)) { switchTab(true) }
            SegmentedTab("Login", !isRegisterTab, Modifier.weight(1f)) { switchTab(false) }
        }
        Spacer(Modifier.height(24.dp))

        if (isRegisterTab) {
            OutlinedTextField(
                value = fullName, onValueChange = { fullName = it },
                label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (!isRegisterTab) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Demo account: demo@mzantsi.com / password123",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(errorMessage!!, color = MzantsiRed, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { submit() },
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
                onContinueWithGoogle { error ->
                    if (error == null) {
                        onAuthenticated()
                    }
                    else {
                        errorMessage = error
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue with Google")
        }
    }
}

@Composable
private fun SegmentedTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(containerColor = MzantsiGreen)
    }
    else {
        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }
    Button(onClick = onClick, colors = colors, shape = RoundedCornerShape(20.dp), modifier = modifier.padding(4.dp)) {
        Text(label)
    }
}
