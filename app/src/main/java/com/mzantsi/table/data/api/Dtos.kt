package com.mzantsi.table.data.api

import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.Difficulty
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.User

// What we send to the API.
data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class SettingsRequest(val dietaryPrefs: String, val languagePref: String)

// A recipe sent to the API. The names match the API's Recipe model (steps, imageUrl).
// The API gives the recipe its own id, and it reads difficulty as a number (0 = Easy, 1 = Medium, 2 = Hard).
data class RecipeRequest(
    val title: String,
    val culture: String,
    val province: String,
    val description: String,
    val imageUrl: String,
    val prepTimeMinutes: Int,
    val servings: Int,
    val difficulty: Int,
    val ingredients: List<String>,
    val steps: List<String>,
    val authorName: String
)

// A review sent to the API (the recipe id goes in the address, the API gives the review its own id).
data class ReviewRequest(
    val userId: Int,
    val rating: Int,
    val comment: String
)

// The API's answer to a review. We don't use the content, but the call needs a type to read it into.
data class ReviewDto(
    val reviewId: Int? = null,
    val recipeId: Int? = null,
    val rating: Int? = null
)


// What the API sends back. Every field can be missing, so we give safe defaults when converting.
data class UserDto(
    val userId: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val dietaryPrefs: String? = null,
    val languagePref: String? = null,
    val recipesAdded: Int? = null
) {
    fun toUser() = User(
        userId = userId ?: 1,
        name = name.orEmpty(),
        email = email.orEmpty(),
        dietaryPrefs = dietaryPrefs.orEmpty(),
        languagePref = runCatching { AppLanguage.valueOf(languagePref.orEmpty().uppercase()) }
            .getOrDefault(AppLanguage.EN),
        recipesAdded = recipesAdded ?: 0
    )
}

data class RecipeDto(
    val recipeId: Int? = null,
    val title: String? = null,
    val culture: String? = null,
    val province: String? = null,
    val description: String? = null,
    val imageRes: String? = null,
    // The ASP.NET API calls these imageUrl and steps (the Firebase side calls them imageRes and method).
    val imageUrl: String? = null,
    val steps: List<String>? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int? = null,
    val difficulty: String? = null,
    val ratingAvg: Float? = null,
    val ingredients: List<String>? = null,
    val method: List<String>? = null,
    val authorName: String? = null
) {
    fun toRecipe(): Recipe? {
        val id = recipeId ?: return null
        val name = title ?: return null
        return Recipe(
            recipeId = id,
            title = name,
            culture = culture ?: "Other",
            province = province.orEmpty(),
            description = description.orEmpty(),
            imageRes = (imageRes ?: imageUrl).orEmpty(),
            prepTimeMinutes = prepTimeMinutes ?: 0,
            cookTimeMinutes = cookTimeMinutes ?: 0,
            servings = servings ?: 1,
            // The API sends difficulty as a number (0, 1, 2); the Firebase side uses the name (EASY, ...).
            difficulty = difficulty?.trim()?.toIntOrNull()?.let { Difficulty.values().getOrNull(it) }
                ?: runCatching { Difficulty.valueOf(difficulty.orEmpty().trim().uppercase()) }
                    .getOrDefault(Difficulty.MEDIUM),
            ratingAvg = ratingAvg ?: 0f,
            ingredients = ingredients.orEmpty(),
            method = (method ?: steps).orEmpty(),
            authorName = authorName.orEmpty()
        )
    }
}
