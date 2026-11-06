package com.mzantsi.table.ui.nav

import android.net.Uri

sealed class Screen(val route: String) {
    data object Launch : Screen("launch")
    data object Register : Screen("register")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Explore : Screen("explore")
    data object AddRecipe : Screen("add_recipe")
    data object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun of(recipeId: Int) = "recipe_detail/$recipeId"
    }
    data object Culture : Screen("culture/{culture}") {
        fun of(culture: String) = "culture/${Uri.encode(culture)}"
    }
    data object Saved : Screen("saved")
    data object Community : Screen("community?share={share}") {
        const val BASE = "community"
        fun of(shareRecipeId: Int? = null) = if (shareRecipeId == null) BASE else "community?share=$shareRecipeId"
    }
    data object CommunityGroup : Screen("community/group/{id}") {
        fun of(id: String) = "community/group/${Uri.encode(id)}"
    }
    data object CookProfile : Screen("community/cook/{name}") {
        fun of(name: String) = "community/cook/${Uri.encode(name)}"
    }
    data object Profile : Screen("profile")
    data object Notifications : Screen("notifications")
}
