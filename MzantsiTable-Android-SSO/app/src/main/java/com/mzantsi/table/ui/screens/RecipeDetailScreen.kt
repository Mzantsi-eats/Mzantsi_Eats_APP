package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.Strings
import com.mzantsi.table.ui.components.AssetImage
import com.mzantsi.table.ui.components.imagePath
import com.mzantsi.table.ui.theme.MzantsiGreen

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    isSaved: Boolean,
    language: AppLanguage,
    onToggleSave: () -> Unit,
    onMarkCompleted: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← Back") }
            IconButton(onClick = onToggleSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Save",
                    tint = MzantsiGreen
                )
            }
        }

        AssetImage(
            path = recipe.imagePath(),
            contentDescription = recipe.title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MzantsiGreen.copy(alpha = 0.2f))
        ) { Text("🍛", modifier = Modifier.align(Alignment.Center)) }
        Spacer(Modifier.height(10.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(recipe.culture) })
                if (recipe.province.isNotBlank()) AssistChip(onClick = {}, label = { Text(recipe.province) })
            }
            Text(recipe.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill(if (recipe.cookTimeMinutes > 0) "${recipe.prepTimeMinutes} min prep" else "${recipe.prepTimeMinutes} min")
                if (recipe.cookTimeMinutes > 0) StatPill("${recipe.cookTimeMinutes} min cook")
                StatPill(recipe.difficulty.name.lowercase().replaceFirstChar { it.uppercase() })
                StatPill("${recipe.servings} people")
                StatPill("★ ${recipe.ratingAvg}")
            }
            Spacer(Modifier.height(8.dp))
            Text(recipe.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
        }

        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            item {
                Text("Ingredients", style = MaterialTheme.typography.titleMedium, color = MzantsiGreen)
                Spacer(Modifier.height(6.dp))
            }
            items(recipe.ingredients.withIndex().toList()) { (i, ingredient) ->
                NumberedRow(i + 1, ingredient)
            }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Text("Method", style = MaterialTheme.typography.titleMedium, color = MzantsiGreen)
                Spacer(Modifier.height(6.dp))
            }
            items(recipe.method.withIndex().toList()) { (i, step) ->
                NumberedRow(i + 1, step)
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        Button(
            onClick = onMarkCompleted,
            colors = ButtonDefaults.buttonColors(containerColor = MzantsiGreen),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Mark as Completed 🎉")
        }
    }
}

@Composable
private fun StatPill(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall)
}

@Composable
private fun NumberedRow(number: Int, text: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier.size(22.dp).clip(CircleShape).background(MzantsiGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(number.toString(), color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CompletionCelebrationDialog(recipe: Recipe, language: AppLanguage, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Nice!") } },
        title = { Text("🎉 🎊 🎉") },
        text = { Text("${Strings.of("cooked_celebration", language)} ${recipe.title}!") }
    )
}
