package com.mzantsi.table.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mzantsi.table.ui.screens.*
import com.mzantsi.table.viewmodel.AppViewModel

private val bottomNavScreens = listOf(Screen.Home, Screen.Explore, Screen.AddRecipe, Screen.Saved, Screen.Profile)

@Composable
fun MzantsiNavGraph() {
    val navController = rememberNavController()
    val appViewModel: AppViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavScreens.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = { navController.navigateSingleTop(Screen.Home.route) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Explore.route,
                        onClick = { navController.navigateSingleTop(Screen.Explore.route) },
                        icon = { Icon(Icons.Filled.Explore, contentDescription = "Explore") },
                        label = { Text("Explore") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.AddRecipe.route,
                        onClick = { navController.navigateSingleTop(Screen.AddRecipe.route) },
                        icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                        label = { Text("Add") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Saved.route,
                        onClick = { navController.navigateSingleTop(Screen.Saved.route) },
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = "Saved") },
                        label = { Text("Saved") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Profile.route,
                        onClick = { navController.navigateSingleTop(Screen.Profile.route) },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Launch.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Launch.route) {
                LaunchScreen(
                    language = appViewModel.language,
                    onLanguageChange = appViewModel::setLanguage,
                    onGetStarted = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                AuthScreen(
                    onRegister = { name, email, password -> appViewModel.register(name, email, password) },
                    onLogin = { email, password -> appViewModel.login(email, password) },
                    onContinueWithGoogle = { appViewModel.continueWithGoogle() },
                    onAuthenticated = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Launch.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    language = appViewModel.language,
                    recipes = appViewModel.recipes,
                    onRecipeClick = { navController.navigate(Screen.RecipeDetail.of(it.recipeId)) },
                    onExploreClick = { navController.navigateSingleTop(Screen.Explore.route) }
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    recipes = appViewModel.recipes,
                    onCultureClick = { }
                )
            }
            composable(Screen.AddRecipe.route) {
                AddRecipeScreen(
                    nextRecipeId = appViewModel.recipes.size + 1,
                    onSave = { appViewModel.addRecipe(it) },
                    onBack = { navController.navigateSingleTop(Screen.Home.route) }
                )
            }
            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("recipeId")
                val recipe = id?.let { appViewModel.recipeById(it) }
                if (recipe != null) {
                    RecipeDetailScreen(
                        recipe = recipe,
                        isSaved = recipe.recipeId in appViewModel.savedRecipeIds,
                        language = appViewModel.language,
                        onToggleSave = { appViewModel.toggleSaved(recipe.recipeId) },
                        onMarkCompleted = { appViewModel.markCompleted(recipe) },
                        onBack = { navController.popBackStack() }
                    )
                    appViewModel.justCompletedRecipe?.let { completed ->
                        CompletionCelebrationDialog(
                            recipe = completed,
                            language = appViewModel.language,
                            onDismiss = { appViewModel.dismissCelebration() }
                        )
                    }
                }
            }
            composable(Screen.Saved.route) {
                SavedScreen(
                    savedRecipes = appViewModel.recipes.filter { it.recipeId in appViewModel.savedRecipeIds },
                    onRecipeClick = { navController.navigate(Screen.RecipeDetail.of(it.recipeId)) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    user = appViewModel.currentUser,
                    onSignOut = {
                        appViewModel.signOut()
                        navController.navigate(Screen.Launch.route) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
