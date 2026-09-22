package com.mzantsi.table.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mzantsi.table.data.AppLanguage
import com.mzantsi.table.data.MockData
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.User
import androidx.lifecycle.viewModelScope
import com.mzantsi.table.data.api.ApiClient
import com.mzantsi.table.data.api.CreateProfileRequest
import com.mzantsi.table.data.api.UpdateSettingsRequest
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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

    fun changeLanguage(lang: AppLanguage) {
        language = lang
    }

    /** registers a new user using firebase authentication */
    fun register(name: String, email: String, password: String, onResult: (String?) -> Unit) {
        val trimmedName = name.trim()
        val normalizedEmail = email.trim().lowercase()

        if (trimmedName.isBlank()) {
            onResult("Please enter your full name.")
            return
        }

        if (!normalizedEmail.contains("@") || !normalizedEmail.contains(".")) {
            onResult("Please enter a valid email address.")
            return
        }
        if (password.length < 6) {
            onResult("Password must be at least 6 characters.")
            return
        }

        auth.createUserWithEmailAndPassword(normalizedEmail, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser

                if (firebaseUser != null) {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(trimmedName).build()

                    firebaseUser.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            viewModelScope.launch {
                                try {
                                    val profile = ApiClient.api.createProfile(
                                        CreateProfileRequest(
                                            name = trimmedName,
                                            email = firebaseUser.email ?: normalizedEmail
                                        )
                                    )

                                    currentUser = User(userId = profile.userId, name = profile.name, email = profile.email, dietaryPrefs = profile.dietaryPrefs, languagePref = when (profile.languagePref.lowercase()) {
                                        "zu" -> AppLanguage.ZU
                                        "st" -> AppLanguage.ST
                                        else -> AppLanguage.EN
                                    },
                                        recipesAdded = profile.recipesAdded)

                                    language = currentUser!!.languagePref

                                    onResult(null)

                                } catch (e: Exception) {
                                    onResult(
                                        e.message ?: "Account created, but the API profile could not be created."
                                    )
                                }
                            }
                        }
                        else {
                            onResult(profileTask.exception?.message ?: "Account created, but profile could not be saved.")
                        }
                    }

                }
                else {
                    onResult("Registration succeeded, but the user profile could not beb loaded.")
                }
            }
            else {
                onResult(task.exception?.message ?: "Registration failed. Please try again.")
            }
        }

    }

    /** logs in an existing user using firebase authentication */
    fun login(email: String, password: String, onResult: (String?) -> Unit) {
        val normalizedEmail = email.trim().lowercase()

        if (normalizedEmail.isBlank() || password.isBlank()) {
            onResult("Please enter both email and password.")
            return
        }

        auth.signInWithEmailAndPassword(normalizedEmail, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                loadCurrentUser { error ->
                    if (error != null) {
                        onResult(error)
                    }
                    else {
                        onResult(null)
                    }
                }

            }
            else {
                val exception = task.exception
                onResult(
                    "Error code: ${exception?.let { (it as? com.google.firebase.auth.FirebaseAuthException) ?.errorCode }
                    }\n" +
                    "Message: ${exception?.message}"
                )
            }
        }
    }


    /** "Continue with Google" login will connect to firebase */
    fun continueWithGoogle(onResult: (String?) -> Unit) {
        onResult("Google sign-in is not connected yet.")
    }

    fun signOut() {
        auth.signOut()
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

    fun loadCurrentUser(onResult: (String?) -> Unit = {}) {
        viewModelScope.launch {
            try {

                val profile = ApiClient.api.getCurrentUser()

                val appLanguage = when (profile.languagePref.lowercase()) {
                    "zu" -> AppLanguage.ZU
                    "st" -> AppLanguage.ST
                    else -> AppLanguage.EN
                }

                currentUser = User(userId = profile.userId, name = profile.name, email = profile.email, dietaryPrefs = profile.dietaryPrefs, languagePref = appLanguage, recipesAdded = profile.recipesAdded)

                language = appLanguage

                onResult(null)
            }
            catch (e: Exception) {
                onResult(e.message ?: "Could not load your profile.")
            }
        }
    }

    fun updateSettings(dietaryPrefs: String, languagePref: AppLanguage, onResult: (String?) -> Unit = {}) {

        //change language immediately
        language = languagePref

        //updates the local user immediately
        currentUser = currentUser?.copy(
            languagePref = languagePref,
            dietaryPrefs = dietaryPrefs
        )

        viewModelScope.launch {
            try {
                val languageCode = when (languagePref) {
                    AppLanguage.EN -> "en"
                    AppLanguage.ZU -> "zu"
                    AppLanguage.ST -> "st"
                }

                val profile = ApiClient.api.updateSettings(UpdateSettingsRequest(dietaryPrefs = dietaryPrefs, languagePref = languageCode))

                val appLanguage = when (profile.languagePref.lowercase()) {
                    "zu" -> AppLanguage.ZU
                    "st" -> AppLanguage.ST
                    else -> AppLanguage.EN
                }

                currentUser = User(userId = profile.userId, name = profile.name, email = profile.email, dietaryPrefs = profile.dietaryPrefs, languagePref = appLanguage, recipesAdded = profile.recipesAdded)

                language = appLanguage

                onResult(null)
            }
            catch (e: Exception) {
                onResult(e.message ?: "Could not save your settings.")
            }
        }
    }
}