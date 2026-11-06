package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.Cook
import com.mzantsi.table.data.MockData
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.Strings
import com.mzantsi.table.ui.components.AssetImage
import com.mzantsi.table.ui.components.CultureCard
import com.mzantsi.table.ui.components.FEATURED_IMAGE_PATH
import com.mzantsi.table.ui.components.imagePath
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiGreenDark

@Composable
fun HomeScreen(
    language: AppLanguage,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onExploreClick: () -> Unit,
    onCultureClick: (String) -> Unit = {},
    cooks: List<Cook> = emptyList(),
    onCommunityClick: () -> Unit = {},
    onCookClick: (String) -> Unit = {},
    unreadNotifications: Int = 0,
    onNotificationsClick: () -> Unit = {}
) {
    val cultures = (MockData.cultures + recipes.map { it.culture }).distinct()

    var query by remember { mutableStateOf("") }
    val search = query.trim()
    val searchResults = if (search.isEmpty()) emptyList() else recipes.filter { r ->
        r.title.contains(search, ignoreCase = true) ||
            r.culture.contains(search, ignoreCase = true) ||
            r.province.contains(search, ignoreCase = true) ||
            r.ingredients.any { it.contains(search, ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MzantsiGreenDark)
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "The Mzantsi Table",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                NotificationBell(unreadCount = unreadNotifications, onClick = onNotificationsClick)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                placeholder = { Text(Strings.of("home_search_hint", language)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
        }

        if (search.isNotEmpty()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "${searchResults.size} result${if (searchResults.size == 1) "" else "s"} for \"$search\"",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))
                if (searchResults.isEmpty()) {
                    Text("No recipes found. Try another word, like a dish, culture or ingredient.")
                }
                searchResults.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { recipe -> RecipeCard(recipe) { onRecipeClick(recipe) } }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        } else Column(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MzantsiGreenDark)
            ) {
                AssetImage(
                    path = FEATURED_IMAGE_PATH,
                    contentDescription = "Featured",
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    Modifier.matchParentSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))
                    )
                )
                Text(
                    Strings.of("featured_today", language),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                )
            }
            Spacer(Modifier.height(20.dp))

            SectionHeader(Strings.of("popular_right_now", language), Strings.of("discover_more", language)) { onExploreClick() }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recipes.sortedByDescending { it.ratingAvg }) { recipe -> RecipeCard(recipe) { onRecipeClick(recipe) } }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("By Culture", "See all") { onExploreClick() }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cultures) { culture ->
                    CultureCard(
                        culture = culture,
                        recipes = recipes,
                        showCount = false,
                        modifier = Modifier.width(110.dp).height(110.dp),
                        onClick = { onCultureClick(culture) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Community", "See all") { onCommunityClick() }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cooks) { cook ->
                    CommunityCookCard(cook, recipes, Modifier.width(150.dp)) { onCookClick(cook.name) }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onCommunityClick,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Open the Community Hub", color = MzantsiGreen) }
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, onSeeAll: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text("$actionLabel →", color = MzantsiGreen, modifier = Modifier.clickable { onSeeAll() })
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

        AssetImage(
            path = recipe.imagePath(),
            contentDescription = recipe.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MzantsiGreen.copy(alpha = 0.25f))
        ) { Text("🍛", modifier = Modifier.align(Alignment.Center)) }
        Spacer(Modifier.height(6.dp))
        AssistChip(onClick = {}, label = { Text(recipe.culture, style = MaterialTheme.typography.labelSmall) })
        Text(recipe.title, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFE0A83A), modifier = Modifier.size(14.dp))
            Text(" ${recipe.ratingAvg}  ·  ${recipe.prepTimeMinutes + recipe.cookTimeMinutes} min", style = MaterialTheme.typography.labelSmall)
        }
    }
}
