package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.MockData
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.Strings
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiGreenDark

@Composable
fun HomeScreen(
    language: AppLanguage,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onExploreClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MzantsiGreenDark)
                .padding(16.dp)
        ) {
            Text("The Mzantsi Table", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text(Strings.of("home_search_hint", language)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
        }

        Column(Modifier.padding(16.dp)) {
            SectionHeader(Strings.of("popular_right_now", language)) { onExploreClick() }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recipes) { recipe -> RecipeCard(recipe) { onRecipeClick(recipe) } }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("By Culture") { onExploreClick() }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(MockData.cultures) { culture -> CultureChip(culture) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text("See all →", color = MzantsiGreen, modifier = Modifier.clickable { onSeeAll() })
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Column(
        Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MzantsiGreen.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) { Text("🍛") }
        Spacer(Modifier.height(6.dp))
        AssistChip(onClick = {}, label = { Text(recipe.culture, style = MaterialTheme.typography.labelSmall) })
        Text(recipe.title, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFE0A83A), modifier = Modifier.size(14.dp))
            Text(" ${recipe.ratingAvg}  ·  ${recipe.prepTimeMinutes} min", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CultureChip(name: String) {
    Box(
        Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MzantsiGreenDark),
        contentAlignment = Alignment.Center
    ) {
        Text(name, color = Color.White, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
