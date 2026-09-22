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

    fun register(name: String, email: String) {
        currentUser = User(userId = 1, name = name, email = email, languagePref = language)
    }

    fun login(email: String) {
        currentUser = User(userId = 1, name = currentUser?.name ?: "User", email = email, languagePref = language)
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
