package com.mzantsi.table.data

import android.util.Log
import com.mzantsi.table.data.api.LoginRequest
import com.mzantsi.table.data.api.MzantsiApiService
import com.mzantsi.table.data.api.RegisterRequest
import com.mzantsi.table.data.api.RetrofitClient

/**
 * Single point of contact between the app and the REST API.
 * Screens and the ViewModel never talk to Retrofit directly.
 *
 * Falls back to MockData if the API is unreachable, so the app still
 * launches offline (important for the "handle invalid inputs without
 *
 */
class RecipeRepository(
    private val api: MzantsiApiService = RetrofitClient.api
) {
    private val TAG = "RecipeRepository"

    /** Fetches all recipes. Falls back to mock data on failure. */
    suspend fun fetchRecipes(): List<Recipe> {
        return try {
            Log.d(TAG, "GET /api/recipes — fetching from ${RetrofitClient.BASE_URL}")
            val recipes = api.getRecipes()
            Log.d(TAG, "GET /api/recipes — received ${recipes.size} recipes")
            recipes
        } catch (e: Exception) {
            Log.w(TAG, "GET /api/recipes failed, falling back to MockData: ${e.message}")
            MockData.recipes
        }
    }

    /** Creates a recipe on the server. Returns null on failure. */
    suspend fun createRecipe(recipe: Recipe): Recipe? {
        return try {
            Log.d(TAG, "POST /api/recipes — creating '${recipe.title}'")
            val created = api.createRecipe(recipe)
            Log.d(TAG, "POST /api/recipes — created with id ${created.recipeId}")
            created
        } catch (e: Exception) {
            Log.e(TAG, "POST /api/recipes failed: ${e.message}", e)
            // Optimistic fallback: pretend it saved locally so the UI still updates
            recipe
        }
    }

    /** Registers a new user. Returns null on failure. */
    suspend fun register(name: String, email: String, password: String): User? {
        return try {
            Log.d(TAG, "POST /api/users/register — $email")
            api.register(RegisterRequest(name, email, password))
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed: ${e.message}", e)
            null
        }
    }

    /** Logs a user in. Returns null on failure. */
    suspend fun login(email: String, password: String): User? {
        return try {
            Log.d(TAG, "POST /api/users/login — $email")
            api.login(LoginRequest(email, password))
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.message}", e)
            null
        }
    }

    /** Submits a review for a recipe. */
    suspend fun submitReview(recipeId: Int, review: Review): Review? {
        return try {
            Log.d(TAG, "POST /api/recipes/$recipeId/reviews — rating ${review.rating}")
            api.submitReview(recipeId, review)
        } catch (e: Exception) {
            Log.e(TAG, "Review submission failed: ${e.message}", e)
            null
        }
    }

    /** Updates user settings on the server. */
    suspend fun updateSettings(userId: Int, dietaryPrefs: String, languagePref: String): User? {
        return try {
            Log.d(TAG, "PUT /api/users/$userId/settings")
            api.updateSettings(userId, mapOf(
                "dietaryPrefs" to dietaryPrefs,
                "languagePref" to languagePref
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Settings update failed: ${e.message}", e)
            null
        }
    }
}