package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import com.mzantsi.table.ui.components.AssetImage
import com.mzantsi.table.ui.components.imagePath
import com.mzantsi.table.ui.theme.MzantsiGold
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiRed
import java.util.Locale

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    isSaved: Boolean,
    language: AppLanguage,
    reviews: List<Review>,
    onToggleSave: () -> Unit,
    onMarkCompleted: () -> Unit,
    onAddReview: (rating: Int, comment: String) -> Unit,
    onBack: () -> Unit,
    isDownloaded: Boolean = false,
    onToggleDownload: () -> Unit = {}
) {
    var showReviewDialog by remember { mutableStateOf(false) }

    val rating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average().toFloat() else recipe.ratingAvg
    val ratingText = "★ " + String.format(Locale.US, "%.1f", rating) +
        if (reviews.isNotEmpty()) " (${reviews.size})" else ""

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← Back") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onToggleDownload) {
                    Icon(
                        imageVector = if (isDownloaded) Icons.Filled.OfflinePin else Icons.Filled.DownloadForOffline,
                        contentDescription = null,
                        tint = MzantsiGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isDownloaded) "Available offline" else "Download", color = MzantsiGreen)
                }
                IconButton(onClick = onToggleSave) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isSaved) "Remove from saved" else "Save",
                        tint = MzantsiGreen
                    )
                }
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
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPill(if (recipe.cookTimeMinutes > 0) "${recipe.prepTimeMinutes} min prep" else "${recipe.prepTimeMinutes} min")
                if (recipe.cookTimeMinutes > 0) StatPill("${recipe.cookTimeMinutes} min cook")
                StatPill(recipe.difficulty.name.lowercase().replaceFirstChar { it.uppercase() })
                StatPill("${recipe.servings} people")
                StatPill(ratingText)
            }
            if (recipe.authorName.isNotBlank()) {
                Text("By ${recipe.authorName}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
            item { Spacer(Modifier.height(16.dp)) }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reviews", style = MaterialTheme.typography.titleMedium, color = MzantsiGreen)
                    TextButton(onClick = { showReviewDialog = true }) {
                        Text("Write a Review", color = MzantsiGreen)
                    }
                }
                if (reviews.isEmpty()) {
                    Text(
                        "No reviews yet. Be the first to share what you think!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            items(reviews) { review -> ReviewRow(review) }
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

    if (showReviewDialog) {
        WriteReviewDialog(
            recipeTitle = recipe.title,
            onDismiss = { showReviewDialog = false },
            onSubmit = { stars, comment ->
                onAddReview(stars, comment)
                showReviewDialog = false
            }
        )
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
            Text(number.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ReviewRow(review: Review) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(review.userName.ifBlank { "Anonymous" }, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(8.dp))
            val stars = review.rating.coerceIn(0, 5)
            Text("★".repeat(stars) + "☆".repeat(5 - stars), color = MzantsiGold)
        }
        if (review.comment.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun WriteReviewDialog(
    recipeTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String) -> Unit
) {
    var stars by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review $recipeTitle") },
        text = {
            Column {
                Row {
                    (1..5).forEach { n ->
                        IconButton(onClick = { stars = n; error = null }) {
                            Icon(
                                imageVector = if (n <= stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "$n star${if (n == 1) "" else "s"}",
                                tint = MzantsiGold
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your comment (optional)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = MzantsiRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (stars == 0) {
                    error = "Please tap a star to give a rating."
                } else {
                    onSubmit(stars, comment)
                }
            }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RecipeNotFound(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Spacer(Modifier.height(24.dp))
        Text("Sorry, we couldn't find this recipe.", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun CompletionCelebrationDialog(
    recipe: Recipe,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onShareWithCommunity: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (onShareWithCommunity != null) {
                TextButton(onClick = onShareWithCommunity) { Text("Share with the community") }
            } else {
                TextButton(onClick = onDismiss) { Text("Nice!") }
            }
        },
        dismissButton = if (onShareWithCommunity != null) {
            { TextButton(onClick = onDismiss) { Text("Nice!") } }
        } else null,
        title = { Text("🎉 🎊 🎉") },
        text = { Text("${Strings.of("cooked_celebration", language)} ${recipe.title}!") }
    )
}
