package com.mzantsi.table.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.Difficulty
import com.mzantsi.table.data.MockData
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.ui.components.AssetImage
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiRed
import java.io.File

private val provinces = listOf(
    "Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal", "Limpopo",
    "Mpumalanga", "North West", "Northern Cape", "Western Cape"
)


@Composable
fun AddRecipeScreen(
    nextRecipeId: Int,
    onSave: (Recipe) -> Unit,
    onBack: () -> Unit,
    authorName: String = "You"
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var culture by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var provinceMenuOpen by remember { mutableStateOf(false) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var servings by remember { mutableStateOf("4") }
    var prepTime by remember { mutableStateOf("30") }
    var cookTime by remember { mutableStateOf("30") }
    var description by remember { mutableStateOf("") }
    var ingredientsText by remember { mutableStateOf("") }
    var methodText by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val cultureOptions = remember { (MockData.cultures + listOf("Afrikaner", "Indian", "Other")).distinct() }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val dest = File(context.filesDir, "recipe_${System.currentTimeMillis()}.jpg")
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { out -> input.copyTo(out) }
                }
            }
            if (dest.exists() && dest.length() > 0) photoPath = dest.absolutePath
        }
    }

    fun save() {
        val ingredients = ingredientsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val steps = methodText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        error = when {
            title.isBlank() -> "Please give your recipe a title."
            culture.isBlank() -> "Please choose a cultural category."
            ingredients.isEmpty() -> "Add at least one ingredient (one per line)."
            steps.isEmpty() -> "Add at least one method step (one per line)."
            else -> null
        }
        if (error != null) return
        onSave(
            Recipe(
                recipeId = nextRecipeId,
                title = title.trim(),
                culture = culture,
                province = province,
                description = description.trim(),
                imageRes = photoPath?.let { "file:$it" }.orEmpty(),
                prepTimeMinutes = prepTime.toIntOrNull() ?: 30,
                cookTimeMinutes = cookTime.toIntOrNull() ?: 0,
                servings = servings.toIntOrNull() ?: 4,
                difficulty = difficulty,
                ratingAvg = 0f,
                ingredients = ingredients,
                method = steps,
                authorName = authorName
            )
        )
        onBack()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Back") }
            Spacer(Modifier.weight(1f))
            Text("Add Recipe", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(72.dp))
        }
        Spacer(Modifier.height(12.dp))

        // Photo upload
        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MzantsiGreen.copy(alpha = 0.12f))
        ) {
            val path = photoPath
            if (path != null) {
                AssetImage("file:$path", "Recipe photo", Modifier.matchParentSize())
            } else {
                Text("📷  No photo yet", modifier = Modifier.align(Alignment.Center))
            }
        }
        OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text(if (photoPath == null) "Upload photo" else "Change photo")
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            title, { title = it; error = null },
            label = { Text("Recipe title (e.g. Mama's Umngqusho)") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Text("Cultural category", style = MaterialTheme.typography.labelSmall)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cultureOptions.forEach { option ->
                FilterChip(
                    selected = culture == option,
                    onClick = { culture = option; error = null },
                    label = { Text(option) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Text("Province", style = MaterialTheme.typography.labelSmall)
        Box {
            OutlinedButton(onClick = { provinceMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                Text(province.ifBlank { "Select province" })
            }
            DropdownMenu(expanded = provinceMenuOpen, onDismissRequest = { provinceMenuOpen = false }) {
                provinces.forEach { p ->
                    DropdownMenuItem(text = { Text(p) }, onClick = { province = p; provinceMenuOpen = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Text("Difficulty", style = MaterialTheme.typography.labelSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Difficulty.values().forEach { d ->
                FilterChip(
                    selected = difficulty == d,
                    onClick = { difficulty = d },
                    label = { Text(d.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                servings, { servings = it.filter(Char::isDigit) }, label = { Text("Servings") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                prepTime, { prepTime = it.filter(Char::isDigit) }, label = { Text("Prep (min)") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                cookTime, { cookTime = it.filter(Char::isDigit) }, label = { Text("Cook (min)") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(ingredientsText, { ingredientsText = it; error = null }, label = { Text("Ingredients (one per line)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(methodText, { methodText = it; error = null }, label = { Text("Method steps (one per line)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MzantsiRed, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { save() },
            colors = ButtonDefaults.buttonColors(containerColor = MzantsiGreen),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Recipe") }
        Spacer(Modifier.height(24.dp))
    }
}
