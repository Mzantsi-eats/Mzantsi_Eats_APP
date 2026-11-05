package com.mzantsi.table.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.Recipe

@Composable
fun SavedScreen(savedRecipes: List<Recipe>, onRecipeClick: (Recipe) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Saved Recipes", style = MaterialTheme.typography.headlineMedium)
        Text("${savedRecipes.size} recipe${if (savedRecipes.size == 1) "" else "s"} saved", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        if (savedRecipes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tap the heart on a recipe to save it here.")
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(savedRecipes) { recipe -> RecipeCard(recipe) { onRecipeClick(recipe) } }
            }
        }
    }
}
