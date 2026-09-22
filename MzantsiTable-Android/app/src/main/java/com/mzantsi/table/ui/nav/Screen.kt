package com.mzantsi.table.ui.nav

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
    data object Saved : Screen("saved")
    data object Profile : Screen("profile")
}
