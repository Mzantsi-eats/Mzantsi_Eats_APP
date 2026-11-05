package com.mzantsi.table.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.MockData
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.User

class AppViewModel : ViewModel() {

    var language by mutableStateOf(AppLanguage.EN)
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    // Prototype-only in-memory "backend": email (lowercased) -> (user, password).
    // A real build swaps this for MzantsiApiService register/login calls.
    private val accounts = mutableMapOf(
        "demo@mzantsi.com" to (User(userId = 0, name = "Demo User", email = "demo@mzantsi.com") to "password123")
    )
    private var nextUserId = 1

    // Backed by MockData; a real build swaps this for MzantsiApiService calls.
    private val _recipes = mutableStateOf(MockData.recipes)
    val recipes get() = _recipes.value

    var savedRecipeIds by mutableStateOf(setOf<Int>())
        private set

    var justCompletedRecipe by mutableStateOf<Recipe?>(null)
        private set

    fun setLanguage(lang: AppLanguage) {
        language = lang
    }

    /** Returns null on success, or a user-facing error message on failure. */
    fun register(name: String, email: String, password: String): String? {
        val trimmedName = name.trim()
        val normalizedEmail = email.trim().lowercase()

        if (trimmedName.isBlank()) return "Please enter your full name."
        if (!normalizedEmail.contains("@") || !normalizedEmail.contains(".")) {
            return "Please enter a valid email address."
        }
        if (password.length < 6) return "Password must be at least 6 characters."
        if (accounts.containsKey(normalizedEmail)) {
            return "An account with that email already exists — try Login instead."
        }

        val user = User(userId = nextUserId++, name = trimmedName, email = normalizedEmail, languagePref = language)
        accounts[normalizedEmail] = user to password
        currentUser = user
        return null
    }

    /** Returns null on success, or a user-facing error message on failure. */
    fun login(email: String, password: String): String? {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || password.isBlank()) {
            return "Please enter both email and password."
        }
        val account = accounts[normalizedEmail]
            ?: return "No account found for that email — try Register instead."
        if (account.second != password) return "Incorrect password."

        currentUser = account.first.copy(languagePref = language)
        return null
    }

    /** "Continue with Google" placeholder: creates the account on first use, signs in on repeat use. */
    fun continueWithGoogle() {
        val email = "user@gmail.com"
        val existing = accounts[email]
        currentUser = if (existing != null) {
            existing.first.copy(languagePref = language)
        } else {
            val user = User(userId = nextUserId++, name = "Google User", email = email, languagePref = language)
            accounts[email] = user to ""
            user
        }
    }

    fun signOut() {
        currentUser = null
    }

    fun toggleSaved(recipeId: Int) {
        savedRecipeIds = if (recipeId in savedRecipeIds) savedRecipeIds - recipeId else savedRecipeIds + recipeId
    }

    fun addRecipe(recipe: Recipe) {
        _recipes.value = _recipes.value + recipe
        currentUser = currentUser?.copy(recipesAdded = (currentUser?.recipesAdded ?: 0) + 1)
    }

    fun markCompleted(recipe: Recipe) {
        justCompletedRecipe = recipe
    }

    fun dismissCelebration() {
        justCompletedRecipe = null
    }

    fun recipeById(id: Int): Recipe? = recipes.find { it.recipeId == id }
}
