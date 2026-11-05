package com.mzantsi.table.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


// The endpoints of our ASP.NET Core Web API, for Retrofit.
// The app now uses Firebase instead, so this is kept for the API part of the POE.
interface MzantsiApiService {

    // Recipes
    @GET("api/recipes")
    suspend fun getRecipes(
        @Query("cuisine") cuisine: String? = null,
        @Query("language") language: String? = null
    ): List<RecipeDto>

    @GET("api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): RecipeDto

    @POST("api/recipes")
    suspend fun createRecipe(@Body recipe: RecipeRequest): RecipeDto

    // Recipes created on or after the given time (ISO text, for example 2026-08-01T00:00:00Z).
    // This is how the phone catches up after being offline.
    @GET("api/recipes/sync")
    suspend fun syncRecipes(@Query("since") since: String): List<RecipeDto>

    @POST("api/recipes/{id}/reviews")
    suspend fun submitReview(@Path("id") id: Int, @Body review: ReviewRequest): ReviewDto

    // Users
    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): UserDto

    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): UserDto

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: Int): UserDto

    @PUT("api/users/{id}/settings")
    suspend fun updateSettings(@Path("id") id: Int, @Body request: SettingsRequest): UserDto
}
