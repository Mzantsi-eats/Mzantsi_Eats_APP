package com.mzantsi.table.data

enum class Difficulty { EASY, MEDIUM, HARD }

enum class AppLanguage { EN, ZU, ST }

data class Recipe(
    val recipeId: Int,
    val title: String,
    val culture: String, // e.g. Xhosa, Zulu, Cape Malay, Sesotho, Braai, Coloured
    val province: String = "",
    val description: String,
    val imageRes: String = "", // placeholder for a drawable/URL reference
    val prepTimeMinutes: Int,
    val servings: Int,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val ratingAvg: Float = 0f,
    val ingredients: List<String>,
    val method: List<String>,
    val authorName: String = ""
)

data class Review(
    val reviewId: Int,
    val recipeId: Int,
    val userId: Int,
    val rating: Int,
    val comment: String
)

data class User(
    val userId: Int,
    val name: String,
    val email: String,
    val dietaryPrefs: String = "",
    val languagePref: AppLanguage = AppLanguage.EN,
    val recipesAdded: Int = 0
)
