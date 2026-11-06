package com.mzantsi.table.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.ui.theme.MzantsiGreenDark

object CultureStyle {

    private val colors = mapOf(
        "xhosa" to Color(0xFFB3261E),
        "zulu" to Color(0xFF1E6B3A),
        "cape_malay" to Color(0xFFD99A1B),
        "sesotho" to Color(0xFF7A4A22),
        "braai" to Color(0xFF8E2A12),
        "coloured" to Color(0xFF1F3A8A),
        "desserts" to Color(0xFF6B3F8F),
        "street_food" to Color(0xFF0F6E6E),
        "afrikaner" to Color(0xFF9C5B1A),
        "indian" to Color(0xFFC2410C)
    )

    private val coverDish = mapOf(
        "xhosa" to "umngqusho",
        "zulu" to "bunny_chow",
        "cape_malay" to "cape_malay_chicken_biryani",
        "sesotho" to "mogodu",
        "braai" to "braai_boerewors",
        "coloured" to "koeksisters",
        "desserts" to "peppermint_crisp_tart",
        "street_food" to "samoosas",
        "afrikaner" to "roosterkoek",
        "indian" to "samoosas"
    )

    fun color(culture: String): Color = colors[slugOf(culture)] ?: MzantsiGreenDark

    fun coverPaths(culture: String, recipes: List<Recipe>): List<String> {
        val slug = slugOf(culture)
        return listOfNotNull(
            cultureImagePath(culture),
            coverDish[slug]?.let { "recipes/$it" },
            recipes.firstOrNull { it.culture.equals(culture, ignoreCase = true) }?.imagePath(),
            FEATURED_IMAGE_PATH
        ).distinct()
    }
}

@Composable
fun FlagStripe(modifier: Modifier = Modifier) {
    Row(modifier.height(3.dp)) {
        listOf(Color(0xFF007A4D), Color(0xFFFFB612), Color(0xFFDE3831), Color(0xFF002395)).forEach {
            Box(Modifier.weight(1f).fillMaxHeight().background(it))
        }
    }
}

@Composable
fun CultureCard(
    culture: String,
    recipes: List<Recipe>,
    modifier: Modifier = Modifier,
    showCount: Boolean = true,
    onClick: () -> Unit
) {
    val tint = CultureStyle.color(culture)
    val count = recipes.count { it.culture.equals(culture, ignoreCase = true) }
    val paths = CultureStyle.coverPaths(culture, recipes)
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tint)
            .clickable { onClick() }
    ) {
        AssetImage(
            path = paths.first(),
            fallbackPaths = paths.drop(1),
            contentDescription = culture,
            modifier = Modifier.matchParentSize()
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.45f to tint.copy(alpha = 0.15f),
                    1f to tint.copy(alpha = 0.92f)
                )
            )
        )
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FlagStripe(Modifier.width(56.dp).clip(RoundedCornerShape(2.dp)))
            Spacer(Modifier.height(6.dp))
            Text(
                culture,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (showCount) {
                Text(
                    "$count dish${if (count == 1) "" else "es"}",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
