package com.mzantsi.table.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.Strings
import com.mzantsi.table.ui.components.AssetImage
import com.mzantsi.table.ui.components.LOGO_IMAGE_PATH
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiOrange


@Composable
fun LaunchScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onLogin: () -> Unit = {},
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        AssetImage(
            path = LOGO_IMAGE_PATH,
            contentDescription = "The Mzantsi Table logo",
            modifier = Modifier.size(110.dp).clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit
        ) { Text("🍲", fontSize = 64.sp, modifier = Modifier.align(Alignment.Center)) }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "The Mzantsi Table",
            style = MaterialTheme.typography.headlineLarge,
            fontStyle = FontStyle.Italic
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = Strings.of("slogan", language),
            color = MzantsiOrange,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = Strings.of("tagline", language),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGetStarted,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MzantsiGreen),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(Strings.of("get_started", language))
        }
        TextButton(onClick = onLogin) {
            Text("Already have an account? Log in", color = MzantsiGreen)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LanguageChip("EN", language == AppLanguage.EN) { onLanguageChange(AppLanguage.EN) }
            LanguageChip("ZU", language == AppLanguage.ZU) { onLanguageChange(AppLanguage.ZU) }
            LanguageChip("ST", language == AppLanguage.ST) { onLanguageChange(AppLanguage.ST) }
        }
    }
}

@Composable
private fun LanguageChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
