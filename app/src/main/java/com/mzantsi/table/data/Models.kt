package com.mzantsi.table.data

// How hard a recipe is to cook.
enum class Difficulty { EASY, MEDIUM, HARD }

// One recipe in the app. The built-in recipes and the ones users add both use this class.
data class Recipe(
    val recipeId: Int,
    val title: String,
    val culture: String, // for example Xhosa, Zulu, Cape Malay, Sesotho, Braai, Coloured
    val province: String = "",
    val description: String,
    // Name of the picture in assets, or "file:..." for a photo the user picked on the Add Recipe screen.
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

// A star rating (1 to 5) and a short comment that a user leaves on a recipe.
data class Review(
    val reviewId: Int,
    val recipeId: Int,
    val userId: Int,
    val rating: Int,
    val comment: String,
    val userName: String = "",
    val createdAtMillis: Long = 0L
)

// How the user signed up: with email and password, or with Google.
enum class AuthProvider { EMAIL, GOOGLE }

// The person who is signed in.
data class User(
    val userId: Int = 0,
    // The Firebase user id (empty in offline mode)
    val uid: String = "",
    val name: String,
    val email: String,
    val dietaryPrefs: String = "",
    val languagePref: AppLanguage = AppLanguage.EN,
    val recipesAdded: Int = 0,
    val authProvider: AuthProvider = AuthProvider.EMAIL,
    val photoUrl: String = ""
)

// The languages the app supports: English, isiZulu and Sesotho.
enum class AppLanguage { EN, ZU, ST }
