package com.mzantsi.table.data.firebase

/**
 * The names of the collections in Firestore. If your database uses other names,
 * change them here only.
 *
 *   users/{uid}                        the profile, settings and saved recipe ids
 *   users/{uid}/completed/{recipeId}   recipes the user marked as "Completed"
 *   recipes/{recipeId}                 recipes added by users
 *   recipes/{recipeId}/reviews/{id}    reviews (stars + comment) for a recipe
 *   community_posts/{postId}           attempts shared in the Community Hub (with ratings, cheers, comments)
 */
object FirestorePaths {
    const val USERS = "users"
    const val RECIPES = "recipes"
    const val COMPLETED = "completed"
    const val REVIEWS = "reviews"
    const val POSTS = "community_posts"          // community_posts/{postId}  attempts shared in the Community Hub
}

/**
 * The field names inside users/{uid}. The first six match the documents that were already in
 * our Firestore (they start with a capital letter). The others were added later in the same style.
 * DietaryPrefs is one text value with commas, for example "Vegan,Halal".
 */
object UserFields {
    const val USER_ID = "UserId"
    const val NAME = "Name"
    const val EMAIL = "Email"
    const val LANGUAGE = "LanguagePref"          // "en", "zu" or "st"
    const val DIETARY = "DietaryPrefs"           // "Vegan,Halal"
    const val RECIPES_ADDED = "RecipesAdded"
    const val AUTH_PROVIDER = "AuthProvider"     // "EMAIL" or "GOOGLE"
    const val PHOTO_URL = "PhotoUrl"
    const val NOTIFICATIONS = "NotificationsEnabled"
    const val SAVED = "SavedRecipeIds"           // a list of numbers
    const val JOINED = "JoinedCommunities"       // a list of community ids, for example ["xhosa", "braai"]
    const val FOLLOWING = "FollowingCooks"       // a list of cook names
}
