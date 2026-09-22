package com.mzantsi.table.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.MockData
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.RecipeRepository
import com.mzantsi.table.data.Review
import com.mzantsi.table.data.User
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val TAG = "AppViewModel"

    //Language
    private var _language by mutableStateOf(AppLanguage.EN)
    val language get() = _language

    fun setLanguage(lang: AppLanguage) {
        Log.d(TAG, "Language changed to $lang")
        _language = lang
    }

    // User
    var currentUser by mutableStateOf<User?>(null)
        private set

    //  Recipes - Starts with MockData so the UI never appears empty, then refreshes from API.
    private var _recipes by mutableStateOf(MockData.recipes)
    val recipes get() = _recipes

    var isLoading by mutableStateOf(false)
        private set

    var lastError by mutableStateOf<String?>(null)
        private set

    // Saved / celebration
    var savedRecipeIds by mutableStateOf(setOf<Int>())
        private set
    var justCompletedRecipe by mutableStateOf<Recipe?>(null)
        private set

    //  Settings
    var notificationsEnabled by mutableStateOf(true)
        private set

    var dietaryPreferences by mutableStateOf(setOf<String>())
        private set

    // Initialisation
    init {
        refreshRecipes()
    }

    /** Fetches recipes from the API. Called on launch and can be called manually. */
    fun refreshRecipes() {
        viewModelScope.launch {
            isLoading = true
            lastError = null
            val fetched = repository.fetchRecipes()
            _recipes = fetched
            isLoading = false
            Log.d(TAG, "Recipes refreshed: ${fetched.size} items")
        }
    }

    // ---------------- Auth ----------------
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            lastError = null
            val user = repository.register(name, email, password)
            if (user != null) {
                currentUser = user.copy(languagePref = language)
                Log.i(TAG, "Registered: ${user.email}")
            } else {
                // Fallback: still log the user in locally so the UI isn't blocked
                currentUser = User(userId = 1, name = name, email = email, languagePref = language)
                lastError = "Could not reach server — working offline"
                Log.w(TAG, "Registration fell back to local user")
            }
            isLoading = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            lastError = null
            val user = repository.login(email, password)
            if (user != null) {
                currentUser = user.copy(languagePref = language)
                Log.i(TAG, "Logged in: ${user.email}")
            } else {
                currentUser = User(
                    userId = 1,
                    name = currentUser?.name ?: "User",
                    email = email,
                    languagePref = language
                )
                lastError = "Could not reach server — working offline"
                Log.w(TAG, "Login fell back to local user")
            }
            isLoading = false
        }
    }

    // ---------------- Recipes CRUD ----------------
    fun addRecipe(recipe: Recipe) {
        // Optimistic update — show it immediately
        _recipes = _recipes + recipe
        currentUser = currentUser?.copy(recipesAdded = (currentUser?.recipesAdded ?: 0) + 1)

        viewModelScope.launch {
            val created = repository.createRecipe(recipe)
            if (created != null) {
                Log.i(TAG, "Recipe created: ${created.title} (id ${created.recipeId})")
            } else {
                lastError = "Recipe saved locally only — server unreachable"
            }
        }
    }

    // ---------------- Saved / celebration ----------------
    fun toggleSaved(recipeId: Int) {
        savedRecipeIds = if (recipeId in savedRecipeIds) {
            savedRecipeIds - recipeId
        } else {
            savedRecipeIds + recipeId
        }
        Log.d(TAG, "Saved list now: $savedRecipeIds")
    }

    fun markCompleted(recipe: Recipe) {
        justCompletedRecipe = recipe
        Log.i(TAG, "Completed: ${recipe.title}")
    }

    fun dismissCelebration() {
        justCompletedRecipe = null
    }

    fun recipeById(id: Int): Recipe? = recipes.find { it.recipeId == id }

    // ---------------- Settings ----------------
    fun updateSettings(dietaryPrefs: String, languagePref: String) {
        val user = currentUser ?: return
        viewModelScope.launch {
            val updated = repository.updateSettings(user.userId, dietaryPrefs, languagePref)
            if (updated != null) {
                currentUser = updated
                Log.i(TAG, "Settings updated on server")
            } else {
                // Still update locally so the UI reflects the change
                currentUser =
                    user.copy(languagePref = AppLanguage.valueOf(languagePref.uppercase()))
                lastError = "Saved locally — server unreachable"
                Log.w(TAG, "Settings saved locally only")
            }
        }
    }
    // Add near the other state variables:
    var reviews by mutableStateOf<List<Review>>(emptyList())
        private set

    fun submitReview(recipeId: Int, rating: Int, comment: String) {
        val user = currentUser ?: return
        val review = Review(
            reviewId = (reviews.maxOfOrNull { it.reviewId } ?: 0) + 1,
            recipeId = recipeId,
            userId = user.userId,
            rating = rating,
            comment = comment
        )
        reviews = reviews + review
        viewModelScope.launch {
            repository.submitReview(recipeId, review)
            Log.i(TAG, "Review submitted: $rating stars for recipe $recipeId")
        }
    }

    fun reviewsFor(recipeId: Int): List<Review> = reviews.filter { it.recipeId == recipeId }

    fun toggleDietaryPreference(pref: String) {
        dietaryPreferences = if (pref in dietaryPreferences) {
            dietaryPreferences - pref
        } else {
            dietaryPreferences + pref
        }
        Log.d(TAG, "Dietary preferences: $dietaryPreferences")
        syncSettingsWithServer()
    }


    fun toggleNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
        Log.d(TAG, "Notifications enabled: $enabled")
        syncSettingsWithServer()
    }

    fun updateProfile(name: String, bio: String) {
        currentUser = currentUser?.copy(name = name)
        Log.i(TAG, "Profile updated: name=$name")
    }

    //Pushes the current settings to the server.
    private fun syncSettingsWithServer() {
        val user = currentUser ?: return
        viewModelScope.launch {
            repository.updateSettings(
                userId = user.userId,
                dietaryPrefs = dietaryPreferences.joinToString(","),
                languagePref = language.name.lowercase()
            )
            Log.d(TAG, "Settings synced: $dietaryPreferences | $language | notif=$notificationsEnabled")
        }
    }
}