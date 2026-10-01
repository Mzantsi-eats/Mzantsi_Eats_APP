package com.mzantsi.table.data.api

import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.Difficulty
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.User

data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class SettingsRequest(val dietaryPrefs: String, val languagePref: String)


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
            imageRes = imageRes.orEmpty(),
            prepTimeMinutes = prepTimeMinutes ?: 0,
            cookTimeMinutes = cookTimeMinutes ?: 0,
            servings = servings ?: 1,
            difficulty = runCatching { Difficulty.valueOf(difficulty.orEmpty().uppercase()) }
                .getOrDefault(Difficulty.MEDIUM),
            ratingAvg = ratingAvg ?: 0f,
            ingredients = ingredients.orEmpty(),
            method = method.orEmpty(),
            authorName = authorName.orEmpty()
        )
    }
}
