package com.mzantsi.table.data

import android.util.Log
import com.mzantsi.table.data.api.LoginRequest
import com.mzantsi.table.data.api.MzantsiApiService
import com.mzantsi.table.data.api.RegisterRequest
import com.mzantsi.table.data.api.RetrofitClient
import com.mzantsi.table.data.api.SettingsRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(
    private val api: MzantsiApiService = RetrofitClient.api
) {
    private val tag = "RecipeRepository"

    private suspend fun <T> safeCall(label: String, block: suspend () -> T): T? =
        withContext(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(tag, "$label failed: ${e.message}")
                null
            }
        }

    suspend fun fetchRecipes(): List<Recipe> {
        val remote = safeCall("fetchRecipes") { api.getRecipes() }
            ?.mapNotNull { it.toRecipe() }
            .orEmpty()
        return remote.ifEmpty { MockData.recipes }
    }

    suspend fun createRecipe(recipe: Recipe): Recipe? =
        safeCall("createRecipe") { api.createRecipe(recipe) }?.toRecipe()

    suspend fun register(name: String, email: String, password: String): User? =
        safeCall("register") { api.register(RegisterRequest(name, email, password)) }?.toUser()

    suspend fun login(email: String, password: String): User? =
        safeCall("login") { api.login(LoginRequest(email, password)) }?.toUser()

    suspend fun updateSettings(userId: Int, dietaryPrefs: String, languagePref: String): User? =
        safeCall("updateSettings") {
            api.updateSettings(userId, SettingsRequest(dietaryPrefs, languagePref))
        }?.toUser()
}
