package com.mzantsi.table.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mzantsi.table.ui.theme.MzantsiGreen

@Composable
fun AuthScreen(
    onAuthenticated: (name: String, email: String) -> Unit
) {
    var isRegisterTab by remember { mutableStateOf(true) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        Text("The Mzantsi Table", style = MaterialTheme.typography.headlineMedium)
        Text("Where South Africa Eats Together", color = MzantsiGreen)
        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth()) {
            SegmentedTab("Register", isRegisterTab, Modifier.weight(1f)) { isRegisterTab = true }
            SegmentedTab("Login", !isRegisterTab, Modifier.weight(1f)) { isRegisterTab = false }
        }
        Spacer(Modifier.height(24.dp))

        if (isRegisterTab) {
            OutlinedTextField(
                value = fullName, onValueChange = { fullName = it },
                label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onAuthenticated(fullName.ifBlank { "User" }, email.ifBlank { "user@example.com" }) },
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
            onClick = { onAuthenticated("Google User", "user@gmail.com") },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue with Google")
        }
    }
}

@Composable
private fun SegmentedTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = if (selected) ButtonDefaults.buttonColors(containerColor = MzantsiGreen)
    else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    Button(onClick = onClick, colors = colors, shape = RoundedCornerShape(20.dp), modifier = modifier.padding(4.dp)) {
        Text(label)
    }
}
