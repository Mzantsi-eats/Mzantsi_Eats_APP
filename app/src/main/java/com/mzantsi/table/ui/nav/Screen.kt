package com.mzantsi.table.ui.nav

import android.net.Uri

// Every screen in the app and its route (the "address" used to open it).
sealed class Screen(val route: String) {
    data object Launch : Screen("launch")
    data object Register : Screen("register")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Explore : Screen("explore")
    data object AddRecipe : Screen("add_recipe")
    // {recipeId} and {culture} are filled in with the real value when the screen is opened.
    data object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun of(recipeId: Int) = "recipe_detail/$recipeId"
    }
    data object Culture : Screen("culture/{culture}") {
        fun of(culture: String) = "culture/${Uri.encode(culture)}"
    }
    data object Saved : Screen("saved")
    // Community Hub. {share} is an optional recipe id: when set, the "Share your attempt" form opens straight away.
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
}
