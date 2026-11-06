package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.MockData
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.ui.components.CultureCard
import com.mzantsi.table.ui.theme.MzantsiGreenDark

@Composable
fun ExploreScreen(recipes: List<Recipe>, onCultureClick: (String) -> Unit) {
    val cultures = (MockData.cultures + recipes.map { it.culture }).distinct()

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MzantsiGreenDark)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text("Explore Cultures", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Text(
                "South Africa's rich culinary heritage",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(cultures) { culture ->
                CultureCard(
                    culture = culture,
                    recipes = recipes,
                    modifier = Modifier.fillMaxWidth().aspectRatio(0.82f),
                    onClick = { onCultureClick(culture) }
                )
            }
        }
    }
}
