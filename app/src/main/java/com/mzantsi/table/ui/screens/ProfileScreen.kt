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
import com.mzantsi.table.data.User
import com.mzantsi.table.ui.theme.MzantsiGold
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiGreenDark
import com.mzantsi.table.ui.theme.MzantsiRed

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
    onSignOut: () -> Unit
) {
    val scrollState = rememberScrollState()
    var editingName by remember { mutableStateOf(false) }
    var nameField by remember { mutableStateOf(user?.name.orEmpty()) }

    // Keep the name field in sync when the user object changes
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

            //Account Details (with editable name)
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
            ProfileRow("Recipes Added", (user?.recipesAdded ?: 0).toString())

            Spacer(Modifier.height(24.dp))

            // Dietary Preferences
            SectionTitle("Dietary Preferences")
            Text(
                "Select any that apply — we'll highlight matching recipes.",
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

            //Notifications
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

            // Sign Out
            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MzantsiRed),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sign Out") }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MzantsiGreen,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

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