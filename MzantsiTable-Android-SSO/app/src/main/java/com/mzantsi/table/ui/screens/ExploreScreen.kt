package com.mzantsi.table.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.MockData
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.ui.components.AssetImage
import com.mzantsi.table.ui.components.cultureImagePath
import com.mzantsi.table.ui.theme.MzantsiGreenDark


@Composable
fun ExploreScreen(recipes: List<Recipe>, onCultureClick: (String) -> Unit) {
    val cultures = (MockData.cultures + recipes.map { it.culture }).distinct()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Explore Cultures", style = MaterialTheme.typography.headlineMedium)
        Text("South Africa's rich culinary heritage", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(cultures) { culture ->
                val count = recipes.count { it.culture.equals(culture, ignoreCase = true) }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MzantsiGreenDark)
                        .clickable { onCultureClick(culture) }
                ) {

                    AssetImage(
                        path = cultureImagePath(culture),
                        contentDescription = culture,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        Modifier.matchParentSize().background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))
                        )
                    )
                    Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                        Text(culture, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("$count dish${if (count == 1) "" else "es"}", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
