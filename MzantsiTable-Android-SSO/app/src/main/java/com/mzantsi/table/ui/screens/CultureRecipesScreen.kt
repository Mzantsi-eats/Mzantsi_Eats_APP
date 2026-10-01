package com.mzantsi.table.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.Recipe


@Composable
fun CultureRecipesScreen(
    culture: String,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text(culture, style = MaterialTheme.typography.headlineMedium)
        Text(
            "${recipes.size} recipe${if (recipes.size == 1) "" else "s"}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        if (recipes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No $culture recipes yet. Be the first to add one from the Add tab!")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes) { recipe -> RecipeCard(recipe) { onRecipeClick(recipe) } }
            }
        }
    }
}
