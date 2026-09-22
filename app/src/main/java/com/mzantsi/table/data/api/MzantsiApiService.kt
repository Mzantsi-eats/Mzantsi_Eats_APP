package com.mzantsi.table.data.api

import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.Review
import com.mzantsi.table.data.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Mirrors the endpoints described in the "API Design" section of the POE:
 * GET/api/recipes, POST/api/recipes, etc. Point Retrofit's base URL at
 * wherever that API is hosted once one is built and deployed —
 * the app currently runs entirely on MockData so it works with no backend.
 */

data class CreateProfileRequest(
    val name: String,
    val email: String
)

data class UpdateSettingsRequest(
    val dietaryPrefs: String,
    val languagePref: String
)

data class UserResponse(
    val userId: String,
    val name: String,
    val email: String,
    val dietaryPrefs: String = "",
    val languagePref: String = "en",
    val recipesAdded: Int = 0
)
interface MzantsiApiService {

    @GET("api/recipes")
    suspend fun getRecipes(
        @Query("cuisine") cuisine: String? = null,
        @Query("language") language: String? = null
    ): List<Recipe>

    @GET("api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): Recipe

    @POST("api/recipes")
    suspend fun createRecipe(@Body recipe: Recipe): Recipe

    @POST("api/recipes/{id}/reviews")
    suspend fun submitReview(@Path("id") id: Int, @Body review: Review): Review

    @GET("api/users/me")
    suspend fun getCurrentUser(): UserResponse

    @POST("api/users/me")
    suspend fun createProfile(@Body request: CreateProfileRequest): UserResponse

    @PUT("api/users/me/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): UserResponse

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: Int): User
}
