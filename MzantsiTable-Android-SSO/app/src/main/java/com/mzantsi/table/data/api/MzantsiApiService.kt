package com.mzantsi.table.data.api

import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.Review
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface MzantsiApiService {

    // ---- Recipes ----
    @GET("api/recipes")
    suspend fun getRecipes(
        @Query("cuisine") cuisine: String? = null,
        @Query("language") language: String? = null
    ): List<RecipeDto>

    @GET("api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): RecipeDto

    @POST("api/recipes")
    suspend fun createRecipe(@Body recipe: Recipe): RecipeDto

    @POST("api/recipes/{id}/reviews")
    suspend fun submitReview(@Path("id") id: Int, @Body review: Review): Review

    // ---- Users ----
    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): UserDto

    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): UserDto

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: Int): UserDto

    @PUT("api/users/{id}/settings")
    suspend fun updateSettings(@Path("id") id: Int, @Body request: SettingsRequest): UserDto
}
