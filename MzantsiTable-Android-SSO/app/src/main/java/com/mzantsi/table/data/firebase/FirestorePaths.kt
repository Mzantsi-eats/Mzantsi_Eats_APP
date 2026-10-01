package com.mzantsi.table.data.firebase

/**
 * Collection names used in Firestore. If your existing database uses different names,
 * change them here (and only here).
 *
 *   users/{uid}                      profile + settings + savedRecipeIds
 *   users/{uid}/completed/{recipeId} recipes the user marked "Completed"
 *   recipes/{recipeId}               community recipes
 */
object FirestorePaths {
    const val USERS = "users"
    const val RECIPES = "recipes"
    const val COMPLETED = "completed"
}

/**
 * Field names inside users/{uid}. The first six match the documents that already exist in
 * your Firestore (capitalised); the rest are new and follow the same style.
 * DietaryPrefs is a comma-separated string (e.g. "Vegan,Halal"), as in your existing documents.
 */
object UserFields {
    const val USER_ID = "UserId"
    const val NAME = "Name"
    const val EMAIL = "Email"
    const val LANGUAGE = "LanguagePref"          // "en" | "zu" | "st"
    const val DIETARY = "DietaryPrefs"           // "Vegan,Halal"
    const val RECIPES_ADDED = "RecipesAdded"
    const val AUTH_PROVIDER = "AuthProvider"     // "EMAIL" | "GOOGLE"
    const val PHOTO_URL = "PhotoUrl"
    const val NOTIFICATIONS = "NotificationsEnabled"
    const val SAVED = "SavedRecipeIds"           // array of numbers
}
