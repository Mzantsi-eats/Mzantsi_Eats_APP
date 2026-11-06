package com.mzantsi.table.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiRed

@Composable
fun SavedScreen(
    savedRecipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    downloadedRecipes: List<Recipe> = emptyList(),
    onRemoveDownload: (Recipe) -> Unit = {},
    onDownloadAllSaved: () -> Unit = {},
    startOnDownloads: Boolean = false
) {
    var tab by rememberSaveable { mutableIntStateOf(if (startOnDownloads) 1 else 0) }
    val downloadedIds = downloadedRecipes.map { it.recipeId }.toSet()
    val notDownloaded = savedRecipes.count { it.recipeId !in downloadedIds }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Saved Recipes", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface, contentColor = MzantsiGreen) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Saved (${savedRecipes.size})") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Offline (${downloadedRecipes.size})") })
        }
        Spacer(Modifier.height(12.dp))

        if (tab == 0) {
            if (savedRecipes.isNotEmpty() && notDownloaded > 0) {
                OutlinedButton(onClick = onDownloadAllSaved, modifier = Modifier.fillMaxWidth()) {
                    Text("Download $notDownloaded saved recipe${if (notDownloaded == 1) "" else "s"} for offline")
                }
                Spacer(Modifier.height(12.dp))
            }
            if (savedRecipes.isEmpty()) {
                EmptyMessage("Tap the heart on a recipe to save it here.")
            } else {
                RecipeGrid(savedRecipes, onRecipeClick)
            }
        } else {
            Text(
                "These recipes are saved on your phone. You can open them and cook without internet.",
                style = MaterialTheme.typography.labelSmall, color = Color.Gray
            )
            Spacer(Modifier.height(12.dp))
            if (downloadedRecipes.isEmpty()) {
                EmptyMessage("Tap \"Download\" on a recipe, or \"Download all\" on a culture, to keep it for offline use.")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(downloadedRecipes, key = { it.recipeId }) { recipe ->
                        Column {
                            RecipeCard(recipe) { onRecipeClick(recipe) }
                            TextButton(onClick = { onRemoveDownload(recipe) }) {
                                Text("Remove download", color = MzantsiRed, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeGrid(recipes: List<Recipe>, onRecipeClick: (Recipe) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recipes, key = { it.recipeId }) { recipe -> RecipeCard(recipe) { onRecipeClick(recipe) } }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, textAlign = TextAlign.Center)
    }
}
