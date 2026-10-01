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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mzantsi.table.auth.GoogleSignInManager
import com.mzantsi.table.ui.screens.*
import kotlinx.coroutines.launch
import com.mzantsi.table.viewmodel.AppViewModel

private val bottomNavScreens = listOf(Screen.Home, Screen.Explore, Screen.AddRecipe, Screen.Saved, Screen.Profile)

@Composable
fun MzantsiNavGraph() {
    val navController = rememberNavController()
    val appViewModel: AppViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavScreens.any { it.route == currentRoute }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Surface Firestore problems (e.g. rules denying a write) instead of failing silently.
    LaunchedEffect(appViewModel.lastError) {
        appViewModel.lastError?.let {
            snackbarHostState.showSnackbar(it)
            appViewModel.clearLastError()
        }
    }

    // Already signed in from a previous session? Skip straight to Home.
    val startRoute = remember {
        if (appViewModel.currentUser != null) Screen.Home.route else Screen.Launch.route
    }

    // As soon as register / login / Google SSO succeeds (currentUser appears) leave the auth screens.
    LaunchedEffect(appViewModel.currentUser?.email) {
        val route = navController.currentBackStackEntry?.destination?.route
        if (appViewModel.currentUser != null &&
            (route == Screen.Register.route || route == Screen.Login.route)
        ) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Launch.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = { navController.navigateSingleTop(Screen.Home.route) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
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
            startDestination = startRoute,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Launch.route) {
                LaunchScreen(
                    language = appViewModel.language,
                    onLanguageChange = appViewModel::setLanguage,
                    onLogin = { navController.navigate(Screen.Login.route) },
                    onGetStarted = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                AuthScreen(
                    language = appViewModel.language,
                    startOnLogin = false,
                    isLoading = appViewModel.isLoading,
                    authError = appViewModel.authError,
                    onRegister = appViewModel::register,
                    onLogin = appViewModel::login,
                    onGoogleSignedIn = appViewModel::onGoogleSignedIn,
                    onGoogleError = appViewModel::onGoogleError,
                    onClearError = appViewModel::clearAuthError
                )
            }
            composable(Screen.Login.route) {
                AuthScreen(
                    language = appViewModel.language,
                    startOnLogin = true,
                    isLoading = appViewModel.isLoading,
                    authError = appViewModel.authError,
                    onRegister = appViewModel::register,
                    onLogin = appViewModel::login,
                    onGoogleSignedIn = appViewModel::onGoogleSignedIn,
                    onGoogleError = appViewModel::onGoogleError,
                    onClearError = appViewModel::clearAuthError
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    language = appViewModel.language,
                    recipes = appViewModel.recipes,
                    onRecipeClick = { navController.navigate(Screen.RecipeDetail.of(it.recipeId)) },
                    onExploreClick = { navController.navigateSingleTop(Screen.Explore.route) },
                    onCultureClick = { navController.navigate(Screen.Culture.of(it)) }
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    recipes = appViewModel.recipes,
                    onCultureClick = { navController.navigate(Screen.Culture.of(it)) }
                )
            }
            composable(
                route = Screen.Culture.route,
                arguments = listOf(navArgument("culture") { type = NavType.StringType })
            ) { entry ->
                val culture = entry.arguments?.getString("culture").orEmpty()
                CultureRecipesScreen(
                    culture = culture,
                    recipes = appViewModel.recipes.filter { it.culture.equals(culture, ignoreCase = true) },
                    onRecipeClick = { navController.navigate(Screen.RecipeDetail.of(it.recipeId)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddRecipe.route) {
                AddRecipeScreen(
                    nextRecipeId = (appViewModel.recipes.maxOfOrNull { it.recipeId } ?: 0) + 1,
                    authorName = appViewModel.currentUser?.name?.ifBlank { null } ?: "You",
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
                    language = appViewModel.language,
                    dietaryPreferences = appViewModel.dietaryPreferences,
                    notificationsEnabled = appViewModel.notificationsEnabled,
                    onNameChange = { name, bio -> appViewModel.updateProfile(name, bio) },
                    onDietaryToggle = { pref -> appViewModel.toggleDietaryPreference(pref) },
                    onNotificationsToggle = { enabled -> appViewModel.toggleNotifications(enabled) },
                    onLanguageChange = { lang -> appViewModel.setLanguage(lang) },
                    onSignOut = {
                        scope.launch { GoogleSignInManager.signOut(context) } // Firebase + Credential Manager
                        appViewModel.signOut()                                // clears session + user state
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
