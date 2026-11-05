package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.AuthProvider
import com.mzantsi.table.data.User
import com.mzantsi.table.auth.AuthValidation
import com.mzantsi.table.ui.components.PasswordField
import androidx.compose.ui.text.input.ImeAction
import com.mzantsi.table.ui.theme.MzantsiGold
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiGreenDark
import com.mzantsi.table.ui.theme.MzantsiRed

// The Profile screen: account details, dietary preferences, language, notifications and Sign Out.
// Every change here is saved straight away (to Firestore, or to the phone in offline mode).
@Composable
fun ProfileScreen(
    user: User?,
    language: AppLanguage,
    dietaryPreferences: Set<String>,
    notificationsEnabled: Boolean,
    onNameChange: (String, String) -> Unit,
    onDietaryToggle: (String) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onSignOut: () -> Unit,
    onChangePassword: (current: String, new: String, onResult: (String?) -> Unit) -> Unit = { _, _, _ -> },
    isBusy: Boolean = false
) {
    var showChangePassword by remember { mutableStateOf(false) }
    var passwordChangedMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    var editingName by remember { mutableStateOf(false) }
    var nameField by remember { mutableStateOf(user?.name.orEmpty()) }

    // When the saved name changes, update the text box too (but not while the user is typing).
    LaunchedEffect(user?.name) {
        if (!editingName) nameField = user?.name.orEmpty()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(Modifier.height(24.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .background(MzantsiGreenDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(MzantsiGold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user?.name?.firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                user?.name?.ifBlank { "Your Name" } ?: "Your Name",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                user?.email?.ifBlank { "your@email.com" } ?: "your@email.com",
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        Column(Modifier.padding(16.dp)) {

            // Account details. Only the name can be edited.
            if (editingName) {
                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onNameChange(nameField.ifBlank { "User" }, "")
                            editingName = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MzantsiGreen),
                        modifier = Modifier.weight(1f)
                    ) { Text("Save") }
                    OutlinedButton(
                        onClick = {
                            nameField = user?.name.orEmpty()
                            editingName = false
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }
                }
            } else {
                ProfileRow(
                    label = "Full Name",
                    value = user?.name.orEmpty().ifBlank { "—" },
                    onEdit = { editingName = true }
                )
            }
            ProfileRow("Email", user?.email.orEmpty().ifBlank { "—" })
            ProfileRow(
                "Sign-in method",
                if (user?.authProvider == AuthProvider.GOOGLE) "Google (single sign-on)" else "Email & password"
            )
            ProfileRow("Recipes Added", (user?.recipesAdded ?: 0).toString())

            // Password (only for email accounts; Google accounts are managed by Google)
            if (user != null && user.authProvider == AuthProvider.EMAIL) {
                Spacer(Modifier.height(16.dp))
                SectionTitle("Password")
                OutlinedButton(
                    onClick = { passwordChangedMessage = null; showChangePassword = true },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Change Password") }
                passwordChangedMessage?.let {
                    Text(it, color = MzantsiGreen, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Dietary preferences
            SectionTitle("Dietary Preferences")
            Text(
                "Select any that apply. They are saved to your profile.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))

            val allPrefs = listOf(
                "Vegetarian", "Vegan", "Halal", "Kosher",
                "Gluten-Free", "Dairy-Free", "Nut-Free"
            )
            allPrefs.chunked(2).forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { pref ->
                        FilterChip(
                            selected = pref in dietaryPreferences,
                            onClick = { onDietaryToggle(pref) },
                            label = { Text(pref, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Language
            SectionTitle("Language")
            Text(
                "Choose your preferred app language.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))

            val languages = listOf(
                AppLanguage.EN to "English",
                AppLanguage.ZU to "isiZulu",
                AppLanguage.ST to "Sesotho"
            )
            languages.forEach { (lang, label) ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = language == lang,
                        onClick = { onLanguageChange(lang) }
                    )
                    Text(label)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Notifications
            SectionTitle("Notifications")
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Enable notifications", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Get alerts for reviews, new recipes and updates.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { onNotificationsToggle(it) }
                )
            }

            Spacer(Modifier.height(32.dp))

            // Sign out
            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MzantsiRed),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sign Out") }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showChangePassword) {
        ChangePasswordDialog(
            isBusy = isBusy,
            onDismiss = { showChangePassword = false },
            onSubmit = onChangePassword,
            onChanged = {
                showChangePassword = false
                passwordChangedMessage = "Your password was changed."
            }
        )
    }
}

// The pop-up for changing the password. The new password is checked while the user types,
// with the same rules as the Register screen.
@Composable
private fun ChangePasswordDialog(
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (current: String, new: String, onResult: (String?) -> Unit) -> Unit,
    onChanged: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }
    var serverError by remember { mutableStateOf<String?>(null) }

    val currentError = if (attempted && current.isEmpty()) "Enter your current password" else null
    val newError = if (attempted || newPassword.isNotEmpty()) AuthValidation.newPasswordError(newPassword) else null
    val newSuccess = if (AuthValidation.isStrongPassword(newPassword)) "Strong password" else null
    val confirmError = if (attempted || confirm.isNotEmpty()) AuthValidation.confirmPasswordError(newPassword, confirm) else null
    val confirmSuccess = if (confirm.isNotEmpty() && confirm == newPassword) "Passwords match" else null

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = { Text("Change Password") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                serverError?.let {
                    Text(it, color = MzantsiRed, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }
                PasswordField(
                    value = current,
                    onValueChange = { current = it; serverError = null },
                    label = "Current Password",
                    error = currentError,
                    imeAction = ImeAction.Next
                )
                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it; serverError = null },
                    label = "New Password",
                    error = newError,
                    success = newSuccess,
                    imeAction = ImeAction.Next
                )
                PasswordField(
                    value = confirm,
                    onValueChange = { confirm = it; serverError = null },
                    label = "Confirm New Password",
                    error = confirmError,
                    success = confirmSuccess
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isBusy,
                onClick = {
                    attempted = true
                    val valid = current.isNotEmpty() &&
                        AuthValidation.newPasswordError(newPassword) == null &&
                        AuthValidation.confirmPasswordError(newPassword, confirm) == null
                    if (valid) {
                        onSubmit(current, newPassword) { error ->
                            if (error == null) onChanged() else serverError = error
                        }
                    }
                }
            ) {
                if (isBusy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        },
        dismissButton = { TextButton(enabled = !isBusy, onClick = onDismiss) { Text("Cancel") } }
    )
}

// A small green heading for each part of the Profile screen.
@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MzantsiGreen,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

// One line of account details. If onEdit is given, an "Edit" button is shown.
@Composable
private fun ProfileRow(
    label: String,
    value: String,
    onEdit: (() -> Unit)? = null
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall)
            if (onEdit != null) {
                TextButton(onClick = onEdit) {
                    Text("Edit", color = MzantsiGreen, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(value, style = MaterialTheme.typography.bodyMedium)
        Divider(Modifier.padding(top = 8.dp))
    }
}