package com.mzantsi.table.data

enum class Difficulty { EASY, MEDIUM, HARD }

data class Recipe(
    val recipeId: Int,
    val title: String,
    val culture: String,
    val province: String = "",
    val description: String,
    val imageRes: String = "",
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int = 0,
    val servings: Int,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val ratingAvg: Float = 0f,
    val ingredients: List<String>,
    val method: List<String>,
    val authorName: String = "",
    val authorUid: String = ""
)

data class Review(
    val reviewId: Int,
    val recipeId: Int,
    val userId: Int,
    val rating: Int,
    val comment: String,
    val userName: String = "",
    val createdAtMillis: Long = 0L
)

enum class AuthProvider { EMAIL, GOOGLE }

data class User(
    val userId: Int = 0,
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
