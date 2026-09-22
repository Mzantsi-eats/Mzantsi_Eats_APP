package com.mzantsi.table.data.api

import com.mzantsi.table.data.Recipe
import com.mzantsi.table.data.Review
import com.mzantsi.table.data.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Mirrors the endpoints described in the "API Design" section of the POE:
 * GET/api/recipes, POST/api/recipes, etc., served by the companion
 * MzantsiTableApi (ASP.NET Core Web API) project. Point Retrofit's base URL at
 * wherever that API is hosted (e.g. an Azure App Service) once deployed —
 * the app currently runs entirely on MockData so it works with no backend.
 */
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

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: Int): User
}
