package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.User
import com.mzantsi.table.ui.theme.MzantsiGold
import com.mzantsi.table.ui.theme.MzantsiGreenDark
import com.mzantsi.table.ui.theme.MzantsiRed

@Composable
fun ProfileScreen(user: User?, onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxWidth().background(MzantsiGreenDark).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(MzantsiGold),
                contentAlignment = Alignment.Center
            ) {
                Text(user?.name?.firstOrNull()?.uppercase() ?: "U", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text(user?.name?.ifBlank { "Your Name" } ?: "Your Name", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(user?.email?.ifBlank { "your@email.com" } ?: "your@email.com", color = Color.White.copy(alpha = 0.8f))
        }
        Column(Modifier.padding(16.dp)) {
            ProfileRow("Full Name", user?.name.orEmpty().ifBlank { "—" })
            ProfileRow("Email", user?.email.orEmpty().ifBlank { "—" })
            ProfileRow("Recipes Added", (user?.recipesAdded ?: 0).toString())
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MzantsiRed),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sign Out") }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
        Divider(Modifier.padding(top = 8.dp))
    }
}
