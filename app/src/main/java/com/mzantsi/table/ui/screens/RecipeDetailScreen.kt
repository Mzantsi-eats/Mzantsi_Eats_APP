package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.Review
import com.mzantsi.table.data.Strings
import com.mzantsi.table.ui.theme.MzantsiGreen

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    isSaved: Boolean,
    language: AppLanguage,
    reviews: List<Review>,
    onSubmitReview: (Int, String) -> Unit,
    onToggleSave: () -> Unit,
    onMarkCompleted: () -> Unit,
    onBack: () -> Unit
) {
    var showReviewDialog by remember { mutableStateOf(false) }

    if (showReviewDialog) {
        var rating by remember { mutableStateOf(5) }
        var comment by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Write a Review") },
            text = {
                Column {
                    Text("Rating:")
                    Row {
                        (1..5).forEach { star ->
                            TextButton(onClick = { rating = star }) {
                                Text(
                                    text = if (star <= rating) "★" else "☆",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFFE0A83A)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Your Comment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSubmitReview(rating, comment)
                    showReviewDialog = false
                }) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") }
            }
        )
    }

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
        Column(Modifier.padding(horizontal = 16.dp)) {
            AssistChip(onClick = {}, label = { Text(recipe.culture) })
            Text(recipe.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatPill("${recipe.prepTimeMinutes} min")
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
            item { Spacer(Modifier.height(24.dp)) }
            item {
                Button(
                    onClick = onMarkCompleted,
                    colors = ButtonDefaults.buttonColors(containerColor = MzantsiGreen),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Mark as Completed 🎉")
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
            item {
                Text("Reviews", style = MaterialTheme.typography.titleMedium, color = MzantsiGreen)
                Spacer(Modifier.height(8.dp))
            }
            items(reviews) { review ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("★".repeat(review.rating), color = Color(0xFFE0A83A))
                    Spacer(Modifier.width(8.dp))
                    Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showReviewDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Write a Review") }
            }
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
