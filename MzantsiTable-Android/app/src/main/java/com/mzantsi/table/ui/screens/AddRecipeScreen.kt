package com.mzantsi.table.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.Difficulty
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiOrange

@Composable
fun AddRecipeScreen(
    nextRecipeId: Int,
    onSave: (Recipe) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var culture by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("4") }
    var prepTime by remember { mutableStateOf("30") }
    var description by remember { mutableStateOf("") }
    var ingredientsText by remember { mutableStateOf("") }
    var methodText by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("← Back") }
            Text("Add Recipe", style = MaterialTheme.typography.titleMedium)
            TextButton(
                onClick = {
                    onSave(
                        Recipe(
                            recipeId = nextRecipeId,
                            title = title.ifBlank { "Untitled Recipe" },
                            culture = culture.ifBlank { "Other" },
                            description = description,
                            prepTimeMinutes = prepTime.toIntOrNull() ?: 30,
                            servings = servings.toIntOrNull() ?: 4,
                            difficulty = Difficulty.MEDIUM,
                            ratingAvg = 0f,
                            ingredients = ingredientsText.split("\n").filter { it.isNotBlank() },
                            method = methodText.split("\n").filter { it.isNotBlank() },
                            authorName = "You"
                        )
                    )
                    onBack()
                }
            ) { Text("Save", color = MzantsiOrange) }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(title, { title = it }, label = { Text("Recipe title (e.g. Mama's Umngqusho)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(culture, { culture = it }, label = { Text("Cultural category (e.g. Xhosa, Zulu, Cape Malay)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(servings, { servings = it }, label = { Text("Servings") }, modifier = Modifier.weight(1f))
            OutlinedTextField(prepTime, { prepTime = it }, label = { Text("Prep time (min)") }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(ingredientsText, { ingredientsText = it }, label = { Text("Ingredients (one per line)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(methodText, { methodText = it }, label = { Text("Method steps (one per line)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                onSave(
                    Recipe(
                        recipeId = nextRecipeId,
                        title = title.ifBlank { "Untitled Recipe" },
                        culture = culture.ifBlank { "Other" },
                        description = description,
                        prepTimeMinutes = prepTime.toIntOrNull() ?: 30,
                        servings = servings.toIntOrNull() ?: 4,
                        ingredients = ingredientsText.split("\n").filter { it.isNotBlank() },
                        method = methodText.split("\n").filter { it.isNotBlank() },
                        authorName = "You"
                    )
                )
                onBack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MzantsiGreen),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Recipe") }
    }
}
