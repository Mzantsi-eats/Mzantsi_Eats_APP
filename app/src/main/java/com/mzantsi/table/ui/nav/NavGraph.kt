package com.mzantsi.table.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.CommunityData
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

// The screens that show the bottom navigation bar.
private val bottomNavScreens = listOf(Screen.Home, Screen.Explore, Screen.AddRecipe, Screen.Saved, Screen.Profile)

// Sets up every screen and how the user moves between them.
// All screens get their data from the same AppViewModel.
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

    // Show save/load problems (for example the Firestore rules blocking a write) as a message at the bottom.
    LaunchedEffect(appViewModel.lastError) {
        appViewModel.lastError?.let {
            snackbarHostState.showSnackbar(it)
            appViewModel.clearLastError()
        }
    }

    // If the user is still signed in from last time, open Home instead of the Launch screen.
    val startRoute = remember {
        if (appViewModel.currentUser != null) Screen.Home.route else Screen.Launch.route
    }

    // When register, login or Google sign-in works (currentUser is no longer null), go to Home.
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
      // Everything the Community screens show, and what happens when the user taps something there.
      val communityState = CommunityState(
          recipes = appViewModel.recipes,
          groups = CommunityData.groups,
          cooks = appViewModel.cooks,
          posts = appViewModel.communityPosts,
          joined = appViewModel.joinedCommunities,
          following = appViewModel.followingCooks,
          myKey = appViewModel.currentUserKey,
          myName = appViewModel.currentUser?.name.orEmpty()
      )
      val communityActions = CommunityActions(
          onToggleJoin = appViewModel::toggleJoin,
          onToggleFollow = appViewModel::toggleFollow,
          onShare = appViewModel::shareAttempt,
          onRate = appViewModel::ratePost,
          onCheer = appViewModel::toggleCheer,
          onEncourage = appViewModel::addEncouragement,
          onRecipeClick = { navController.navigate(Screen.RecipeDetail.of(it.recipeId)) },
          onCommunityClick = { navController.navigate(Screen.CommunityGroup.of(it)) },
          onCookClick = { navController.navigate(Screen.CookProfile.of(it)) }
      )
      Column(Modifier.padding(padding)) {
        // Sync banner: offline = downloaded recipes still work and changes wait in a queue;
        // online with a queue = "Syncing..."; otherwise nothing is shown.
        if (appViewModel.currentUser != null) {
            val pending = appViewModel.totalPending
            val bannerText = when {
                !appViewModel.isOnline && pending > 0 ->
                    "You're offline. $pending change${if (pending == 1) "" else "s"} will sync when you're back online. Tap to open your downloaded recipes."
                !appViewModel.isOnline ->
                    "You're offline. Your downloaded recipes are still available - tap to open them."
                pending > 0 -> "Syncing $pending change${if (pending == 1) "" else "s"}..."
                else -> null
            }
            if (bannerText != null) {
                Text(
                    text = bannerText,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (appViewModel.isOnline) Color(0xFF2E7D32) else Color(0xFF5A5A5A))
                        .clickable(enabled = !appViewModel.isOnline) { navController.navigateSingleTop(Screen.Saved.route) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier
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
                    onClearError = appViewModel::clearAuthError,
                    registeredEmail = appViewModel.registeredEmail,
                    onRegisteredHandled = appViewModel::clearRegisteredEmail
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
                    onClearError = appViewModel::clearAuthError,
                    registeredEmail = appViewModel.registeredEmail,
                    onRegisteredHandled = appViewModel::clearRegisteredEmail
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    language = appViewModel.language,
                    recipes = appViewModel.recipes,
                    onRecipeClick = { navController.navigate(Screen.RecipeDetail.of(it.recipeId)) },
                    onExploreClick = { navController.navigateSingleTop(Screen.Explore.route) },
                    onCultureClick = { navController.navigate(Screen.Culture.of(it)) },
                    cooks = appViewModel.cooks,
                    onCommunityClick = { navController.navigate(Screen.Community.of()) },
                    onCookClick = { navController.navigate(Screen.CookProfile.of(it)) }
                )
            }
            composable(
                route = Screen.Community.route,
                arguments = listOf(navArgument("share") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { entry ->
                CommunityHubScreen(
                    state = communityState,
                    actions = communityActions,
                    shareRecipeId = entry.arguments?.getString("share")?.toIntOrNull(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.CommunityGroup.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                CommunityDetailScreen(
                    communityId = entry.arguments?.getString("id").orEmpty(),
                    state = communityState,
                    actions = communityActions,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.CookProfile.route,
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { entry ->
                CookProfileScreen(
                    cookName = entry.arguments?.getString("name").orEmpty(),
                    state = communityState,
                    actions = communityActions,
                    onBack = { navController.popBackStack() }
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
                    onBack = { navController.popBackStack() },
                    downloadedIds = appViewModel.downloadedRecipeIds,
                    onDownloadAll = appViewModel::downloadRecipes
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
                // Load the reviews of this recipe when the screen opens.
                LaunchedEffect(id) {
                    if (id != null) appViewModel.loadReviews(id)
                }
                if (recipe == null) {
                    RecipeNotFound(onBack = { navController.popBackStack() })
                } else {
                    RecipeDetailScreen(
                        recipe = recipe,
                        isSaved = recipe.recipeId in appViewModel.savedRecipeIds,
                        language = appViewModel.language,
                        reviews = appViewModel.reviews,
                        onToggleSave = { appViewModel.toggleSaved(recipe.recipeId) },
                        onMarkCompleted = { appViewModel.markCompleted(recipe) },
                        onAddReview = { rating, comment -> appViewModel.addReview(recipe.recipeId, rating, comment) },
                        onBack = { navController.popBackStack() },
                        isDownloaded = recipe.recipeId in appViewModel.downloadedRecipeIds,
                        onToggleDownload = {
                            if (recipe.recipeId in appViewModel.downloadedRecipeIds) appViewModel.removeDownload(recipe.recipeId)
                            else appViewModel.downloadRecipe(recipe)
                        }
                    )
                    appViewModel.justCompletedRecipe?.let { completed ->
                        CompletionCelebrationDialog(
                            recipe = completed,
                            language = appViewModel.language,
                            onDismiss = { appViewModel.dismissCelebration() },
                            onShareWithCommunity = {
                                appViewModel.dismissCelebration()
                                navController.navigate(Screen.Community.of(completed.recipeId))
                            }
                        )
                    }
                }
            }
            composable(Screen.Saved.route) {
                SavedScreen(
                    savedRecipes = appViewModel.recipes.filter { it.recipeId in appViewModel.savedRecipeIds },
                    onRecipeClick = { navController.navigate(Screen.RecipeDetail.of(it.recipeId)) },
                    downloadedRecipes = appViewModel.downloadedRecipes,
                    onRemoveDownload = { appViewModel.removeDownload(it.recipeId) },
                    onDownloadAllSaved = {
                        appViewModel.downloadRecipes(appViewModel.recipes.filter { it.recipeId in appViewModel.savedRecipeIds })
                    },
                    startOnDownloads = !appViewModel.isOnline
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
                    onChangePassword = appViewModel::changePassword,
                    isBusy = appViewModel.isLoading,
                    onSignOut = {
                        scope.launch { GoogleSignInManager.signOut(context) } // signs out of Google
                        appViewModel.signOut()                                // clears the user and their data from the app
                        navController.navigate(Screen.Launch.route) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
      }
    }
}

// Used by the bottom bar: opens a tab without stacking many copies of the same screen.
private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
