package com.mzantsi.table.data

enum class Difficulty { EASY, MEDIUM, HARD }

data class Recipe(
    val recipeId: Int,
    val title: String,
    val culture: String, // e.g. Xhosa, Zulu, Cape Malay, Sesotho, Braai, Coloured
    val province: String = "",
    val description: String,
    // for photos uploaded by the user on the Add Recipe screen.
    val imageRes: String = "",
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int = 0,
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


enum class AuthProvider { EMAIL, GOOGLE }

data class User(
    val userId: Int = 0,
    // Firebase Auth uid
    val uid: String = "",
    val name: String,
    val email: String,
    val dietaryPrefs: String = "",
    val languagePref: AppLanguage = AppLanguage.EN,
    val recipesAdded: Int = 0,
    val authProvider: AuthProvider = AuthProvider.EMAIL,
    val photoUrl: String = ""
)

enum class AppLanguage { EN, ZU, ST }
