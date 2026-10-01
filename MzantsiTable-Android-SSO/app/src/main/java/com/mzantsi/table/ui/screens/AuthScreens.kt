package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mzantsi.table.auth.AuthValidation
import com.mzantsi.table.auth.GoogleAccount
import com.mzantsi.table.auth.GoogleSignInManager
import com.mzantsi.table.auth.GoogleSignInResult
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.Strings
import com.mzantsi.table.ui.components.AssetImage
import com.mzantsi.table.ui.components.LOGO_IMAGE_PATH
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiRed
import kotlinx.coroutines.launch


@Composable
fun AuthScreen(
    language: AppLanguage,
    startOnLogin: Boolean,
    isLoading: Boolean,
    authError: String?,
    onRegister: (name: String, email: String, password: String) -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onGoogleSignedIn: (GoogleAccount) -> Unit,
    onGoogleError: (String) -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRegisterTab by remember { mutableStateOf(!startOnLogin) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    var showDemoChooser by remember { mutableStateOf(false) }
    var googleBusy by remember { mutableStateOf(false) }

    val nameError = if (attempted && isRegisterTab) AuthValidation.nameError(fullName) else null
    val emailError = if (attempted) AuthValidation.emailError(email) else null
    val passwordError = if (attempted) {
        if (isRegisterTab) AuthValidation.passwordError(password)
        else if (password.isEmpty()) "Password is required" else null
    } else null

    val busy = isLoading || googleBusy

    fun submit() {
        attempted = true
        val valid = AuthValidation.emailError(email) == null &&
            if (isRegisterTab) AuthValidation.nameError(fullName) == null && AuthValidation.passwordError(password) == null
            else password.isNotEmpty()
        if (!valid) return
        if (isRegisterTab) onRegister(fullName.trim(), email.trim(), password) else onLogin(email.trim(), password)
    }

    fun startGoogle() {
        onClearError()
        if (GoogleSignInManager.isConfigured(context)) {
            scope.launch {
                googleBusy = true
                when (val result = GoogleSignInManager.signIn(context)) {
                    is GoogleSignInResult.Success -> onGoogleSignedIn(result.account)
                    is GoogleSignInResult.Failure -> onGoogleError(result.message)
                    GoogleSignInResult.Cancelled -> Unit
                }
                googleBusy = false
            }
        } else {
            showDemoChooser = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            AssetImage(
                path = LOGO_IMAGE_PATH,
                contentDescription = "The Mzantsi Table logo",
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            ) { Text("🍲", fontSize = 44.sp, modifier = Modifier.align(Alignment.Center)) }
            Spacer(Modifier.height(8.dp))
            Text("The Mzantsi Table", style = MaterialTheme.typography.headlineMedium)
            Text(Strings.of("slogan", language), color = MzantsiGreen)
        }
        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth()) {
            SegmentedTab("Register", isRegisterTab, Modifier.weight(1f)) {
                isRegisterTab = true; attempted = false; onClearError()
            }
            SegmentedTab("Login", !isRegisterTab, Modifier.weight(1f)) {
                isRegisterTab = false; attempted = false; onClearError()
            }
        }
        Spacer(Modifier.height(20.dp))

        if (authError != null) {
            ErrorBanner(authError)
            Spacer(Modifier.height(12.dp))
        }

        if (isRegisterTab) {
            AuthField(
                value = fullName,
                onValueChange = { fullName = it; onClearError() },
                label = "Full Name",
                error = nameError,
                keyboardType = KeyboardType.Text
            )
            Spacer(Modifier.height(12.dp))
        }
        AuthField(
            value = email,
            onValueChange = { email = it; onClearError() },
            label = "Email",
            error = emailError,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        AuthField(
            value = password,
            onValueChange = { password = it; onClearError() },
            label = "Password",
            error = passwordError,
            keyboardType = KeyboardType.Password,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailing = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            }
        )

        if (isRegisterTab) {
            Spacer(Modifier.height(8.dp))
            AuthValidation.passwordChecks(password).forEach { (label, ok) ->
                Text(
                    text = (if (ok) "✓  " else "○  ") + label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ok) MzantsiGreen else Color.Gray
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { submit() },
            enabled = !busy,
            colors = ButtonDefaults.buttonColors(containerColor = MzantsiGreen),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text(if (isRegisterTab) "Register" else "Login")
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f))
            Text("  or  ", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            HorizontalDivider(Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { startGoogle() },
            enabled = !busy,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (googleBusy) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Text("Continue with Google")
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            if (isRegisterTab) "Accounts created with Google can only sign in with Google."
            else "If you signed up with Google, use “Continue with Google” to log in.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (!GoogleSignInManager.isConfigured(context)) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Demo mode: add google-services.json to enable real Google accounts (see SETUP_GOOGLE_SSO.md).",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showDemoChooser) {
        DemoGoogleChooserDialog(
            onDismiss = { showDemoChooser = false },
            onAccountChosen = { account ->
                showDemoChooser = false
                onGoogleSignedIn(account)
            }
        )
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = trailing,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MzantsiRed.copy(alpha = 0.12f))
            .padding(12.dp)
    ) {
        Text(message, color = MzantsiRed, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SegmentedTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = if (selected) ButtonDefaults.buttonColors(containerColor = MzantsiGreen)
    else ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Button(onClick = onClick, colors = colors, shape = RoundedCornerShape(20.dp), modifier = modifier.padding(4.dp)) {
        Text(label)
    }
}


@Composable
private fun DemoGoogleChooserDialog(
    onDismiss: () -> Unit,
    onAccountChosen: (GoogleAccount) -> Unit
) {
    val samples = listOf(
        GoogleAccount("demo-1", "Thandi Nkosi", "thandi.nkosi@gmail.com", ""),
        GoogleAccount("demo-2", "Sipho Dlamini", "sipho.dlamini@gmail.com", "")
    )
    var otherName by remember { mutableStateOf("") }
    var otherEmail by remember { mutableStateOf("") }
    var otherError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose an account") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("to continue to The Mzantsi Table (demo)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                samples.forEach { acct ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAccountChosen(acct) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(MzantsiGreen),
                            contentAlignment = Alignment.Center
                        ) { Text(acct.name.first().toString(), color = Color.White) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(acct.name, fontWeight = FontWeight.SemiBold)
                            Text(acct.email, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Use another Google account", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = otherName, onValueChange = { otherName = it },
                    label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = otherEmail, onValueChange = { otherEmail = it; otherError = null },
                    label = { Text("Gmail address") }, singleLine = true,
                    isError = otherError != null,
                    supportingText = { otherError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val err = AuthValidation.emailError(otherEmail)
                if (err != null) {
                    otherError = err
                } else {
                    onAccountChosen(
                        GoogleAccount(
                            uid = "demo-" + otherEmail.trim().lowercase(),
                            name = otherName.trim().ifBlank { otherEmail.substringBefore('@') },
                            email = otherEmail.trim(),
                            photoUrl = ""
                        )
                    )
                }
            }) { Text("Continue") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
